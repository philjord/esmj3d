package esmj3d.j3d.water;

import java.io.IOException;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttribute;
import javax.media.j3d.ShaderAttributeArray;
import javax.media.j3d.ShaderAttributeObject;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.vecmath.Point2f;

import nif.NifToJ3d;
import nif.j3d.J3dNiGeometry;
import tools3d.utils.Utils3D;
import utils.PerFrameUpdateBehavior;
import utils.source.TextureSource;

import com.sun.j3d.utils.shader.StringIO;

import esmj3d.j3d.j3drecords.inst.J3dLAND;

public class WaterApp extends BranchGroup
{
	private Appearance app;

	private ShaderAttributeValue timeShaderAttribute = null;

	private long start = System.currentTimeMillis();

	private WaterTexBehavior waterTexBehavior;

	private boolean USE_SHADERS = NifToJ3d.USE_SHADERS;

	public WaterApp(String defaultTexture, TextureSource textureSource)
	{
		this(new String[]
		{ defaultTexture }, textureSource);
	}

	public WaterApp(String[] textureStrings, TextureSource textureSource)
	{
		USE_SHADERS = true;

		setCapability(BranchGroup.ALLOW_DETACH);

		Texture tex = textureSource.getTexture(textureStrings[0]);
		app = createAppearance(tex);
		if (textureStrings.length > 1)
		{
			// we need a flip controller type thing now
			Texture[] textures = new Texture[textureStrings.length];

			for (int t = 0; t < textureStrings.length; t++)
			{
				textures[t] = J3dNiGeometry.loadTexture(textureStrings[t], textureSource);
			}

			if (app.getTextureUnitCount() > 0)
			{
				app.getTextureUnitState(0).setCapability(TextureUnitState.ALLOW_STATE_WRITE);
			}
			else
			{
				app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
			}

			waterTexBehavior = new WaterTexBehavior(app, textures);
			waterTexBehavior.setEnable(true);
			waterTexBehavior.setSchedulingBounds(Utils3D.defaultBounds);
			addChild(waterTexBehavior);
		}

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);

		//TODO: if the texture is not transparent we need to use the vertex color transparency
		TransparencyAttributes trans = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f);
		app.setTransparencyAttributes(trans);

		Material mat = new Material();
		mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		mat.setShininess(20.0f);
		//mat.setDiffuseColor(0.4f, 0.4f, 0.4f);
		mat.setSpecularColor(0.5f, 0.5f, 0.6f);
		app.setMaterial(mat);

		if (USE_SHADERS)
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

	public Appearance getApp()
	{
		return app;
	}

	private static ShaderProgram shaderProgram = null;

	private static String vertexProgram = null;

	private static String fragmentProgram = null;

	protected Appearance createAppearance(Texture tex)
	{
		app = null;
		if (!USE_SHADERS)
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
					vertexProgram = StringIO.readFully("./shaders/water.vert");
					fragmentProgram = StringIO.readFully("./shaders/water.frag");
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

	private class WaterTexBehavior extends Behavior
	{
		private Appearance app2;

		private Texture[] textures;

		private WakeupOnElapsedTime wakeUp;

		private int idx = 0;

		public WaterTexBehavior(Appearance app, Texture[] textures)
		{
			this.app2 = app;
			this.textures = textures;

			wakeUp = new WakeupOnElapsedTime(50);
		}

		public void initialize()
		{
			wakeupOn(wakeUp);
		}

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public void processStimulus(Enumeration critiria)
		{
			idx++;
			idx = ((idx >= textures.length) ? 0 : idx);
			if (app2.getTextureUnitCount() > 0)
			{
				app2.getTextureUnitState(0).setTexture(textures[idx]);
			}
			else
			{
				app2.setTexture(textures[idx]);
			}
			//reset the wakeup
			wakeupOn(wakeUp);
		}
	}
}
