package esmj3d.j3d.water;

import java.util.Enumeration;

import javax.vecmath.Point2f;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderAttribute;
import org.jogamp.java3d.ShaderAttributeArray;
import org.jogamp.java3d.ShaderAttributeObject;
import org.jogamp.java3d.ShaderAttributeSet;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.WakeupOnElapsedTime;

import nif.NifToJ3d;
import nif.j3d.J3dNiGeometry;
import tools3d.utils.ShaderSourceIO;
import tools3d.utils.Utils3D;
import utils.PerFrameUpdateBehavior;
import utils.source.TextureSource;

public class WaterApp extends BranchGroup
{
	private Appearance app;

	private ShaderAttributeValue timeShaderAttribute = null;

	private long start = System.currentTimeMillis();

	private WaterTexBehavior waterTexBehavior;

	private boolean USE_SHADERS = NifToJ3d.USE_SHADERS;

	public WaterApp(String defaultTexture, TextureSource textureSource)
	{
		this(new String[] { defaultTexture }, textureSource);
	}

	public WaterApp(String[] textureStrings, TextureSource textureSource)
	{
		USE_SHADERS = true;

		setCapability(BranchGroup.ALLOW_DETACH);

		app = createAppearance(textureSource.getTextureUnitState(textureStrings[0]));
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
		TransparencyAttributes trans = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.3f);
		app.setTransparencyAttributes(trans);

		Material mat = new Material();
		mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		mat.setShininess(20.0f);
		mat.setDiffuseColor(0.4f, 0.4f, 0.45f);
		mat.setSpecularColor(0.8f, 0.8f, 0.9f);
		app.setMaterial(mat);

		if (USE_SHADERS)
		{
			PerFrameUpdateBehavior pfub = new PerFrameUpdateBehavior(new PerFrameUpdateBehavior.CallBack() {
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

	protected Appearance createAppearance(TextureUnitState tus)
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

				String vertexProgram = ShaderSourceIO.getTextFileAsString("shaders/water.vert");
				String fragmentProgram = ShaderSourceIO.getTextFileAsString("shaders/water.frag");

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

				final String[] shaderAttrNames = { "isCubeMap", "envMap", "tex", "numWaves", "amplitude", "wavelength", "speed",
						"direction", "time" };

				shaderProgram = new GLSLShaderProgram() {
					public String toString()
					{
						return "Water Shader Program";
					}
				};
				shaderProgram.setShaders(shaders);
				shaderProgram.setShaderAttrNames(shaderAttrNames);

				int isCubeMap = 0;
				ShaderAttribute shaderAttribute = new ShaderAttributeValue("isCubeMap", new Integer(isCubeMap));
				shaderAttributeSet.put(shaderAttribute);

				if (isCubeMap == 1)
				{
					shaderAttribute = new ShaderAttributeValue("envMap", new Integer(0));
					shaderAttributeSet.put(shaderAttribute);
				}
				else
				{
					shaderAttribute = new ShaderAttributeValue("tex", new Integer(0));
					shaderAttributeSet.put(shaderAttribute);
				}

				int numWaves = 4;
				shaderAttribute = new ShaderAttributeValue("numWaves", new Integer(numWaves));
				shaderAttributeSet.put(shaderAttribute);

				Float[] amplitude = new Float[numWaves];
				Float[] wavelength = new Float[numWaves];
				Float[] speed = new Float[numWaves];
				Point2f[] direction = new Point2f[numWaves];
				for (int i = 0; i < numWaves; ++i)
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

			}

			timeShaderAttribute = new ShaderAttributeValue("time", new Float(0));
			timeShaderAttribute.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			shaderAttributeSet.put(timeShaderAttribute);

			((ShaderAppearance) app).setShaderProgram(shaderProgram);
			((ShaderAppearance) app).setShaderAttributeSet(shaderAttributeSet);
		}

		app.setTextureUnitState(new TextureUnitState[] { tus });

		app.setMaterial(getLandMaterial());

		//app.setRenderingAttributes(new RenderingAttributes());

		return app;
	}

	public Material getLandMaterial()
	{

		Material landMaterial = new Material();

		landMaterial.setShininess(100.0f); // water is  very shiny, generally
		landMaterial.setDiffuseColor(0.5f, 0.5f, 0.6f);
		landMaterial.setSpecularColor(1.0f, 1.0f, 1.0f);
		landMaterial.setColorTarget(Material.AMBIENT_AND_DIFFUSE);

		return landMaterial;
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
			if (app2.getTextureUnitCount() > 0)
			{
				app2.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ);
				app2.getTextureUnitState(0).setCapability(Appearance.ALLOW_TEXTURE_WRITE);
			}
			else
			{
				app2.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
			}
			this.textures = textures;

			wakeUp = new WakeupOnElapsedTime(50);
		}

		public void initialize()
		{
			wakeupOn(wakeUp);
		}

		@SuppressWarnings({ "rawtypes" })
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