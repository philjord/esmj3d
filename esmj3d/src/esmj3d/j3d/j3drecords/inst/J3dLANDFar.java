package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TextureAttributes;
import javax.vecmath.Color4f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;

import tools.io.ESMByteConvert;
import utils.source.TextureSource;
import esmLoader.common.data.record.IRecordStore;
import esmj3d.data.shared.records.LAND;
import esmj3d.data.shared.records.LAND.BTXT;
import esmj3d.j3d.TESLANDGen;

public class J3dLANDFar extends J3dRECOStatInst
{

	/**
	 * makes the visual version of land for farness (1/4 detail no layers)
	 * @param land
	 * @param master
	 */

	private static TextureAttributes textureAttributesBase = null;

	private Geometry[] quadrantBaseSubGeoms;

	public J3dLANDFar(LAND land, IRecordStore master, TextureSource textureSource)
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

			// make up some base quadrants, keep seperate to allow frustrum culling
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
				//VTEXs a bad def that in fact should be only in the older tes3 system, I wager
				if (land.VTEXids.length > 0)
				{
					System.out.println("***********************VTEXs in LAND");
				}

				// make up some base land texture, pre sorted to btxt by quadrant
				for (int quadrant = 0; quadrant < totalQuadrants; quadrant++)
				{
					Appearance app = createAppearance();

					app.setMaterial(J3dLAND.getLandMaterial());
					app.setTextureAttributes(textureAttributesBase);

					app.setTexture(J3dLAND.getDefaultTexture(textureSource));
					//oddly btxt are optional
					BTXT btxt = land.BTXTs[quadrant];
					if (btxt != null && btxt.textureFormID != 0)
					{
						app.setTexture(J3dLAND.getTexture(btxt.textureFormID, master, textureSource));
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
					app.setMaterial(J3dLAND.getLandMaterial());
					app.setTextureAttributes(textureAttributesBase);

					app.setTexture(J3dLAND.getTextureTes3(texFormId, master, textureSource));

					Shape3D baseQuadShape = new Shape3D();
					baseQuadShape.setAppearance(app);

					baseQuadShape.setGeometry(quadrantBaseSubGeoms[quadrant]);

					quadrantBaseGroups[quadrant].addChild(baseQuadShape);
				}
			}
		}
	}

	protected Appearance createAppearance()
	{
		return new Appearance();
	}

	protected static GeometryArray makeQuadrantBaseSubGeom(float[][] heights, Vector3f[][] normals, Color4f[][] colors,
			int quadrantsPerSide, int quadrant)
	{
		int quadrantSquareCount = (J3dLAND.GRID_COUNT / quadrantsPerSide) + 1;
		float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
		Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
		Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
		TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];

		makeQuadrantData(quadrantsPerSide, quadrant, heights, normals, colors, quadrantHeights, quadrantNormals, quadrantColors,
				quadrantTexCoords);

		//Note that 33 by 33 sets of point equals 32 by 32 set of triangles between them
		TESLANDGen gridGenerator = new TESLANDGen(J3dLAND.LAND_SIZE / quadrantsPerSide, J3dLAND.LAND_SIZE / quadrantsPerSide,
				quadrantSquareCount, quadrantSquareCount, quadrantHeights, quadrantNormals, quadrantColors, quadrantTexCoords);

		GeometryData terrainData = new GeometryData();
		gridGenerator.generateIndexedTriangleStrips(terrainData);

		//offset for quadrant
		Vector3f offset = J3dLAND.quadOffSet(quadrantsPerSide, quadrant);
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

		int quadrant_grid_count = (J3dLAND.GRID_COUNT / quadrantsPerSide);

		for (int row = 0; row < quadrant_grid_count + 1; row++)
		{
			for (int col = 0; col < quadrant_grid_count + 1; col++)
			{
				int baseRow = row + (((quadrantsPerSide - 1) - qy) * quadrant_grid_count);
				int baseCol = col + ((qx) * quadrant_grid_count);
				quadrantHeights[row][col] = baseHeights[baseRow][baseCol];
				quadrantNormals[row][col] = baseNormals[baseRow][baseCol];
				quadrantColors[row][col] = new Color4f(baseColors[baseRow][baseCol]);//copy to allow modification
				quadrantTexCoords[row][col] = new TexCoord2f((row * J3dLAND.TEX_REPEAT), (col * J3dLAND.TEX_REPEAT));
			}
		}
	}

	protected static float[][] extractHeights(byte[] heightBytes)
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
				heights[J3dLAND.GRID_COUNT - row][col] = (height * J3dLAND.HEIGHT_TO_J3D_SCALE);
			}
		}

		//last 3 bytes, what are they?
		// Unknown. Haven't noticed any ill-effects just filling this with arbitrary values in TES3 or TES4. 
		// This is probably just a 3-byte filler so that the entire subrecord's data can be aligned on a 4 byte word boundary.

		return heights;

	}

	protected static Vector3f[][] extractNormals(byte[] normalBytes)
	{
		Vector3f[][] normals = new Vector3f[(J3dLAND.GRID_COUNT + 1)][(J3dLAND.GRID_COUNT + 1)];
		for (int row = 0; row < (J3dLAND.GRID_COUNT + 1); row++)
		{
			for (int col = 0; col < (J3dLAND.GRID_COUNT + 1); col++)
			{
				byte x = normalBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 0];
				byte y = normalBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 1];
				byte z = normalBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 2];

				Vector3f v = new Vector3f(x & 0xff, z & 0xff, -y & 0xff);
				v.normalize();
				// note reverse order, due to x,y,z => x,z,-y
				normals[J3dLAND.GRID_COUNT - row][col] = v;
			}
		}
		return normals;
	}

	protected static Color4f[][] extractColors(byte[] colorBytes)
	{

		Color4f[][] colors = new Color4f[(J3dLAND.GRID_COUNT + 1)][(J3dLAND.GRID_COUNT + 1)];

		for (int row = 0; row < (J3dLAND.GRID_COUNT + 1); row++)
		{
			for (int col = 0; col < (J3dLAND.GRID_COUNT + 1); col++)
			{
				if (colorBytes != null)
				{
					float r = (colorBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 0] & 0xff) / 255f;
					float g = (colorBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 1] & 0xff) / 255f;
					float b = (colorBytes[(col + (row * (J3dLAND.GRID_COUNT + 1))) * 3 + 2] & 0xff) / 255f;
					Color4f c = new Color4f(r, g, b, 1.0f);//note hard coded opaque

					// note reverse order, due to x,y,z => x,z,-y
					colors[J3dLAND.GRID_COUNT - row][col] = c;
				}
				else
				{
					// no colors let's try white
					colors[J3dLAND.GRID_COUNT - row][col] = new Color4f(1.0f, 1.0f, 1.0f, 1.0f);
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
