package esmj3d.j3d.cell;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransparencyAttributes;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import tools3d.utils.SimpleShaderAppearance;
import tools3d.utils.Utils3D;

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

	private ArrayList<IndexedGeometryArray> baseItsas = new ArrayList<IndexedGeometryArray>();

	public MorphingLandscape(int lodX, int lodY, int scale)
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);

		this.lodX = lodX;
		this.lodY = lodY;
		this.scale = scale;
	}

	protected void addGeometryArray(IndexedGeometryArray baseItsa)
	{
		baseItsas.add(baseItsa);
	}

	/**
	 * these params are in lod coords already
	 * @param charX
	 * @param charY
	 */
	public void updateVisibility(float charX, float charY)
	{
		for (final IndexedGeometryArray baseItsa : baseItsas)
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

	protected static Appearance createAppearance(Texture tex)
	{
		Appearance app = new SimpleShaderAppearance(true);

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		tus[0] = tus0;
		app.setTextureUnitState(tus);

		Material mat = new Material();
		mat.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		mat.setShininess(1.0f);
		mat.setDiffuseColor(0.5f, 0.6f, 0.5f);
		mat.setSpecularColor(0.0f, 0.0f, 0.0f);
		app.setMaterial(mat);

		app.setRenderingAttributes(new RenderingAttributes());

		return app;
	}

	protected static Appearance createBasicWaterApp()
	{
		Appearance app = new SimpleShaderAppearance();

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(pa);
		RenderingAttributes ra = new RenderingAttributes();
		ra.setIgnoreVertexColors(true);//TODO: this is not working so fonv water is still white
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

	protected static Shape3D createBasicWater(float rectWidth, float rectHeight)
	{
		// ready for prebaking coords if required
		float x = 0;
		float y = 0;
		float z = 0;

		float yPosition = 0f;

		float[] verts1 = { x + (rectWidth / 2), y + yPosition, z + (-rectHeight / 2), //
				x + (rectWidth / 2), y + yPosition, z + (rectHeight / 2), //
				x + (-rectWidth / 2), y + yPosition, z + (rectHeight / 2), //
				x + (-rectWidth / 2), y + yPosition, z + (-rectHeight / 2) //
		};

		float[] normals = { 0f, 0f, 1f, //
				0f, 0f, 1f, //
				0f, 0f, 1f, //
				0f, 0f, 1f, //
		};

		QuadArray rect = new QuadArray(4,
				GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE);
		//rect.setCoordinates(0, verts1);
		//rect.setNormals(0, normals);
		rect.setCoordRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(verts1)));
		rect.setNormalRefBuffer(new J3DBuffer(Utils3D.makeFloatBuffer(normals)));

		Shape3D shape = new Shape3D(rect, createBasicWaterApp());
		return shape;
	}

}
