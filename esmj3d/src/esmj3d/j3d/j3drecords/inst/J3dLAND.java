package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color4f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import nif.j3d.J3dNiGeometry;

import org.j3d.geom.GeometryData;

import tools.io.ESMByteConvert;
import utils.PhysAppearance;
import utils.source.TextureSource;

import com.sun.j3d.utils.geometry.GeometryInfo;

import esmLoader.common.data.record.IRecordStore;
import esmLoader.common.data.record.Record;
import esmj3d.data.shared.records.LAND;
import esmj3d.data.shared.records.LAND.ATXT;
import esmj3d.data.shared.records.LAND.BTXT;
import esmj3d.data.shared.records.LAND.VTXT;
import esmj3d.data.shared.records.LTEX;
import esmj3d.data.shared.records.TXST;
import esmj3d.j3d.TESLANDGen;

public class J3dLAND extends J3dRECOStatInst
{
	public static int GRID_COUNT = 32;

	public static final float TERRIAN_SQUARE_SIZE = 2.56f;// confirmed empirically

	//	NOTE nif x,y,z to j3d x,z,-y
	public static float HEIGHT_TO_J3D_SCALE = 0.04f; //where does this come from? 1/25th?

	public static float LAND_SIZE = GRID_COUNT * TERRIAN_SQUARE_SIZE; //= (32*2.56) = 81.92

	//LOD tristrip in 5.12 increments (2.56?)
	//public static float HEIGHT_TO_J3D_SCALE = 0.057f;

	/*Oblivion uses a coordinate system with units which, like in Morrowind, are 21.3 'units' to a foot, or 7 units to 10 centimeters 	 
	 (or to put it another way 64 units per yard [~70 units per metre]).	
	 The base of this system is an exterior cell which is 4096 x 4096 units or 192 x 192 feet or 58.5 x 58.5 meters.
	 
	 Another way of approximation is that any race at height 1.0 will be 128 'units' tall, and we assume that the average height of the people of 
	 Tamriel is 6 feet. 128 divided by 6 is 21+(1/3) (twenty-one and a third). Round this down, and 21 units per foot gives an average height of 
	 about 6' 1.14". This seems to be a reasonable approximation.
	 
	 When importing height values for terrain into TESCS, 1 person height is only 64 units. Which equates to a smidge under 35 units per metre 
	 (32 units per yard). Also, the game seems to round height values down to the nearest 4 units, so this gives a vertical resolution of 5.7cm 
	 (2.25 inches). But when you load your terrain into the game, it seems to scale the height by 2 so that 64 vertical units equals 1 yard again! 
	 Confused? I certainly have been for the past couple of hours! 
	 http://cs.elderscrolls.com/constwiki/index.php/Oblivion_Units */

	/* TES3
	  37: LAND =  1390 (    28,  27374.14,  30243)
	Landscape
	INTV (8 bytes)
		long CellX
		long CellY
			The cell coordinates of the cell.
	DATA (4 bytes)
		long Unknown (default of 0x09)
			Changing this value makes the land 'disappear' in the editor.			
	VNML (12675 bytes)
		struct {
		  signed byte X
		  signed byte Y
		  signed byte Z
		} normals[65][65];
			A RGB color map 65x65 pixels in size representing the land normal vectors.
			The signed value of the 'color' represents the vector's component. Blue
			is vertical (Z), Red the X direction and Green the Y direction. Note that
			the y-direction of the data is from the bottom up.
	VHGT (4232 bytes)
		float Unknown1
			A height offset for the entire cell. Decreasing this value will shift the
			entire cell land down.
		byte Unknown2 (0x00)
		signed byte  HeightData[65][65]
			Contains the height data for the cell in the form of a 65x65 pixel array. The
			height data is not absolute values but uses differences between adjacent pixels.
			Thus a pixel value of 0 means it has the same height as the last pixel. Note that
			the y-direction of the data is from the bottom up.
		short Unknown2 (0x0000)
	WNAM (81 bytes)
		byte Data[9][9]
			Unknown byte data.		
	VCLR (12675 bytes) optional
		Vertex color array, looks like another RBG image 65x65 pixels in size.
	VTEX (512 bytes) optional
		A 16x16 array of short texture indices (from a LTEX record I think).
		*/

	private GeometryInfo gi;//for Bullet later

	public static void setTes3()
	{
		GRID_COUNT = 64;//64 not 32
		LAND_SIZE = GRID_COUNT * TERRIAN_SQUARE_SIZE;//refresh
	}

	/**
	 * Makes the physics version of land
	 */
	public J3dLAND(LAND land)
	{
		super(land, false, false);
		if (land.VHGT != null)
		{
			// extract the heights
			byte[] heightBytes = land.VHGT;
			float[][] heights = extractHeights(heightBytes);

			//now translate the heights into a nice mesh, 82 has been confirmed empirically			
			//Note that 33 by 33 sets of point equals 32 by 32 sets of triangles between them
			TESLANDGen gridGenerator = new TESLANDGen(J3dLAND.LAND_SIZE, J3dLAND.LAND_SIZE, (GRID_COUNT + 1), (GRID_COUNT + 1), heights,
					null, null, null);
			GeometryData terrainData = new GeometryData();
			gridGenerator.generateIndexedTriangleStrips(terrainData);

			Shape3D shape = new Shape3D();
			IndexedTriangleStripArray physicsTriStripArray = new IndexedTriangleStripArray(terrainData.vertexCount,
					GeometryArray.COORDINATES, terrainData.indexesCount, terrainData.stripCounts);
			physicsTriStripArray.setCoordinates(0, terrainData.coordinates);
			physicsTriStripArray.setCoordinateIndices(0, terrainData.indexes);

			gi = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
			gi.setStripCounts(terrainData.stripCounts);
			gi.setCoordinates(terrainData.coordinates);
			gi.setCoordinateIndices(terrainData.indexes);

			//apply them
			shape.setGeometry(physicsTriStripArray);
			shape.setAppearance(new PhysAppearance());
			addNodeChild(shape);
		}
	}

	public GeometryInfo getGeometryInfo()
	{
		return gi;
	}

	/**
	 * makes the visual version of land
	 * @param land
	 * @param master
	 */

	private static TextureAttributes textureAttributesBase = null;

	private static TextureAttributes textureAttributesLayer = null;

	private static TransparencyAttributes taLayer = null;

	private Geometry[] quadrantBaseSubGeoms;

	public J3dLAND(LAND land, IRecordStore master, TextureSource textureSource)
	{
		super(land, false, false);
		int quadrantsPerSide = land.tes3 ? 16 : 2;
		int totalQuadrants = quadrantsPerSide * quadrantsPerSide;

		quadrantBaseSubGeoms = new Geometry[totalQuadrants];
		Group[] quadrantBaseGroups = new Group[totalQuadrants];

		if (land.VHGT != null)
		{
			// extract the heights
			byte[] heightBytes = land.VHGT;
			float[][] heights = extractHeights(heightBytes);

			// extract the normals
			byte[] normalBytes = land.VNML;
			Vector3f[][] normals = extractNormals(normalBytes);

			// extract the colors
			byte[] colorBytes = land.VCLR;
			Color4f[][] colors = extractColors(colorBytes);

			if (textureAttributesBase == null)
			{
				textureAttributesBase = new TextureAttributes();
				textureAttributesBase.setTextureMode(TextureAttributes.MODULATE);
			}

			// make up some base quadrants
			for (int quadrant = 0; quadrant < totalQuadrants; quadrant++)
			{
				//this makes for no see throughs, the other 2 do odd things 
				Group decalGroup = new Group();
				//DecalGroup decalGroup = new DecalGroup();
				//OrderedGroup decalGroup = new OrderedGroup();
				quadrantBaseGroups[quadrant] = decalGroup;
				addNodeChild(decalGroup);

				quadrantBaseSubGeoms[quadrant] = makeQuadrantBaseSubGeom(heights, normals, colors, quadrantsPerSide, quadrant);
			}

			if (!land.tes3)
			{
				// make up some base land texture, pre sorted to btxt by quadrant
				for (int quadrant = 0; quadrant < totalQuadrants; quadrant++)
				{
					Appearance app = createAppearance();

					app.setMaterial(getLandMaterial());
					app.setTextureAttributes(textureAttributesBase);

					app.setTexture(getDefaultTexture(textureSource));
					//oddly btxt are optional
					BTXT btxt = land.BTXTs[quadrant];
					if (btxt != null && btxt.textureFormID != 0)
					{
						app.setTexture(getTexture(btxt.textureFormID, master, textureSource));
					}

					Shape3D baseQuadShape = new Shape3D();
					baseQuadShape.setAppearance(app);

					baseQuadShape.setGeometry(quadrantBaseSubGeoms[quadrant]);
					quadrantBaseGroups[quadrant].addChild(baseQuadShape);
				}
			}
			else
			{
				//It's a 16x16 quadrant system! No transparency or smooth nothing
				//IIRC the textures are not in a 16x16 grid, but in a 4x4 grid in a 4x4 grid.
				for (int a = 0; a < land.VTEXshorts.length; a++)
				{
					short texFormId = land.VTEXshorts[a];
					if (texFormId != 0)
					{
						int lqbx = (a / 16) % 4;
						int lqix = a % 4;

						int lqby = a / (4 * 16);
						int lqiy = (a % 16) / 4;

						//each y little box moves me 4 rows worth(4x16)
						//each y little inner moves by 1 row (16)
						//each x little box moves me across 4
						//each x little inner moves me 1
						int quadrant = (lqby * 4 * 16) + (lqiy * 16) + (lqbx * 4) + lqix;

						Appearance app = createAppearance();
						app.setMaterial(getLandMaterial());
						app.setTextureAttributes(textureAttributesBase);

						app.setTexture(getTextureTes3(texFormId, master, textureSource));

						Shape3D aTxtShape = new Shape3D();
						aTxtShape.setAppearance(app);

						aTxtShape.setGeometry(quadrantBaseSubGeoms[quadrant]);

						quadrantBaseGroups[quadrant].addChild(aTxtShape);
					}
				}

			}

			//If I add transparency attributes to the base grid I see the texture itself has transparency
			// So I need to ignore the textures transparency in all cases
			// and this is how it's done!	
			if (textureAttributesLayer == null)
			{
				textureAttributesLayer = new TextureAttributes();
				textureAttributesLayer.setTextureMode(TextureAttributes.COMBINE);

				textureAttributesLayer.setCombineRgbMode(TextureAttributes.COMBINE_MODULATE);
				textureAttributesLayer.setCombineRgbSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);
				textureAttributesLayer.setCombineRgbSource(1, TextureAttributes.COMBINE_TEXTURE_COLOR);

				textureAttributesLayer.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
				textureAttributesLayer.setCombineAlphaSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);

				taLayer = new TransparencyAttributes();
				taLayer.setTransparencyMode(TransparencyAttributes.BLENDED);

				taLayer.setTransparency(0.0f);
			}

			if (!land.tes3)
			{
				if (land.VTEXids.length > 0)
				{
					//TODO: are VTEXs a bad def that in fact should be only in the older tes3 system?
					System.out.println("***********************VTEXs in LAND");
				}
				else
				{
					//These are per sorted by layer in LAND RECO
					for (int a = 0; a < land.ATXTs.length; a++)
					{
						ATXT atxt = land.ATXTs[a];

						int quadrant = atxt.quadrant;
						Appearance app = createAppearance();
						app.setMaterial(getLandMaterial());
						app.setTextureAttributes(textureAttributesLayer);
						app.setTransparencyAttributes(taLayer);

						if (atxt.textureFormID != 0)
						{
							app.setTexture(getTexture(atxt.textureFormID, master, textureSource));
						}
						else
						{
							app.setTexture(getDefaultTexture(textureSource));
						}

						Shape3D aTxtShape = new Shape3D();
						aTxtShape.setAppearance(app);
						aTxtShape.setGeometry(makeQuadrantLayerSubGeom(heights, normals, colors, quadrantsPerSide, quadrant, atxt.vtxt));
						quadrantBaseGroups[quadrant].addChild(aTxtShape);
					}
				}
			}
			else
			{
				// there is no layer data in morrowind, but a little one grid square fading layer
				// with teh neighbours texture might make them blend better

			}

		}
	}

	protected Appearance createAppearance()
	{
		return new Appearance();
	}

	private static Material landMaterial = null;

	public static Material getLandMaterial()
	{
		if (landMaterial == null)
		{
			landMaterial = new Material();

			landMaterial.setShininess(1.0f); // land is not very shiny, generally
			landMaterial.setDiffuseColor(0.5f, 0.5f, 0.5f);
			landMaterial.setSpecularColor(0.0f, 0.0f, 0.0f);// is the shiny value above not working?
			landMaterial.setColorTarget(Material.AMBIENT_AND_DIFFUSE);//new
		}
		return landMaterial;
	}

	protected static Vector3f quadOffSet(int quadrantsPerSide, int quadrant)
	{
		//Yes it's mad, but get a pen and paper and this is where a quadrant is

		float quadSize = LAND_SIZE / quadrantsPerSide;
		float halfQuadSize = quadSize / 2f;

		int qx = quadrant % quadrantsPerSide;
		int qy = quadrant / quadrantsPerSide;

		float x = ((qx - (quadrantsPerSide / 2)) * quadSize) + halfQuadSize;
		float y = ((qy - (quadrantsPerSide / 2)) * quadSize) + halfQuadSize;
		return new Vector3f(x, 0, -y);
	}

	protected static GeometryArray makeQuadrantBaseSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors,
			int quadrantsPerSide, int quadrant)
	{
		int quadrantSquareCount = (GRID_COUNT / quadrantsPerSide) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrantsPerSide, quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors,
				quadrantTexCoords);

		//Note that 33 by 33 sets of point equals 32 by 32 set of triangles between them
		TESLANDGen gridGenerator = new TESLANDGen(LAND_SIZE / quadrantsPerSide, LAND_SIZE / quadrantsPerSide, quadrantSquareCount,
				quadrantSquareCount, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		gridGenerator.generateIndexedTriangleStrips(terrainData);

		//offset for quadrant
		Vector3f offset = quadOffSet(quadrantsPerSide, quadrant);
		for (int i = 0; i < terrainData.coordinates.length; i += 3)
		{
			terrainData.coordinates[i + 0] += offset.x;
			terrainData.coordinates[i + 1] += offset.y;
			terrainData.coordinates[i + 2] += offset.z;
		}

		IndexedTriangleStripArray itsa = new IndexedTriangleStripArray(terrainData.vertexCount, GeometryArray.COORDINATES
				| GeometryArray.NORMALS | GeometryArray.COLOR_4 | GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.USE_COORD_INDEX_ONLY,
				terrainData.indexesCount, terrainData.stripCounts);
		itsa.setCoordinates(0, terrainData.coordinates);
		itsa.setCoordinateIndices(0, terrainData.indexes);
		itsa.setNormals(0, terrainData.normals);
		itsa.setColors(0, terrainData.colors);
		itsa.setTextureCoordinates(0, 0, terrainData.textureCoordinates);

		return itsa;

	}

	private static GeometryArray makeQuadrantLayerSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors,
			int quadrantsPerSide, int quadrant, VTXT vtxt)
	{
		int quadrantSquareCount = (GRID_COUNT / quadrantsPerSide) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrantsPerSide, quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		if (vtxt != null)
		{
			// reset everything to transparent
			for (int row = 0; row < quadrantSquareCount; row++)
			{
				for (int col = 0; col < quadrantSquareCount; col++)
				{
					quadrantColors[row][col].w = 0.0f;
				}
			}

			for (int v = 0; v < vtxt.count; v++)
			{
				int rowno = (GRID_COUNT / quadrantsPerSide) - (vtxt.position[v] / quadrantSquareCount);
				int colno = (vtxt.position[v] % quadrantSquareCount);

				quadrantColors[rowno][colno].w = vtxt.opacity[v];
			}
		}

		TESLANDGen gridGenerator = new TESLANDGen(LAND_SIZE / quadrantsPerSide, LAND_SIZE / quadrantsPerSide, quadrantSquareCount, quadrantSquareCount,
				quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		gridGenerator.generateIndexedTriangleStrips(terrainData);

		//offset for quadrant
		Vector3f offset = quadOffSet(quadrantsPerSide, quadrant);
		for (int i = 0; i < terrainData.coordinates.length; i += 3)
		{
			terrainData.coordinates[i + 0] += offset.x;
			terrainData.coordinates[i + 1] += offset.y;
			terrainData.coordinates[i + 2] += offset.z;
		}

		IndexedTriangleStripArray itsa = new IndexedTriangleStripArray(terrainData.vertexCount, GeometryArray.COORDINATES
				| GeometryArray.NORMALS | GeometryArray.COLOR_4 | GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.USE_COORD_INDEX_ONLY,
				terrainData.indexesCount, terrainData.stripCounts);
		itsa.setCoordinates(0, terrainData.coordinates);
		itsa.setCoordinateIndices(0, terrainData.indexes);
		itsa.setNormals(0, terrainData.normals);
		itsa.setColors(0, terrainData.colors);
		itsa.setTextureCoordinates(0, 0, terrainData.textureCoordinates);

		return itsa;

	}

	/**Note colors might have the alpha adjusted so they are copies not references
	 * 
	 * @param quadrant Specifies the quadrant this BTXT record applies to. 0 = bottom left. 1 = bottom right. 2 = upper-left. 3 = upper-right.
	 * @param quadrant2 
	 * @param baseHeights 33x33 array of all 4 quads
	 * @param baseNormals 33x33 array of all 4 quads
	 * @param baseColors  33x33 array of all 4 quads
	 * @param quadrantHeights 17x17 array to be filled
	 * @param quadrantNormals 17x17 array to be filled
	 * @param quadrantColors  17x17 array to be filled
	 */
	private static void makeQuadrantData(int quadrantsPerSide, int quadrant, float[][] baseHeights, Vector3f[][] baseNormals,
			Color4f[][] baseColors, float[][] quadrantHeights, Vector3f[][] quadrantNormals, Color4f[][] quadrantColors,
			TexCoord2f[][] quadrantTexCoords)
	{
		//trust me on this madness
		int qx = quadrant % quadrantsPerSide;
		int qy = quadrant / quadrantsPerSide;

		int quadrant_grid_count = (GRID_COUNT / quadrantsPerSide);

		for (int row = 0; row < quadrant_grid_count + 1; row++)
		{
			for (int col = 0; col < quadrant_grid_count + 1; col++)
			{
				int baseRow = row + (((quadrantsPerSide - 1) - qy) * quadrant_grid_count);
				int baseCol = col + ((qx) * quadrant_grid_count);
				quadrantHeights[row][col] = baseHeights[baseRow][baseCol];
				quadrantNormals[row][col] = baseNormals[baseRow][baseCol];
				quadrantColors[row][col] = new Color4f(baseColors[baseRow][baseCol]);//copy to allow modification
				quadrantTexCoords[row][col] = new TexCoord2f((row / 2f), (col / 2f));
				// this /2f above simply suggests how many times to repeat the texture over a grid
			}
		}
	}

	private Texture getTextureTes3(int textureID, IRecordStore master, TextureSource textureSource)
	{
		Record ltexRec = master.getRecord("LTEX_" + textureID);
		if (ltexRec != null)
		{
			if (ltexRec.getRecordType().equals("LTEX"))
			{
				LTEX ltex = new LTEX(ltexRec);
				if (ltex.ICON != null)
				{
					return J3dNiGeometry.loadTexture(ltex.ICON.str, textureSource);
				}
			}
			else
			{
				System.out.println("Tes3 Bad textureFormID " + textureID + " type is not LTEX but " + ltexRec.getRecordType());
			}
		}

		return getDefaultTexture(textureSource);
	}

	private Texture getTexture(int textureFormID, IRecordStore master, TextureSource textureSource)
	{
		Record ltexRec = master.getRecord(textureFormID);
		if (ltexRec.getRecordType().equals("LTEX"))
		{
			LTEX ltex = new LTEX(ltexRec);
			int texSetId = ltex.textureSetId;
			//obliv uses simpler system
			if (texSetId != -1)
			{
				Record texSetRec = master.getRecord(texSetId);
				TXST textureSet = new TXST(texSetRec);
				return textureSource.getTexture(textureSet.TX00.str);
			}
			else if (ltex.ICON != null)
			{
				return textureSource.getTexture("Landscape\\" + ltex.ICON.str);
			}
		}
		else
		{
			System.out.println("Bad textureFormID " + textureFormID + " type is not LTEX but " + ltexRec.getRecordType());
		}

		return getDefaultTexture(textureSource);

	}

	private static Texture defaultTex = null;

	protected static Texture getDefaultTexture(TextureSource textureSource)
	{
		//Skyrim //textures\\landscape\\dirt01.dds
		//FO3 //textures\\landscape\\dirt01.dds
		//Obliv //textures\\landscape\\default.dds
		if (defaultTex == null)
		{
			if (textureSource.textureFileExists("Landscape\\dirt01.dds"))
			{
				defaultTex = textureSource.getTexture("Landscape\\dirt01.dds");
			}
			else if (textureSource.textureFileExists("Landscape\\default.dds"))
			{
				defaultTex = textureSource.getTexture("Landscape\\default.dds");
			}
			else if (textureSource.textureFileExists("tx_ac_dirt_01.dds"))
			{
				defaultTex = textureSource.getTexture("tx_ac_dirt_01.dds");
			}
			else
			{
				System.out.println("BUM, no default LAND texture found somehow?");
			}
		}
		return defaultTex;
	}

	protected static float[][] extractHeights(byte[] heightBytes)
	{
		// extract the heights
		float[][] heights = new float[(GRID_COUNT + 1)][(GRID_COUNT + 1)];

		float startHeightOffset = ESMByteConvert.extractFloat(heightBytes, 0);

		float startRowHeight = (startHeightOffset * 4);
		for (int row = 0; row < (GRID_COUNT + 1); row++)
		{
			float height = startRowHeight;
			for (int col = 0; col < (GRID_COUNT + 1); col++)
			{
				int idx = col + (row * (GRID_COUNT + 1)) + 4;//+4 is start float
				height += heightBytes[idx] * 4;

				// start next row relative to the start of this row
				if (col == 0)
					startRowHeight = height;

				// note reverse order, due to x,y,z => x,z,-y
				heights[GRID_COUNT - row][col] = (height * HEIGHT_TO_J3D_SCALE);
			}
		}

		//last 3 bytes, what are they?
		// Unknown. Haven't noticed any ill-effects just filling this with arbitrary values in TES3 or TES4. 
		// This is probably just a 3-byte filler so that the entire subrecord's data can be aligned on a 4 byte word boundary.

		return heights;

	}

	protected static Vector3f[][] extractNormals(byte[] normalBytes)
	{
		Vector3f[][] normals = new Vector3f[(GRID_COUNT + 1)][(GRID_COUNT + 1)];
		for (int row = 0; row < (GRID_COUNT + 1); row++)
		{
			for (int col = 0; col < (GRID_COUNT + 1); col++)
			{
				byte x = normalBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 0];
				byte y = normalBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 1];
				byte z = normalBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 2];

				Vector3f v = new Vector3f(x & 0xff, z & 0xff, -y & 0xff);
				v.normalize();
				// note reverse order, due to x,y,z => x,z,-y
				normals[GRID_COUNT - row][col] = v;
			}
		}
		return normals;
	}

	protected static Color4f[][] extractColors(byte[] colorBytes)
	{

		Color4f[][] colors = new Color4f[(GRID_COUNT + 1)][(GRID_COUNT + 1)];

		for (int row = 0; row < (GRID_COUNT + 1); row++)
		{
			for (int col = 0; col < (GRID_COUNT + 1); col++)
			{
				if (colorBytes != null)
				{
					float r = (colorBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 0] & 0xff) / 255f;
					float g = (colorBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 1] & 0xff) / 255f;
					float b = (colorBytes[(col + (row * (GRID_COUNT + 1))) * 3 + 2] & 0xff) / 255f;
					Color4f c = new Color4f(r, g, b, 1.0f);//note hard coded opaque

					// note reverse order, due to x,y,z => x,z,-y
					colors[GRID_COUNT - row][col] = c;
				}
				else
				{
					// no colors let's try white
					colors[GRID_COUNT - row][col] = new Color4f(1.0f, 1.0f, 1.0f, 1.0f);
				}

			}
		}

		return colors;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
