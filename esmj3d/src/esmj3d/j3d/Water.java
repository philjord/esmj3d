package esmj3d.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Texture;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import utils.source.TextureSource;

public class Water extends Link
{
	private static SharedGroup waterGroup;

	public Water(float size, String defaultTexture, TextureSource textureSource)
	{
		if (waterGroup == null)
		{
			waterGroup = new SharedGroup();

			Appearance app = new Appearance();

			Texture tex = textureSource.getTexture(defaultTexture);
			app.setTexture(tex);

			PolygonAttributes pa = new PolygonAttributes();
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			app.setPolygonAttributes(pa);

			TransparencyAttributes trans = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f);
			app.setTransparencyAttributes(trans);

			Material mat = new Material();
			mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
			mat.setShininess(20.0f);
			//mat.setDiffuseColor(0.4f, 0.4f, 0.4f);
			mat.setSpecularColor(0.5f, 0.5f, 0.6f);
			app.setMaterial(mat);

			QuadArray quads = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2
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
			quads.setColor(3, new Color4f(0.8f, 0.9f, 1.0f, 0.5f));

			waterGroup.addChild(new Shape3D(quads, app));
		}

		setSharedGroup(waterGroup);
	}
}
