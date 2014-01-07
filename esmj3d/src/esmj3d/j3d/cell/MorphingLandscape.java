package esmj3d.j3d.cell;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttribute;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;

import nif.NifToJ3d;

import com.sun.j3d.utils.shader.StringIO;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;

/**
 * Used by Oblivion only, the morph land system
 * @author philip
 *
 */
public class MorphingLandscape extends BranchGroup
{

	private int lodX = 0;

	private int lodY = 0;

	private int scale = 0;

	private Rectangle prevAbsBounds = new Rectangle();

	private Rectangle prevBounds = new Rectangle();

	private IndexedGeometryArray baseItsa;

	public MorphingLandscape(int lodX, int lodY, int scale)
	{
		this.lodX = lodX;
		this.lodY = lodY;
		this.scale = scale;
	}

	protected void setGeometryArray(IndexedGeometryArray baseItsa)
	{
		this.baseItsa = baseItsa;
	}

	/**
	 * these params are in lod coords already
	 * @param charX
	 * @param charY
	 */
	public void updateVisibility(float charX, float charY)
	{
		if (baseItsa != null)
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

					baseItsa.updateData(new GeometryUpdater()
					{
						public void updateData(Geometry geometry)
						{
							float[] coordRefFloat = baseItsa.getCoordRefFloat();

							Point p = new Point();
							for (int i = 0; i < coordRefFloat.length / 3; i++)
							{
								float x = coordRefFloat[(i * 3) + 0];
								//float y = coordRefFloat[(i * 3) + 1];
								float z = coordRefFloat[(i * 3) + 2];

								int xSpaceIdx = (int) (x / J3dLAND.LAND_SIZE);
								int zSpaceIdx = -(int) (z / J3dLAND.LAND_SIZE);

								p.setLocation(xSpaceIdx, zSpaceIdx);

								if (bounds.contains(p) && !prevBounds.contains(p))
								{
									coordRefFloat[(i * 3) + 1] -= 20;
								}
								else if (!bounds.contains(p) && prevBounds.contains(p))
								{
									coordRefFloat[(i * 3) + 1] += 20;
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

	private static ShaderProgram shaderProgram = null;

	private static ShaderAttributeSet shaderAttributeSet = null;

	private static String vertexProgram = null;

	private static String fragmentProgram = null;

	protected static Appearance createAppearance(Texture tex)
	{
		Appearance app = null;
		if (!NifToJ3d.USE_SHADERS)
		{
			app = new Appearance();
		}
		else
		{
			app = new ShaderAppearance();

			if (shaderProgram == null)
			{
				try
				{
					vertexProgram = StringIO.readFully("./fixedpipeline.vert");
					fragmentProgram = StringIO.readFully("./fixedpipeline.frag");
				}
				catch (IOException e)
				{
					System.err.println(e);
				}

				Shader[] shaders = new Shader[2];
				shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
				shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
				final String[] shaderAttrNames =
				{ "tex" };
				final Object[] shaderAttrValues =
				{ new Integer(0) };
				shaderProgram = new GLSLShaderProgram();
				shaderProgram.setShaders(shaders);
				shaderProgram.setShaderAttrNames(shaderAttrNames);

				// Create the shader attribute set
				shaderAttributeSet = new ShaderAttributeSet();
				for (int i = 0; i < shaderAttrNames.length; i++)
				{
					ShaderAttribute shaderAttribute = new ShaderAttributeValue(shaderAttrNames[i], shaderAttrValues[i]);
					shaderAttributeSet.put(shaderAttribute);
				}

				// Create shader appearance to hold the shader program and
				// shader attributes
			}
			((ShaderAppearance) app).setShaderProgram(shaderProgram);
			((ShaderAppearance) app).setShaderAttributeSet(shaderAttributeSet);

		}

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		tus[0] = tus0;
		app.setTextureUnitState(tus);

		app.setMaterial(J3dLAND.getLandMaterial());
		return app;
	}
}
