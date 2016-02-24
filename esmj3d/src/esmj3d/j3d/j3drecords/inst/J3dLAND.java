package esmj3d.j3d.j3drecords.inst;

import java.io.IOException;
import java.util.ArrayList;

import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.Material;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.vecmath.Color4f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;

import com.sun.j3d.utils.geometry.GeometryInfo;

import esmj3d.data.shared.records.LAND;
import esmj3d.data.shared.records.LAND.ATXT;
import esmj3d.data.shared.records.LAND.BTXT;
import esmj3d.data.shared.records.LAND.VTXT;
import esmj3d.data.shared.records.LTEX;
import esmj3d.data.shared.records.TXST;
import esmj3d.j3d.TESLANDGen;
import esmmanager.common.data.record.IRecordStore;
import esmmanager.common.data.record.Record;
import nif.BgsmSource;
import nif.j3d.J3dNiGeometry;
import nif.j3d.J3dNiTriBasedGeom;
import nif.niobject.bgsm.BSMaterial;
import tools.io.ESMByteConvert;
import tools3d.utils.PhysAppearance;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.Utils3D;
import utils.source.TextureSource;

public class J3dLAND extends J3dRECOStatInst
{
	public static int GRID_COUNT = 32;

	public static final float TERRIAN_SQUARE_SIZE = 2.56f;// confirmed empirically

	private static final boolean OUTPUT_BINDINGS = false;

	public static float TEX_REPEAT = 0.5f;// suggests how many times to repeat the texture over a grid square

	//	NOTE nif x,y,z to j3d x,z,-y
	public static float HEIGHT_TO_J3D_SCALE = 0.04f; //where does this come from? 1/25th?

	public static float LAND_SIZE = GRID_COUNT * TERRIAN_SQUARE_SIZE; //= (32*2.56) = 81.92

	public static boolean BY_REF = true;

	public static boolean BUFFERS = true;

	public static boolean INTERLEAVE = false;

	public static boolean STRIPIFY = true;

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

	public static void setTes3()
	{
		GRID_COUNT = 64;//64 not 32
		LAND_SIZE = GRID_COUNT * TERRIAN_SQUARE_SIZE;//refresh
		TEX_REPEAT = 0.25f;
		INTERLEAVE = false;
		STRIPIFY = false;
		BY_REF = true;
		BUFFERS = true;
	}

	private GeometryInfo gi;//for Bullet later

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
					GeometryArray.COORDINATES | GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE
							| GeometryArray.BY_REFERENCE_INDICES | GeometryArray.USE_COORD_INDEX_ONLY,
					terrainData.indexesCount, terrainData.stripCounts);
			physicsTriStripArray.setCoordRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(terrainData.coordinates)));
			physicsTriStripArray.setCoordIndicesRef(terrainData.indexes);

			gi = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
			gi.setStripCounts(terrainData.stripCounts);
			gi.setCoordinates(terrainData.coordinates);
			gi.setCoordinateIndices(terrainData.indexes);

			//apply them
			physicsTriStripArray.setName("LAND phys geo");
			shape.setGeometry(physicsTriStripArray);
			shape.setAppearance(PhysAppearance.makeAppearance());
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

	//Notice none of the below are static, I don't want too much sharing of appearance parts

	private float lowestHeight = Float.MAX_VALUE;

	private static ShaderProgram shaderProgram = null;

	public J3dLAND(LAND land, IRecordStore master, TextureSource textureSource)
	{
		super(land, false, false);
		int quadrantsPerSide = land.tes3 ? 16 : 2;
		int totalQuadrants = quadrantsPerSide * quadrantsPerSide;
		int quadrantSquareCount = (GRID_COUNT / quadrantsPerSide) + 1;

		Group baseGroup = new Group();
		addNodeChild(baseGroup);

		//ensure shader ready
		createShaderProgram();

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

			// get the atxts
			ATXT[] atxts;
			if (land.tes3)
			{
				atxts = createTes3ATXT(land.VTEXshorts);
			}
			else
			{
				atxts = land.ATXTs;
			}

			for (int quadrant = 0; quadrant < totalQuadrants; quadrant++)
			{

				ShaderAppearance app = new ShaderAppearance();

				app.setMaterial(createMat());
				app.setRenderingAttributes(createRA());

				//TODO: LAND vertex attributes proper
				// ok so the texcoord stuff is bullshit, must use 2 vertex attributes with  4-floats of data
				// but it's gonna require a lot of stuffing around!

				/*String[] attribNames = new String[8];
				for (int i = 0; i < 8; i++)
				{
					attribNames[i] = "attribLayer" + i;
					if (OUTPUT_BINDINGS)
						System.out.println("set attribute name " + i + " to " + attribNames[i]);
				}
				shaderProgram.setVertexAttrNames(attribNames);*/

				ArrayList<ShaderAttributeValue> allShaderAttributeValues = new ArrayList<ShaderAttributeValue>();
				ArrayList<TextureUnitState> allTextureUnitStates = new ArrayList<TextureUnitState>();

				// need texcoord count up front for constructor
				int texCoordCount = 1;
				for (int a = 0; a < atxts.length; a++)
				{
					ATXT atxt = atxts[a];
					//TODO: I've seen layer == 8 which is too many
					if (atxt.quadrant == quadrant && atxt.layer < 8 && atxt.vtxt != null)
					{
						texCoordCount++;
					}
				}
				// Notice -1 as the tex coords includes the baseMap coord (the real coords)
				allShaderAttributeValues.add(new ShaderAttributeValue("layerCount", new Integer(texCoordCount - 1)));

				TextureUnitState tus = null;

				if (!land.tes3)
				{
					//oddly btxt are optional
					BTXT btxt = land.BTXTs[quadrant];

					if (btxt != null)
					{
						tus = getTexture(btxt.textureFormID, master, textureSource);
					}
					else
					{
						tus = getDefaultTexture(textureSource);
					}
				}
				else
				{
					if (land.VTEXshorts != null)
					{
						int texFormId = land.VTEXshorts[quadrant];
						tus = getTextureTes3(texFormId, master, textureSource);
					}
				}
				allTextureUnitStates.add(tus);
				allShaderAttributeValues.add(new ShaderAttributeValue("baseMap", new Integer(0)));

				Shape3D baseQuadShape = new Shape3D();
				baseQuadShape.setAppearance(app);
				GeometryArray ga = makeQuadrantBaseSubGeom(heights, normals, colors, quadrantsPerSide, quadrant, texCoordCount, 0, null);
				ga.setName(land.toString() + ":LAND " + quadrant + " " + land.landX + " " + land.landY);
				//System.out.println(""+ quadrant + " "  + land.landX + " " + land.landY);
				baseQuadShape.setGeometry(ga);

				//These are per sorted by layer in LAND RECO
				for (int a = 0; a < atxts.length; a++)
				{
					ATXT atxt = atxts[a];

					//TODO: I've seen layer ==8 which is too many
					if (atxt.quadrant == quadrant && atxt.layer < 8)
					{
						// now build up the vertex attribute float arrays to hand to the geometry	
						VTXT vtxt = atxt.vtxt;
						if (vtxt != null)
						{
							if (land.tes3)
							{
								tus = getTextureTes3(atxt.textureFormID, master, textureSource);
							}
							else
							{
								tus = getTexture(atxt.textureFormID, master, textureSource);
							}
							allTextureUnitStates.add(tus);
							//Notice +2 as space for base and size is one more than final index, these are in order so there should be no spaces
							if (allTextureUnitStates.size() != atxt.layer + 2)
								System.err.println("allTextureUnitStates.size()!= atxt.layer + 2 " + allTextureUnitStates.size() + " != "
										+ (atxt.layer + 2));

							allShaderAttributeValues
									.add(new ShaderAttributeValue("layerMap" + (atxt.layer + 1), new Integer(atxt.layer + 1)));

							float[][] quadrantColors = new float[quadrantSquareCount][quadrantSquareCount];

							for (int v = 0; v < vtxt.count; v++)
							{
								int rowno = (GRID_COUNT / quadrantsPerSide) - (vtxt.position[v] / quadrantSquareCount);
								int colno = (vtxt.position[v] % quadrantSquareCount);

								quadrantColors[rowno][colno] = vtxt.opacity[v];
							}

							float[] opacities = new float[(quadrantSquareCount * quadrantSquareCount) * 2];
							int i = 0;
							for (int row = 0; row < quadrantSquareCount; row++)
							{
								for (int col = 0; col < quadrantSquareCount; col++)
								{
									opacities[i++] = quadrantColors[row][col];
									opacities[i++] = 0;// texcoord v unused
								}
							}

							ga.setTexCoordRefBuffer(atxt.layer + 1, new J3DBuffer(Utils3D.makeFloatBuffer(opacities)));
						}
					}
				}

				TextureUnitState[] tusa = new TextureUnitState[allTextureUnitStates.size()];
				for (int i = 0; i < allTextureUnitStates.size(); i++)
				{
					tusa[i] = allTextureUnitStates.get(i);
					if (OUTPUT_BINDINGS)
						System.out.println("LAND Tus " + i + " " + tusa[i]);
				}
				app.setTextureUnitState(tusa);

				app.setShaderProgram(shaderProgram);

				ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();
				for (ShaderAttributeValue sav : allShaderAttributeValues)
				{
					if (OUTPUT_BINDINGS)
						System.out.println(sav.getAttributeName() + " " + sav.getValue());

					shaderAttributeSet.put(sav);
				}
				app.setShaderAttributeSet(shaderAttributeSet);

				baseGroup.addChild(baseQuadShape);

			}

		}
	}

	private static Material mat;

	public static Material createMat()
	{
		if (mat == null)
		{
			mat = new Material();
			mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
			mat.setShininess(1.0f);
			mat.setDiffuseColor(1f, 1f, 1f);
			mat.setSpecularColor(1f, 1f, 1f);
		}
		return mat;
	}

	private static RenderingAttributes ra;

	public static RenderingAttributes createRA()
	{
		if (ra == null)
		{
			ra = new RenderingAttributes();
		}
		return ra;
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
			int quadrantsPerSide, int quadrant, int texCoordCount, int vertexAttrCount, int[] vertexAttrSizes)
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
		if (STRIPIFY)
			gridGenerator.generateIndexedTriangleStrips(terrainData);
		else
			gridGenerator.generateIndexedTriangles(terrainData);

		//offset for quadrant
		Vector3f offset = quadOffSet(quadrantsPerSide, quadrant);
		for (int i = 0; i < terrainData.coordinates.length; i += 3)
		{
			terrainData.coordinates[i + 0] += offset.x;
			terrainData.coordinates[i + 1] += offset.y;
			terrainData.coordinates[i + 2] += offset.z;
		}

		return createGA(terrainData, texCoordCount, vertexAttrCount, vertexAttrSizes);

	}

	/**
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
				quadrantColors[row][col] = baseColors[baseRow][baseCol];
				quadrantTexCoords[row][col] = new TexCoord2f((row * TEX_REPEAT), (col * TEX_REPEAT));
			}
		}
	}

	public static TextureUnitState getTextureTes3(int textureID, IRecordStore master, TextureSource textureSource)
	{
		//0 means default?
		if (textureID > 0)
		{
			//not sure why -1 has correct texture but it sure does see openMW
			Record ltexRec = master.getRecord("LTEX_" + (textureID - 1));
			if (ltexRec != null)
			{
				if (ltexRec.getRecordType().equals("LTEX"))
				{
					LTEX ltex = new LTEX(ltexRec);
					if (ltex.ICON != null)
					{
						Texture texture = J3dNiGeometry.loadTexture(ltex.ICON.str, textureSource);
						if (texture != null)
						{
							TextureUnitState tus = new TextureUnitState();
							tus.setTexture(texture);
							tus.setName(ltex.ICON.str);
							return tus;
						}
					}
				}
				else
				{
					System.out.println("Tes3 Bad textureFormID " + textureID + " type is not LTEX but " + ltexRec.getRecordType());
				}
			}
		}

		return getDefaultTexture(textureSource);
	}

	public static TextureUnitState getTexture(int textureFormID, IRecordStore master, TextureSource textureSource)
	{
		if (textureFormID > 0)
		{
			Record ltexRec = master.getRecord(textureFormID);
			if (ltexRec.getRecordType().equals("LTEX"))
			{
				TextureUnitState tus = new TextureUnitState();
				LTEX ltex = new LTEX(ltexRec);
				int texSetId = ltex.textureSetId;

				if (texSetId != -1)
				{
					Record texSetRec = master.getRecord(texSetId);
					TXST textureSet = new TXST(texSetRec);
					if (textureSet.TX00 != null)
					{
						tus.setTexture(textureSource.getTexture(textureSet.TX00.str));
						tus.setName(textureSet.TX00.str);
					}
					else if (textureSet.MNAM != null)
					{
						// new fallout 4 texture system
						try
						{
							BSMaterial material = BgsmSource.getMaterial("Materials\\" + textureSet.MNAM.str);
							if (material != null)
							{
								tus.setTexture(textureSource.getTexture(material.textureList.get(0)));
								tus.setName(material.textureList.get(0));
							}

						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if (ltex.ICON != null)
				{
					//obliv uses simpler system					
					tus.setTexture(textureSource.getTexture("Landscape\\" + ltex.ICON.str));
					tus.setName("Landscape\\" + ltex.ICON.str);
				}
				return tus;
			}
			else
			{
				System.out.println("Bad textureFormID " + textureFormID + " type is not LTEX but " + ltexRec.getRecordType());
			}

		}
		return getDefaultTexture(textureSource);

	}

	private static TextureUnitState defaultTex = null;

	protected static TextureUnitState getDefaultTexture(TextureSource textureSource)
	{
		//Skyrim //textures\\landscape\\dirt01.dds
		//FO3 //textures\\landscape\\dirt01.dds
		//Obliv //textures\\landscape\\default.dds
		if (defaultTex == null)
		{
			defaultTex = new TextureUnitState();
			if (textureSource.textureFileExists("Landscape\\dirt01.dds"))
			{
				defaultTex.setTexture(textureSource.getTexture("Landscape\\dirt01.dds"));
				defaultTex.setName("Landscape\\dirt01.dds");
			}
			else if (textureSource.textureFileExists("Landscape\\default.dds"))
			{
				defaultTex.setTexture(textureSource.getTexture("Landscape\\default.dds"));
				defaultTex.setName("Landscape\\default.dds");
			}
			else if (textureSource.textureFileExists("_land_default.dds"))
			{
				defaultTex.setTexture(textureSource.getTexture("_land_default.dds"));
				defaultTex.setName("_land_default.dds");
			}
			else if (textureSource.textureFileExists("Landscape\\Ground\\BlastedForestDirt01_d.DDS"))
			{
				defaultTex.setTexture(textureSource.getTexture("Landscape\\Ground\\BlastedForestDirt01_d.DDS"));
				defaultTex.setName("Landscape\\Ground\\BlastedForestDirt01_d.DDS");
			}
			else
			{
				System.out.println("BUM, no default LAND texture found somehow?");
			}
		}
		return defaultTex;
	}

	private float[][] extractHeights(byte[] heightBytes)
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
				float h = (height * HEIGHT_TO_J3D_SCALE);
				heights[GRID_COUNT - row][col] = h;

				//update lowest
				lowestHeight = h < lowestHeight ? h : lowestHeight;
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

	public static GeometryArray createGA(GeometryData terrainData, int texCoordCount, int vertexAttrCount, int[] vertexAttrSizes)
	{
		int basicFormat = GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR_4 //
				| GeometryArray.TEXTURE_COORDINATE_2 //
				| GeometryArray.USE_COORD_INDEX_ONLY //
				| (BY_REF || STRIPIFY ? (GeometryArray.BY_REFERENCE_INDICES | GeometryArray.BY_REFERENCE) : 0)//
				| (BUFFERS ? GeometryArray.USE_NIO_BUFFER : 0);

		int[] texMap = new int[texCoordCount];
		for (int i = 0; i < texCoordCount; i++)
			texMap[i] = i;

		IndexedGeometryArray iga;
		if (INTERLEAVE)
		{
			if (STRIPIFY)
			{
				iga = new IndexedTriangleStripArray(terrainData.vertexCount, basicFormat | GeometryArray.INTERLEAVED, //
						texCoordCount, texMap, // vertexAttrCount, vertexAttrSizes, //
						terrainData.indexesCount, terrainData.stripCounts);
			}
			else
			{
				iga = new IndexedTriangleArray(terrainData.vertexCount, basicFormat | GeometryArray.INTERLEAVED, //
						texCoordCount, texMap, // vertexAttrCount, vertexAttrSizes, //
						terrainData.indexesCount);
			}
			iga.setCoordIndicesRef(terrainData.indexes);

			float[] vertexData = J3dNiTriBasedGeom.interleave(2, new float[][] { terrainData.textureCoordinates }, null, terrainData.colors,
					terrainData.normals, terrainData.coordinates);

			if (!BUFFERS)
			{
				iga.setInterleavedVertices(vertexData);
			}
			else
			{
				iga.setInterleavedVertexBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(vertexData)));
			}

		}
		else
		{
			if (STRIPIFY)
			{
				iga = new IndexedTriangleStripArray(terrainData.vertexCount, basicFormat, texCoordCount, texMap, // vertexAttrCount,
						//vertexAttrSizes, //
						terrainData.indexesCount, terrainData.stripCounts);
			}
			else
			{
				iga = new IndexedTriangleArray(terrainData.vertexCount, basicFormat, texCoordCount, texMap, // vertexAttrCount, vertexAttrSizes, //
						terrainData.indexesCount);
			}

			if (!BY_REF)
			{
				iga.setCoordinates(0, terrainData.coordinates);
				iga.setCoordinateIndices(0, terrainData.indexes);
				iga.setNormals(0, terrainData.normals);
				iga.setColors(0, terrainData.colors);
				iga.setTextureCoordinates(0, 0, terrainData.textureCoordinates);
			}
			else
			{
				if (!BUFFERS)
				{
					iga.setCoordRefFloat(terrainData.coordinates);
					iga.setCoordIndicesRef(terrainData.indexes);
					iga.setNormalRefFloat(terrainData.normals);
					iga.setColorRefFloat(terrainData.colors);
					iga.setTexCoordRefFloat(0, terrainData.textureCoordinates);
				}
				else
				{
					iga.setCoordRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(terrainData.coordinates)));
					iga.setCoordIndicesRef(terrainData.indexes);
					iga.setNormalRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(terrainData.normals)));
					iga.setColorRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(terrainData.colors)));
					iga.setTexCoordRefBuffer(0, new J3DBuffer(Utils3D.makeFloatBuffer(terrainData.textureCoordinates)));
				}
			}

		}

		return iga;

	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}

	/**
	 * return ATXTs array that are just faked up side faders with the right textureid on them
	 * It will be (2 per interior texture grid, plus 1 for edge grids) in size with a pointer to the original
	 * Blurr only needs to go down and left not up and right as well
	 * Each will have 1 ref to a static VTXT
	 *  
	 * @param VTEXs
	 * @return
	 */
	private static ATXT[] createTes3ATXT(int[] VTEXs)
	{
		if (rightVTXT == null)
			createTes3VTXT();

		int quadrantsPerSide = 16;

		ArrayList<ATXT> atxts = new ArrayList<ATXT>();
		for (int q = 0; q < VTEXs.length; q++)
		{
			int texFormId = VTEXs[q];

			int qx = q % quadrantsPerSide;
			int qy = q / quadrantsPerSide;

			// find each neighbour in 2 dirs (if still a valid quadrant)
			// make a ATXT with my texture and the appropriate VTXT static

			// but must put neighbour into me quadrant and keep track of layer count
			int layer = 0;

			//down
			int downqy = qy + 1;
			// did we not move off the grid?
			if (downqy < quadrantsPerSide)
			{
				int downQuadrant = (downqy * 16) + qx;
				// don't bother if it's the same texture, notice upQuandrant is  NOT is (4x4)x(4x4)
				// squares it's regular 16x16 style
				int downTexFormId = VTEXs[downQuadrant];
				if (downTexFormId != texFormId)
				{
					ATXT atxt = new LAND.ATXT();
					atxt.layer = layer;
					atxt.textureFormID = downTexFormId;
					atxt.quadrant = q;
					atxt.vtxt = downVTXT;
					atxts.add(atxt);

					layer++;
				}
			}

			//right
			int rqx = qx + 1;

			// did we not move off the grid?
			if (rqx < quadrantsPerSide)
			{
				int rightQuadrant = (qy * 16) + rqx;
				// don't bother if it's the same texture
				int rightTexFormId = VTEXs[rightQuadrant];
				if (rightTexFormId != texFormId)
				{
					ATXT atxt = new LAND.ATXT();
					atxt.layer = layer;
					atxt.textureFormID = rightTexFormId;
					atxt.quadrant = q;
					atxt.vtxt = rightVTXT;
					atxts.add(atxt);
				}
			}

		}

		ATXT[] ret = atxts.toArray(new ATXT[0]);
		return ret;
	}

	private static void createShaderProgram()
	{
		if (shaderProgram == null)
		{

			String vertexProgram = ShaderSourceIO.getTextFileAsString("./shaders/land.vert");
			String fragmentProgram = ShaderSourceIO.getTextFileAsString("./shaders/land.frag");

			Shader[] shaders = new Shader[2];
			shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram) {
				public String toString()
				{
					return "vertexProgram";
				}
			};
			shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram) {
				public String toString()
				{
					return "fragmentProgram";
				}
			};

			shaderProgram = new GLSLShaderProgram() {
				public String toString()
				{
					return "Land Shader Program";
				}
			};
			shaderProgram.setShaders(shaders);

			String[] shaderAttrNames = new String[10];

			shaderAttrNames[0] = "baseMap";
			for (int i = 1; i < 9; i++)
			{
				shaderAttrNames[i] = "layerMap" + i;
				if (OUTPUT_BINDINGS)
					System.out.println("shaderAttrNames " + shaderAttrNames[i]);
			}
			shaderAttrNames[9] = "layerCount";

			shaderProgram.setShaderAttrNames(shaderAttrNames);

		}
	}

	private static VTXT rightVTXT;

	private static VTXT downVTXT;

	private static void createTes3VTXT()
	{

		rightVTXT = new VTXT();
		rightVTXT.count = 5;
		rightVTXT.position = new int[] { 4, 9, 14, 19, 24 };// one strip down the right
		rightVTXT.opacity = new float[] { 0.5f, 1f, 1f, 1f, 0.5f };

		downVTXT = new VTXT();
		downVTXT.count = 5;
		downVTXT.position = new int[] { 20, 21, 22, 23, 24 };// one strip along the bottom
		downVTXT.opacity = new float[] { 0.5f, 1f, 1f, 1f, 0.5f };

	}

	public float getLowestHeight()
	{
		return lowestHeight;
	}
}
