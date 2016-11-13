package esmj3d.j3d.cell;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.GeometryUpdater;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Color3f;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;
import javaawt.Rectangle;
import tools3d.utils.ShaderSourceIO;

/**
 * Used by Oblivion only, the morph land system
 * @author philip
 *
 */
public class MorphingLandscape extends BranchGroup
{
	private static ShaderProgram shaderProgram = null;

	private int lodX = 0;

	private int lodY = 0;

	private int scale = 0;

	private Rectangle prevAbsBounds = new Rectangle();

	private Rectangle prevBounds = new Rectangle();

	private ArrayList<GeometryArray> baseItsas = new ArrayList<GeometryArray>();

	public MorphingLandscape(int lodX, int lodY, int scale)
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);

		this.lodX = lodX;
		this.lodY = lodY;
		this.scale = scale;
	}

	protected void addGeometryArray(GeometryArray baseItsa)
	{
		baseItsa.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
		baseItsa.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
		baseItsas.add(baseItsa);
	}

	/**
	 * these params are in lod coords already
	 * @param charX
	 * @param charY
	 */
	public void updateVisibility(float charX, float charY)
	{
		for (final GeometryArray baseItsa : baseItsas)
		{
			Rectangle absBounds = Beth32LodManager.getBounds(charX, charY, BethRenderSettings.getNearLoadGridCount());

			//has anything happen much?
			if (!prevAbsBounds.equals(absBounds))
			{
				//adjust to this landscale x,y
				final int lowX = absBounds.x - lodX;
				final int highX = (absBounds.x + absBounds.width) - lodX;
				final int lowY = absBounds.y - lodY;
				final int highY = (absBounds.y + absBounds.height) - lodY;

				if ((highX > 0 && lowX < scale) || (highY > 0 && lowY < scale))
				{
					final Rectangle bounds = new Rectangle(lowX, lowY, absBounds.width + 1, absBounds.height + 1);

					baseItsa.updateData(new GeometryUpdater() {
						public void updateData(Geometry geometry)
						{
							FloatBuffer coordRefFloat = (FloatBuffer) baseItsa.getCoordRefBuffer().getBuffer();

							Point p = new Point();
							for (int i = 0; i < coordRefFloat.limit() / 3; i++)
							{
								float x = coordRefFloat.get((i * 3) + 0);
								//float y = coordRefFloat[(i * 3) + 1];
								float z = coordRefFloat.get((i * 3) + 2);

								int xSpaceIdx = (int) (x / J3dLAND.LAND_SIZE);
								int zSpaceIdx = -(int) (z / J3dLAND.LAND_SIZE);

								p.setLocation(xSpaceIdx, zSpaceIdx);

								if (bounds.contains(p) && !prevBounds.contains(p))
								{
									coordRefFloat.put((i * 3) + 1, coordRefFloat.get((i * 3) + 1) - 40);
								}
								else if (!bounds.contains(p) && prevBounds.contains(p))
								{
									coordRefFloat.put((i * 3) + 1, coordRefFloat.get((i * 3) + 1) + 40);
								}
							}

						}
					});
					prevBounds = bounds;
				}

				prevAbsBounds = absBounds;
			}
		}
	}

	protected static Appearance createAppearance(Texture tex)
	{
		ShaderAppearance app = new ShaderAppearance();
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

		return app;
	}

	private static void loadShaderProgram()
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
					return "Land (lod) Shader Program";
				}
			};
			shaderProgram.setShaders(shaders);
			shaderProgram.setShaderAttrNames(new String[] { "baseMap" });

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
