package esmj3d.j3d.cell;

import java.util.ArrayList;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderAttributeObject;
import org.jogamp.java3d.ShaderAttributeSet;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector2f;

import nif.shader.ShaderSourceIO;

/**
 * Used by Oblivion only, the morph land system
 * @author philip
 *
 */
public class MorphingLandscape extends BranchGroup
{
	private static ShaderProgram shaderProgram = null;

	private ArrayList<GeometryArray> baseItsas = new ArrayList<GeometryArray>();
	
	public static ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();

	public MorphingLandscape(int lodX, int lodY, int scale)
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	protected void addGeometryArray(GeometryArray baseItsa)
	{
		baseItsa.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
		baseItsa.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
		baseItsas.add(baseItsa);
	}

	protected static Appearance createAppearance(Texture tex)
	{
		ShaderAppearance app = new ShaderAppearance();
		//app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST,1));
		
		Material mat = new Material();
		mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		mat.setShininess(1.0f);
		mat.setDiffuseColor(1f, 1f, 1f);
		mat.setSpecularColor(1f, 1f, 1f);
		app.setMaterial(mat);

		app.setRenderingAttributes(new RenderingAttributes());

		if (shaderProgram == null)
		{
			loadShaderProgram();
		}
		app.setShaderProgram(shaderProgram);

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		tus[0] = tus0;
		app.setTextureUnitState(tus);		
		 
		app.setShaderAttributeSet(shaderAttributeSet);

		return app;
	}

	private static void loadShaderProgram()
	{
		if (shaderProgram == null)
		{

			String vertexProgram = ShaderSourceIO.getTextFileAsString("shaders/landlod.vert");
			String fragmentProgram = ShaderSourceIO.getTextFileAsString("shaders/landlod.frag");

			Shader[] shaders = new Shader[2];
			shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram) {
				@Override
				public String toString()
				{
					return "vertexProgram";
				}
			};
			shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram) {
				@Override
				public String toString()
				{
					return "fragmentProgram";
				}
			};

			shaderProgram = new GLSLShaderProgram() {
				@Override
				public String toString()
				{
					return "Land (lod) Shader Program";
				}
			};
			shaderProgram.setShaders(shaders);
			shaderProgram.setShaderAttrNames(new String[] { "baseMap", "minXYRemoval", "maxXYRemoval" });			 
			
			
			shaderAttributeSet.setCapability(ShaderAttributeSet.ALLOW_ATTRIBUTES_READ);
			ShaderAttributeValue sav0 = new ShaderAttributeValue("baseMap", new Integer(0));
			
			
			// this moves the uodateVisibility code into the shader
			Vector2f minXYRemoval = new Vector2f(0,0);
			Vector2f maxXYRemoval = new Vector2f(0,0);
			ShaderAttributeValue sav1 = new ShaderAttributeValue("minXYRemoval", minXYRemoval);
			ShaderAttributeValue sav2 = new ShaderAttributeValue("maxXYRemoval", maxXYRemoval);
			sav1.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			sav2.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			shaderAttributeSet.put(sav0);
			shaderAttributeSet.put(sav1);
			shaderAttributeSet.put(sav2);
		}
	}

	protected static Appearance createBasicWaterApp()
	{
		Appearance app = new SimpleShaderAppearance(new Color3f(0.5f, 0.5f, 0.6f));

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setIgnoreVertexColors(true);
		app.setRenderingAttributes(ra);

		TransparencyAttributes trans = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.1f);
		app.setTransparencyAttributes(trans);

		Material mat = new Material();
		mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		mat.setShininess(120.0f);
		mat.setDiffuseColor(0.5f, 0.5f, 0.6f);
		mat.setSpecularColor(0.9f, 0.9f, 1.0f);
		app.setMaterial(mat);
		return app;
	}

}
