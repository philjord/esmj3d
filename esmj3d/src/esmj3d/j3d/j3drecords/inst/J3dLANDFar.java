package esmj3d.j3d.j3drecords.inst;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.TextureUnitState;
import javax.vecmath.Color4f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;

import com.frostwire.util.SparseArray;

import esmj3d.data.shared.records.LAND;
import esmj3d.data.shared.records.LAND.BTXT;
import esmj3d.j3d.TESLANDGen;
import esmmanager.common.data.record.IRecordStore;
import tools.io.ESMByteConvert;
import tools3d.utils.ShaderSourceIO;
import utils.source.TextureSource;

public class J3dLANDFar extends J3dRECOStatInst
{

	private int reduceFactor = 4; // 2 or 4 only (2 for non tes3) 2 for far 4 for lod 

	private float lowestHeight = Float.MAX_VALUE;

	private static ShaderProgram shaderProgram = null;
	private static ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();

	/**
	 * makes the visual version of land for farness (1/4 detail no layers)
	 * @param land
	 * @param master
	 */

	public J3dLANDFar(LAND land, IRecordStore master, TextureSource textureSource)
	{
		this(land, master, textureSource, 4);
	}

	public J3dLANDFar(LAND land, IRecordStore master, TextureSource textureSource, int reduceFactor2)
	{
		super(land, false, false);
		this.reduceFactor = reduceFactor2;

		if (land.tes3)
			tes3LAND(land, master, textureSource);
		else
			LAND(land, master, textureSource);
	}

	private void LAND(LAND land, IRecordStore master, TextureSource textureSource)
	{
		int quadrantsPerSide = 2;
		int totalQuadrants = quadrantsPerSide * quadrantsPerSide;

		loadShaderProgram();

		Group baseGroup = new Group();
		addNodeChild(baseGroup);

		if (land.VHGT != null)
		{
			// extract the heights
			byte[] heightBytes = land.VHGT;
			float[][] heights = extractHeights(heightBytes);

			// extract the normals
			byte[] normalBytes = land.VNML;
			Vector3f[][] normals = J3dLAND.extractNormals(normalBytes);

			// extract the colors
			byte[] colorBytes = land.VCLR;
			Color4f[][] colors = J3dLAND.extractColors(colorBytes);

			for (int quadrant = 0; quadrant < totalQuadrants; quadrant++)
			{

				ShaderAppearance app = new ShaderAppearance();
				app.clearCapabilities();
				app.setMaterial(J3dLAND.createMat());
				app.setRenderingAttributes(J3dLAND.createRA());

				app.setShaderProgram(shaderProgram);
				app.setShaderAttributeSet(shaderAttributeSet);

				TextureUnitState tus = null;

				if (!land.tes3)
				{
					//oddly btxt are optional
					BTXT btxt = land.BTXTs[quadrant];

					if (btxt != null)
					{
						tus = J3dLAND.getTexture(btxt.textureFormID, master, textureSource);
					}
					else
					{
						tus = J3dLAND.getDefaultTexture(textureSource);
					}
				}
				else
				{
					if (land.VTEXshorts != null)
					{
						int texFormId = land.VTEXshorts[quadrant];
						tus = J3dLAND.getTextureTes3(texFormId, master, textureSource);
					}
				}

				Shape3D baseQuadShape = new Shape3D();
				baseQuadShape.clearCapabilities();
				baseQuadShape.setPickable(false);
				baseQuadShape.setCollidable(false);
				baseQuadShape.setAppearance(app);
				GeometryArray ga = makeQuadrantBaseSubGeom(heights, normals, colors, quadrantsPerSide, quadrant, 0, null, reduceFactor);
				ga.setName("LANDfar geo");
				baseQuadShape.setGeometry(ga);

				app.setTextureUnitState(new TextureUnitState[] { tus });

				baseGroup.addChild(baseQuadShape);

			}
		}
	}

	private static void loadShaderProgram()
	{
		// in case 2 threads come in trying to lazy create
		synchronized (shaderAttributeSet)
		{
			if (shaderProgram == null)
			{

				String vertexProgram = ShaderSourceIO.getTextFileAsString("shaders/landfar.vert");
				String fragmentProgram = ShaderSourceIO.getTextFileAsString("shaders/landfar.frag");

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
						return "Land (far) Shader Program";
					}
				};
				shaderProgram.setShaders(shaders);
				shaderProgram.setShaderAttrNames(new String[] { "baseMap" });

				shaderAttributeSet.put(new ShaderAttributeValue("baseMap", new Integer(0)));

			}
		}
	}

	protected static GeometryArray makeQuadrantBaseSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors,
			int quadrantsPerSide, int quadrant, int vertexAttrCount, int[] vertexAttrSizes, int reduceFactor)
	{
		int quadrantSquareCount = ((J3dLAND.GRID_COUNT / reduceFactor) / quadrantsPerSide) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrantsPerSide, quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors,
				quadrantTexCoords, reduceFactor);

		//Note that 33 by 33 sets of point equals 32 by 32 set of triangles between them
		TESLANDGen gridGenerator = new TESLANDGen(J3dLAND.LAND_SIZE / quadrantsPerSide, J3dLAND.LAND_SIZE / quadrantsPerSide,
				quadrantSquareCount, quadrantSquareCount, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		//if (J3dLAND.STRIPIFY)
		//	gridGenerator.generateIndexedTriangleStrips(terrainData);
		//else
		gridGenerator.generateIndexedTriangles(terrainData);

		//offset for quadrant
		Vector3f offset = J3dLAND.quadOffSet(quadrantsPerSide, quadrant);
		for (int i = 0; i < terrainData.coordinates.length; i += 3)
		{
			terrainData.coordinates[i + 0] += offset.x;
			terrainData.coordinates[i + 1] += offset.y;
			terrainData.coordinates[i + 2] += offset.z;
		}

		return J3dLAND.createGA(terrainData, 1, vertexAttrCount, vertexAttrSizes);

	}

	/**NOT a copy of J3dLAND as wew skip each second
	 * Note colors might have the alpha adjusted so they are copies not references
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
			TexCoord2f[][] quadrantTexCoords, int reduceFactor)
	{
		//trust me on this madness
		int qx = quadrant % quadrantsPerSide;
		int qy = quadrant / quadrantsPerSide;

		int quadrant_grid_count = ((J3dLAND.GRID_COUNT / reduceFactor) / quadrantsPerSide);

		for (int row = 0; row < quadrant_grid_count + 1; row++)
		{
			for (int col = 0; col < quadrant_grid_count + 1; col++)
			{
				int baseRow = row + (((quadrantsPerSide - 1) - qy) * quadrant_grid_count);
				int baseCol = col + ((qx) * quadrant_grid_count);
				baseRow *= reduceFactor;
				baseCol *= reduceFactor;
				quadrantHeights[row][col] = baseHeights[baseRow][baseCol];
				quadrantNormals[row][col] = baseNormals[baseRow][baseCol];
				quadrantColors[row][col] = new Color4f(baseColors[baseRow][baseCol]);//copy to allow modification
				quadrantTexCoords[row][col] = new TexCoord2f((row * (J3dLAND.TEX_REPEAT * reduceFactor)),
						(col * (J3dLAND.TEX_REPEAT * reduceFactor)));
			}
		}
	}

	private float[][] extractHeights(byte[] heightBytes)
	{
		// extract the heights
		float[][] heights = new float[(J3dLAND.GRID_COUNT + 1)][(J3dLAND.GRID_COUNT + 1)];

		float startHeightOffset = ESMByteConvert.extractFloat(heightBytes, 0);

		float startRowHeight = (startHeightOffset * 4);
		for (int row = 0; row < (J3dLAND.GRID_COUNT + 1); row++)
		{
			float height = startRowHeight;
			for (int col = 0; col < (J3dLAND.GRID_COUNT + 1); col++)
			{
				int idx = col + (row * (J3dLAND.GRID_COUNT + 1)) + 4;//+4 is start float
				height += heightBytes[idx] * 4;

				// start next row relative to the start of this row
				if (col == 0)
					startRowHeight = height;

				// note reverse order, due to x,y,z => x,z,-y
				float h = (height * J3dLAND.HEIGHT_TO_J3D_SCALE);
				heights[J3dLAND.GRID_COUNT - row][col] = h;

				//update lowest
				lowestHeight = h < lowestHeight ? h : lowestHeight;
			}
		}

		//last 3 bytes, what are they?
		// Unknown. Haven't noticed any ill-effects just filling this with arbitrary values in TES3 or TES4. 
		// This is probably just a 3-byte filler so that the entire subrecord's data can be aligned on a 4 byte word boundary.

		return heights;

	}

	public float getLowestHeight()
	{
		return lowestHeight;
	}

	public void tes3LAND(LAND land, IRecordStore master, TextureSource textureSource)
	{

		int quadrantsPerSide = 16;

		Group baseGroup = new Group();
		addNodeChild(baseGroup);

		//ensure shader ready
		createShaderProgramTes3();

		if (land.VHGT != null)
		{
			// extract the heights
			byte[] heightBytes = land.VHGT;
			float[][] heights = extractHeights(heightBytes);

			// extract the normals
			byte[] normalBytes = land.VNML;
			Vector3f[][] normals = J3dLAND.extractNormals(normalBytes);

			// extract the colors
			byte[] colorBytes = land.VCLR;
			Color4f[][] colors = J3dLAND.extractColors(colorBytes);

			ShaderAppearance app = new ShaderAppearance();
			app.clearCapabilities();
			app.setMaterial(J3dLAND.createMat());
			app.setRenderingAttributes(J3dLAND.createRA());

			Shape3D baseQuadShape = new Shape3D();
			baseQuadShape.clearCapabilities();
			baseQuadShape.setPickable(false);
			baseQuadShape.setCollidable(false);
			baseQuadShape.setAppearance(app);

			int[] attributeSizes = new int[] { 4, 4, 4, 4 };
			GeometryArray ga = makeQuadrantBaseSubGeom(heights, normals, colors, 1, 0, 4, attributeSizes, reduceFactor);
			ga.setName(land.toString() + ":LAND " + 0 + " " + land.landX + " " + land.landY);

			baseQuadShape.setGeometry(ga);

			ArrayList<ShaderAttributeValue> allShaderAttributeValues = new ArrayList<ShaderAttributeValue>();
			ArrayList<TextureUnitState> allTextureUnitStates = new ArrayList<TextureUnitState>();

			SparseArray<Integer> texIdToTUS = new SparseArray<Integer>();
			int tusCount = 0;
			//16x16 texids each one is for a set of 4x4 squares (5x5 verts make up the square) 
			//max seen 14
			for (int t = 0; t < land.VTEXshorts.length; t++)
			{
				int texFormId = land.VTEXshorts[t];
				//ensure the TUS exiosts and we have a map to it's sampler id
				if (texIdToTUS.get(texFormId) == null)
				{
					TextureUnitState tus = J3dLAND.getTextureTes3(texFormId, master, textureSource);
					allTextureUnitStates.add(tus);
					allShaderAttributeValues.add(new ShaderAttributeValue("sampler" + tusCount, new Integer(tusCount)));
					texIdToTUS.put(texFormId, new Integer(tusCount));
					//System.out.println("t " + t + " putting texid = " + texFormId + " against " + tusCount);
					tusCount++;
				}
			}

			int vertsPerSide = ((J3dLAND.GRID_COUNT / reduceFactor) + 1);
			int vertexCount = vertsPerSide * vertsPerSide;
			ByteBuffer bb = ByteBuffer.allocateDirect(vertexCount * 4 * 4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer samplers0 = bb.asFloatBuffer();
			bb = ByteBuffer.allocateDirect(vertexCount * 4 * 4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer samplers1 = bb.asFloatBuffer();
			bb = ByteBuffer.allocateDirect(vertexCount * 4 * 4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer samplers2 = bb.asFloatBuffer();
			bb = ByteBuffer.allocateDirect(vertexCount * 4 * 4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer samplers3 = bb.asFloatBuffer();
			for (int row = 0; row < vertsPerSide; row++)
			{
				for (int col = 0; col < vertsPerSide; col++)
				{
					int vertexIdx = (row * vertsPerSide) + col;

					int quadRow = (row / (4 / reduceFactor));
					int quadCol = (col / (4 / reduceFactor));
					//put final verts into prev quadrant (each has 5 lines of verts reused by the next except for the final)
					quadRow = quadRow >= quadrantsPerSide ? quadrantsPerSide - 1 : quadRow;
					quadCol = quadCol >= quadrantsPerSide ? quadrantsPerSide - 1 : quadCol;

					int quadrant = ((((quadrantsPerSide - 1) - quadRow) * quadrantsPerSide)) + quadCol;

					// look up sampler id from texture id mapped earlier
					if (quadrant < land.VTEXshorts.length)
					{
						int samplerId = texIdToTUS.get(land.VTEXshorts[quadrant]);

						//NOTICE 1s! as this is the base only
						// 0.5 etc are for the layers in a moment

						int idx = (vertexIdx * 4) + (samplerId % 4);
						if (samplerId < 4)
							samplers0.put(idx, 1);
						else if (samplerId < 8)
							samplers1.put(idx, 1);
						else if (samplerId < 12)
							samplers2.put(idx, 1);
						else if (samplerId < 16)
							samplers3.put(idx, 1);
						else
							new Throwable("SamplerId too big! " + samplerId).printStackTrace();
					}
				}
			}

			ga.setVertexAttrRefBuffer(0, new J3DBuffer(samplers0));
			ga.setVertexAttrRefBuffer(1, new J3DBuffer(samplers1));
			ga.setVertexAttrRefBuffer(2, new J3DBuffer(samplers2));
			ga.setVertexAttrRefBuffer(3, new J3DBuffer(samplers3));

			TextureUnitState[] tusa = new TextureUnitState[allTextureUnitStates.size()];
			for (int i = 0; i < allTextureUnitStates.size(); i++)
			{
				tusa[i] = allTextureUnitStates.get(i);
				//TODO: I notice the same texture repeats in the layers a lot sometimes
				if (J3dLAND.OUTPUT_BINDINGS)
					System.out.println("LAND Tus " + i + " " + tusa[i]);
			}
			app.setTextureUnitState(tusa);

			app.setShaderProgram(shaderProgram);

			ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();
			for (ShaderAttributeValue sav : allShaderAttributeValues)
			{
				if (J3dLAND.OUTPUT_BINDINGS)
					System.out.println(sav.getAttributeName() + " " + sav.getValue());

				shaderAttributeSet.put(sav);
			}
			app.setShaderAttributeSet(shaderAttributeSet);

			baseGroup.addChild(baseQuadShape);

		}

	}

	private static void createShaderProgramTes3()
	{
		// in case 2 threads come in trying to lazy create
		synchronized (shaderAttributeSet)
		{
			if (shaderProgram == null)
			{
				String vertexProgram = ShaderSourceIO.getTextFileAsString("shaders/landtes3far.vert");
				String fragmentProgram = ShaderSourceIO.getTextFileAsString("shaders/landtes3far.frag");

				Shader[] shaders = new Shader[2];
				shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram) {
					public String toString()
					{
						return "Tes3 land vertex Program";
					}
				};
				shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram) {
					public String toString()
					{
						return "Tes3 land fragment Program";
					}
				};

				shaderProgram = new GLSLShaderProgram() {
					public String toString()
					{
						return "Land Shader Program";
					}
				};
				shaderProgram.setShaders(shaders);

				String[] shaderAttrNames = new String[20];
				for (int i = 0; i < 20; i++)
				{
					shaderAttrNames[i] = "sampler" + i;
					if (J3dLAND.OUTPUT_BINDINGS)
						System.out.println("shaderAttrNames " + shaderAttrNames[i]);
				}

				shaderProgram.setShaderAttrNames(shaderAttrNames);

				String[] vertexAttrNames = new String[] { "samplers0", "samplers1", "samplers2", "samplers3" };
				shaderProgram.setVertexAttrNames(vertexAttrNames);

			}
		}
	}
}
