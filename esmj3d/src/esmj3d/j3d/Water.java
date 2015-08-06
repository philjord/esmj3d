package esmj3d.j3d;

import java.io.IOException;

import javax.media.j3d.Appearance;
import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttribute;
import javax.media.j3d.ShaderAttributeArray;
import javax.media.j3d.ShaderAttributeObject;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point2f;

import nif.NifToJ3d;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;

import tools3d.utils.Utils3D;
import utils.PerFrameUpdateBehavior;
import utils.source.TextureSource;

import com.sun.j3d.utils.shader.StringIO;

import esmj3d.j3d.j3drecords.inst.J3dLAND;

public class Water extends Group//Link
{
	private Group waterGroup;

	private ShaderAttributeValue timeShaderAttribute = null;

	private long start = System.currentTimeMillis();

	public Water(float size, String defaultTexture, TextureSource textureSource)
	{
		if (waterGroup == null)
		{
			waterGroup = new Group();

			Texture tex = textureSource.getTexture(defaultTexture);
			Appearance app = createAppearance(tex);

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

			QuadArray quads = createQuad(size);

			waterGroup.addChild(new Shape3D(quads, app));
		}

		//setSharedGroup(waterGroup);
		addChild(waterGroup);
		if (NifToJ3d.USE_SHADERS)
		{
			PerFrameUpdateBehavior pfub = new PerFrameUpdateBehavior(new PerFrameUpdateBehavior.CallBack()
			{
				@Override
				public void update()
				{
					if (timeShaderAttribute != null)
					{
						//in seconds for maths in shader
						timeShaderAttribute.setValue(new Float((System.currentTimeMillis() - start) / 1000f));
					}
				}

			});
			pfub.setSchedulingBounds(Utils3D.defaultBounds);
			pfub.setEnable(true);
			addChild(pfub);
		}
	}

	private static ShaderProgram shaderProgram = null;

	private static String vertexProgram = null;

	private static String fragmentProgram = null;

	protected Appearance createAppearance(Texture tex)
	{

		Appearance app = null;
		if (!NifToJ3d.USE_SHADERS)
		{
			app = new Appearance();
		}
		else
		{
			// Create the shader attribute set
			ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();

			app = new ShaderAppearance();

			if (shaderProgram == null)
			{
				try
				{
					vertexProgram = StringIO.readFully("./water.vert");
					fragmentProgram = StringIO.readFully("./water.frag");
				}
				catch (IOException e)
				{
					System.err.println(e);
				}

				Shader[] shaders = new Shader[2];
				shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
				shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
				final String[] shaderAttrNames =
				{ "envMap", "numWaves", "amplitude", "wavelength", "speed", "direction", "time" };

				shaderProgram = new GLSLShaderProgram();
				shaderProgram.setShaders(shaders);
				shaderProgram.setShaderAttrNames(shaderAttrNames);

				ShaderAttribute shaderAttribute = new ShaderAttributeValue("envMap", new Integer(0));
				shaderAttributeSet.put(shaderAttribute);

				shaderAttribute = new ShaderAttributeValue("numWaves", new Integer(4));
				shaderAttributeSet.put(shaderAttribute);

				Float[] amplitude = new Float[4];
				Float[] wavelength = new Float[4];
				Float[] speed = new Float[4];
				Point2f[] direction = new Point2f[4];
				for (int i = 0; i < 4; ++i)
				{
					amplitude[i] = 0.2f / (i + 1);
					wavelength[i] = (float) (8 * Math.PI / (i + 1));
					speed[i] = 1.0f + 2 * i;
					float angle = uniformRandomInRange(-Math.PI / 3, Math.PI / 3);
					direction[i] = new Point2f((float) Math.cos(angle), (float) Math.sin(angle));
				}

				ShaderAttributeArray amplitudes = new ShaderAttributeArray("amplitude", amplitude);
				ShaderAttributeArray wavelengths = new ShaderAttributeArray("wavelength", wavelength);
				ShaderAttributeArray speeds = new ShaderAttributeArray("speed", speed);
				ShaderAttributeArray directions = new ShaderAttributeArray("direction", direction);
				shaderAttributeSet.put(amplitudes);
				shaderAttributeSet.put(wavelengths);
				shaderAttributeSet.put(speeds);
				shaderAttributeSet.put(directions);

				// Create shader appearance to hold the shader program and
				// shader attributes
			}

			timeShaderAttribute = new ShaderAttributeValue("time", new Float(0));
			timeShaderAttribute.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			shaderAttributeSet.put(timeShaderAttribute);

			((ShaderAppearance) app).setShaderProgram(shaderProgram);
			((ShaderAppearance) app).setShaderAttributeSet(shaderAttributeSet);
		}

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		
		
		//TextureCubeMap textureCubeMap = new TextureCubeMap();
		
		tus[0] = tus0;
		app.setTextureUnitState(tus);

		app.setMaterial(J3dLAND.getLandMaterial());
		return app;
	}

	private static float uniformRandomInRange(double d, double e)
	{
		return (float) (d + (Math.random() * (e - d)));
	}

	private static QuadArray createQuad(float size)
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

		ElevationGridGenerator elevationGridGenerator = new ElevationGridGenerator(size, size, 30, 30);
		GeometryData gd = new GeometryData();
		gd.geometryType = GeometryData.QUADS;
		gd.geometryComponents = GeometryData.NORMAL_DATA | GeometryData.TEXTURE_2D_DATA;
		float[] flatHeights = new float[900];
		elevationGridGenerator.setTerrainDetail(flatHeights, 0);
		elevationGridGenerator.generate(gd);
		QuadArray quads = new QuadArray(gd.vertexCount, GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2);
		quads.setCoordinates(0, gd.coordinates);
		quads.setNormals(0, gd.normals);
		quads.setTextureCoordinates(0, 0, gd.textureCoordinates);
		

		return quads;
	}
}
