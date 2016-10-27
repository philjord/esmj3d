package esmj3d.j3d.water;

import java.util.HashMap;

import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.J3DBuffer;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.geom.GeometryData;
import org.jogamp.vecmath.Color4f;
import org.jogamp.vecmath.TexCoord2f;
import org.jogamp.vecmath.Vector3f;

import esmj3d.j3d.TESLANDGen;
import tools3d.utils.Utils3D;

public class Water extends Group
{
	public Water(float size, WaterApp waterApp)
	{
		if (waterApp != null)
		{
			Shape3D s = new Shape3D(createQuad(size), waterApp.getApp());
			addChild(s);
		}
		else
		{
			System.err.println("No water App");
		}
	}

	//private static HashMap<Float, IndexedTriangleStripArray> preLoadedQuads = new HashMap<Float, IndexedTriangleStripArray>();
	private static HashMap<Float, IndexedTriangleArray> preLoadedQuads = new HashMap<Float, IndexedTriangleArray>();

	private static GeometryArray createQuad(float size)
	{
		// don't let two threads try to create the quad
		//	IndexedTriangleStripArray quads = null;
		IndexedTriangleArray quads = null;
		synchronized (preLoadedQuads)
		{
			quads = preLoadedQuads.get(size);

			if (quads == null)
			{

				float TEX_REPEAT = 2f;// suggests how many times to repeat the texture over the entire square

				int quadrantSquareCount = 10;
				float[][] quadrantHeights = new float[quadrantSquareCount][quadrantSquareCount];
				Vector3f[][] quadrantNormals = new Vector3f[quadrantSquareCount][quadrantSquareCount];
				Color4f[][] quadrantColors = new Color4f[quadrantSquareCount][quadrantSquareCount];
				TexCoord2f[][] quadrantTexCoords = new TexCoord2f[quadrantSquareCount][quadrantSquareCount];
				for (int row = 0; row < quadrantSquareCount; row++)
				{
					for (int col = 0; col < quadrantSquareCount; col++)
					{
						quadrantHeights[row][col] = 0;
						quadrantNormals[row][col] = new Vector3f(0, 1, 0);
						quadrantColors[row][col] = new Color4f(0.8f, 0.8f, 0.9f, 0.8f);
						quadrantTexCoords[row][col] = new TexCoord2f(((row / (float) quadrantSquareCount) * TEX_REPEAT),
								((col / (float) quadrantSquareCount) * TEX_REPEAT));
					}
				}

				TESLANDGen gridGenerator = new TESLANDGen(size, size, quadrantSquareCount, quadrantSquareCount, quadrantHeights,
						quadrantNormals, quadrantColors, quadrantTexCoords);

				GeometryData gd = new GeometryData();
				gd.geometryType = GeometryData.INDEXED_TRIANGLES;
				gd.geometryComponents = GeometryData.NORMAL_DATA | GeometryData.TEXTURE_2D_DATA;

				gridGenerator.generateIndexedTriangles(gd);
				quads = new IndexedTriangleArray(gd.vertexCount,
						GeometryArray.COORDINATES | GeometryArray.USE_COORD_INDEX_ONLY | GeometryArray.NORMALS | GeometryArray.COLOR_4
								| GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE,
						gd.indexesCount);

				quads.setCoordinateIndices(0, gd.indexes);
				quads.setCoordRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(gd.coordinates)));
				quads.setNormalRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(gd.normals)));
				quads.setColorRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(gd.colors)));
				quads.setTexCoordRefBuffer(0, new J3DBuffer(Utils3D.makeFloatBuffer(gd.textureCoordinates)));
				quads.setName("Water");
				preLoadedQuads.put(size, quads);
			}
		}
		return quads;
	}
}
