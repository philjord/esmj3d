package esmj3d.j3d.water;

import java.util.HashMap;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleStripArray;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;

import tools3d.utils.Utils3D;

public class Water extends Group
{
	public Water(float size, WaterApp waterApp)
	{
		if (waterApp != null)
		{
			Shape3D s = new Shape3D(createQuad(size), waterApp.getApp());
			s.clearCapabilities();
			addChild(s);
		}
		else
		{
			System.err.println("No water App");
		}
	}

	private static HashMap<Float, TriangleArray> preLoadedQuads = new HashMap<Float, TriangleArray>();

	private static GeometryArray createQuad(float size)
	{

		/*	QuadArray quads = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2
					| GeometryArray.COLOR_4);
		
			quads.setCoordinate(0, new Point3f(-size / 2f, 0, -size / 2f));
			quads.setCoordinate(1, new Point3f(-size / 2f, 0, size / 2f));
			quads.setCoordinate(2, new Point3f(size / 2f, 0, size / 2f));
			quads.setCoordinate(3, new Point3f(size / 2f, 0, -size / 2f));
			quads.setNormal(0, new Vector3f(0f, 1f, 0f));
			quads.setNormal(1, new Vector3f(0f, 1f, 0f));
			quads.setNormal(2, new Vector3f(0f, 1f, 0f));
			quads.setNormal(3, new Vector3f(0f, 1f, 0f));
			quads.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0f));
			quads.setTextureCoordinate(0, 1, new TexCoord2f(0f, 4f));
			quads.setTextureCoordinate(0, 2, new TexCoord2f(4f, 4f));
			quads.setTextureCoordinate(0, 3, new TexCoord2f(4f, 0f));
			quads.setColor(0, new Color4f(0.8f, 0.9f, 1.0f, 0.5f));
			quads.setColor(1, new Color4f(0.8f, 0.9f, 1.0f, 0.5f));
			quads.setColor(2, new Color4f(0.8f, 0.9f, 1.0f, 0.5f));
			quads.setColor(3, new Color4f(0.8f, 0.9f, 1.0f, 0.5f));*/

		TriangleArray quads = preLoadedQuads.get(size);

		if (quads == null)
		{
			ElevationGridGenerator elevationGridGenerator = new ElevationGridGenerator(size, size, 30, 30);
			GeometryData gd = new GeometryData();
			//TODO: once tristrips work set back to tristrips (indexed too!)

			//	gd.geometryType = GeometryData.TRIANGLE_STRIPS;
			gd.geometryType = GeometryData.TRIANGLES;
			gd.geometryComponents = GeometryData.NORMAL_DATA | GeometryData.TEXTURE_2D_DATA;
			float[] flatHeights = new float[900];
			elevationGridGenerator.setTerrainDetail(flatHeights, 0);
			elevationGridGenerator.generate(gd);
			//	quads = new TriangleStripArray(gd.vertexCount, GeometryArray.COORDINATES | GeometryArray.NORMALS
			//			| GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE, gd.stripCounts);

			quads = new TriangleArray(gd.vertexCount, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2
					| GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE);

			// repeat image every say 10 of size?
			for (int i = 0; i < gd.textureCoordinates.length; i++)
			{
				gd.textureCoordinates[i] *= size / 10f;
			}

			quads.setCoordRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(gd.coordinates)));
			quads.setNormalRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(gd.normals)));
			quads.setTexCoordRefBuffer(0, new J3DBuffer(Utils3D.makeFloatBuffer(gd.textureCoordinates)));
			quads.setName("Water");
			preLoadedQuads.put(size, quads);
		}
		return quads;
	}
}
