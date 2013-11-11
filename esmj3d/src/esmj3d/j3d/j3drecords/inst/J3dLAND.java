package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.Appearance;
import javax.media.j3d.DecalGroup;
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

import org.j3d.geom.GeometryData;

import tools.io.ESMByteConvert;
import utils.ESConfig;
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
	//	NOTE nif x,y,z to j3d x,z,-y
	public static float HEIGHT_TO_J3D_SCALE = 0.04f; //where does this come from? 1/25th?

	public static float LAND_SIZE = ESConfig.TERRIAN_GRID_SQUARE_COUNT * ESConfig.TERRIAN_SQUARE_SIZE; //= (32*2.56) = 81.92

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

	private GeometryInfo gi;//for Bullet later

	/**
	 * Makes the physics version of land
	 */
	public J3dLAND(LAND land)
	{
		super(land, false);
		if (land.VHGT != null)
		{
			// extract the heights
			byte[] heightBytes = land.VHGT;
			float[][] heights = extractHeights(heightBytes);

			//now translate the heights into a nice mesh, 82 has been confirmed empirically			
			//Note that 33 by 33 sets of point equals 32 by 32 sets of triangles between them
			TESLANDGen gridGenerator = new TESLANDGen(J3dLAND.LAND_SIZE, J3dLAND.LAND_SIZE, 33, 33, heights, null, null, null);
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
	public J3dLAND(LAND land, IRecordStore master, TextureSource textureSource)
	{
		super(land, false);
		Group[] quadrantBaseGroups = new Group[4];

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

			TextureAttributes textureAttributesBase = new TextureAttributes();
			textureAttributesBase.setTextureMode(TextureAttributes.MODULATE);

			// make up some base land texture, pre sorted to btxt by quadrant
			for (int quadrant = 0; quadrant < 4; quadrant++)
			{
				//TODO: this makes for no see throughs and I think I'm not losing textures?
				Group decalGroup = new Group();
				//DecalGroup decalGroup = new DecalGroup();
				//OrderedGroup decalGroup = new OrderedGroup();
				quadrantBaseGroups[quadrant] = decalGroup;
				addNodeChild(decalGroup);

				Appearance app = new Appearance();

				app.setMaterial(getLandMaterial());
				app.setTextureAttributes(textureAttributesBase);

				//oddly btxt are optional
				BTXT btxt = land.BTXTs[quadrant];
				if (btxt != null && btxt.textureFormID != 0)
				{
					app.setTexture(getTexture(btxt.textureFormID, master, textureSource));
				}
				else
				{
					app.setTexture(getDefaultTexture(textureSource));
				}

				Shape3D baseQuadShape = new Shape3D();
				baseQuadShape.setAppearance(app);
				baseQuadShape.setGeometry(makeQuadrantBaseSubGeom(heights, normals, colors, quadrant));
				quadrantBaseGroups[quadrant].addChild(baseQuadShape);

			}

			//If I add transparency attirbute to teh base grid I see the texture itself has transparency
			// So I need to ignore the textures transparency in all cases
			// and this is how it's done!			
			TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode(TextureAttributes.COMBINE);

			textureAttributes.setCombineRgbMode(TextureAttributes.COMBINE_MODULATE);
			textureAttributes.setCombineRgbSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);
			textureAttributes.setCombineRgbSource(1, TextureAttributes.COMBINE_TEXTURE_COLOR);

			textureAttributes.setCombineAlphaMode(TextureAttributes.COMBINE_REPLACE);
			textureAttributes.setCombineAlphaSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);

			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparencyMode(TransparencyAttributes.BLENDED);

			ta.setTransparency(0.0f);

			//These are per sorted by layer in LAND RECO
			for (int a = 0; a < land.ATXTs.length; a++)
			{
				ATXT atxt = land.ATXTs[a];

				int quadrant = atxt.quadrant;
				Appearance app = new Appearance();
				app.setMaterial(getLandMaterial());
				app.setTextureAttributes(textureAttributes);

				app.setTransparencyAttributes(ta);

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
				aTxtShape.setGeometry(makeQuadrantLayerSubGeom(heights, normals, colors, quadrant, atxt.vtxt));
				quadrantBaseGroups[quadrant].addChild(aTxtShape);
			}
		}
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

	private static Vector3f quadOffSet(int quadrant)
	{
		float pos = LAND_SIZE / 4f;
		if (quadrant == 0)
			return new Vector3f(-pos, 0, pos);
		else if (quadrant == 1)
			return new Vector3f(pos, 0, pos);
		else if (quadrant == 2)
			return new Vector3f(-pos, 0, -pos);
		else if (quadrant == 3)
			return new Vector3f(pos, 0, -pos);
		return null;
	}

	private static GeometryArray makeQuadrantBaseSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors, int quadrant)
	{
		int quadrantSquareCount = (ESConfig.TERRIAN_GRID_SQUARE_COUNT / 2) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		//Note that 33 by 33 sets of point equals 32 by 32 set of triangles between them
		TESLANDGen gridGenerator = new TESLANDGen(LAND_SIZE / 2f, LAND_SIZE / 2f, quadrantSquareCount, quadrantSquareCount,
				quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		gridGenerator.generateIndexedTriangleStrips(terrainData);

		//offset for quadrant
		Vector3f offset = quadOffSet(quadrant);
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

	private static GeometryArray makeQuadrantLayerSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors, int quadrant,
			VTXT vtxt)
	{
		int quadrantSquareCount = (ESConfig.TERRIAN_GRID_SQUARE_COUNT / 2) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

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
			int rowno = 16 - (vtxt.position[v] / quadrantSquareCount);
			int colno = (vtxt.position[v] % quadrantSquareCount);

			quadrantColors[rowno][colno].w = vtxt.opacity[v];
		}

		TESLANDGen gridGenerator = new TESLANDGen(LAND_SIZE / 2f, LAND_SIZE / 2f, quadrantSquareCount, quadrantSquareCount,
				quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		gridGenerator.generateIndexedTriangleStrips(terrainData);

		//offset for quadrant
		Vector3f offset = quadOffSet(quadrant);
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
	 * @param baseHeights 33x33 array of all 4 quads
	 * @param baseNormals 33x33 array of all 4 quads
	 * @param baseColors  33x33 array of all 4 quads
	 * @param quadrantHeights 17x17 array to be filled
	 * @param quadrantNormals 17x17 array to be filled
	 * @param quadrantColors  17x17 array to be filled
	 */
	private static void makeQuadrantData(int quadrant, float[][] baseHeights, Vector3f[][] baseNormals, Color4f[][] baseColors,
			float[][] quadrantHeights, Vector3f[][] quadrantNormals, Color4f[][] quadrantColors, TexCoord2f[][] quadrantTexCoords)
	{
		for (int row = 0; row < 17; row++)
		{
			for (int col = 0; col < 17; col++)
			{
				int baseRow = row + ((quadrant == 0 || quadrant == 1) ? 16 : 0);
				int baseCol = col + ((quadrant == 1 || quadrant == 3) ? 16 : 0);
				quadrantHeights[row][col] = baseHeights[baseRow][baseCol];
				quadrantNormals[row][col] = baseNormals[baseRow][baseCol];
				quadrantColors[row][col] = new Color4f(baseColors[baseRow][baseCol]);//copy to allow modification
				quadrantTexCoords[row][col] = new TexCoord2f((row / 2f), (col / 2f));
			}
		}
	}

	private Texture getTexture(int textureFormID, IRecordStore master, TextureSource textureSource)
	{
		Record ltexRec = master.getRecord(textureFormID);
		LTEX ltex = new LTEX(ltexRec);
		int texSetId = ltex.textureSetId;
		//obliv uses simpler system
		if (texSetId != -1)
		{
			Record texSetRec = master.getRecord(texSetId);
			TXST textureSet = new TXST(texSetRec);
			return textureSource.getTexture(textureSet.TX00.str);
		}
		else
		{
			return textureSource.getTexture("Landscape\\" + ltex.ICON.str);
		}
	}

	private static Texture defaultTex = null;

	private static Texture getDefaultTexture(TextureSource textureSource)
	{
		//Skyrim //textures\\landscape\\dirt02.dds
		//FO3 //textures\\landscape\\dirt01.dds
		//Obliv //textures\\landscape\\terraingcgrass01.dds
		if (defaultTex == null)
		{
			if (textureSource.textureFileExists("Landscape\\dirt02.dds"))
			{
				defaultTex = textureSource.getTexture("Landscape\\dirt02.dds");
			}
			else if (textureSource.textureFileExists("Landscape\\dirt01.dds"))
			{
				defaultTex = textureSource.getTexture("Landscape\\dirt01.dds");
			}
			else if (textureSource.textureFileExists("Landscape\\terraingcgrass01.dds"))
			{
				defaultTex = textureSource.getTexture("Landscape\\terraingcgrass01.dds");
			}
			else
			{
				System.out.println("BUM, no default LAND texture found somehow?");
			}
		}
		return defaultTex;
	}

	private static float[][] extractHeights(byte[] heightBytes)
	{
		// extract the heights
		float[][] heights = new float[33][33];

		float startHeightOffset = ESMByteConvert.extractFloat(heightBytes, 0);

		float startRowHeight = (startHeightOffset * 4);
		for (int row = 0; row < 33; row++)
		{
			float height = startRowHeight;
			for (int col = 0; col < 33; col++)
			{
				int idx = col + (row * 33) + 4;
				height += heightBytes[idx] * 4;

				// start next row relative to the start of this row
				if (col == 0)
					startRowHeight = height;

				// note reverse order, due to x,y,z => x,z,-y
				heights[32 - row][col] = (height * J3dLAND.HEIGHT_TO_J3D_SCALE);
			}
		}

		//last 3 bytes, what are they?
		// Unknown. Haven't noticed any ill-effects just filling this with arbitrary values in TES3 or TES4. 
		// This is probably just a 3-byte filler so that the entire subrecord's data can be aligned on a 4 byte word boundary.

		return heights;

	}

	private static Vector3f[][] extractNormals(byte[] normalBytes)
	{
		Vector3f[][] normals = new Vector3f[33][33];
		for (int row = 0; row < 33; row++)
		{
			for (int col = 0; col < 33; col++)
			{
				byte x = normalBytes[(col + (row * 33)) * 3 + 0];
				byte y = normalBytes[(col + (row * 33)) * 3 + 1];
				byte z = normalBytes[(col + (row * 33)) * 3 + 2];

				Vector3f v = new Vector3f(x & 0xff, z & 0xff, -y & 0xff);
				v.normalize();
				// note reverse order, due to x,y,z => x,z,-y
				normals[32 - row][col] = v;
			}
		}
		return normals;
	}

	private static Color4f[][] extractColors(byte[] colorBytes)
	{

		Color4f[][] colors = new Color4f[33][33];

		for (int row = 0; row < 33; row++)
		{
			for (int col = 0; col < 33; col++)
			{
				if (colorBytes != null)
				{
					float r = (colorBytes[(col + (row * 33)) * 3 + 0] & 0xff) / 255f;
					float g = (colorBytes[(col + (row * 33)) * 3 + 1] & 0xff) / 255f;
					float b = (colorBytes[(col + (row * 33)) * 3 + 2] & 0xff) / 255f;
					Color4f c = new Color4f(r, g, b, 1.0f);//note hard coded opaque

					// note reverse order, due to x,y,z => x,z,-y
					colors[32 - row][col] = c;
				}
				else
				{
					// no colors let's try white
					colors[32 - row][col] = new Color4f(1.0f, 1.0f, 1.0f, 1.0f);
				}

			}
		}

		return colors;
	}

}
