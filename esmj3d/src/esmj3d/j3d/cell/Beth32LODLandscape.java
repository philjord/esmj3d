package esmj3d.j3d.cell;

import java.awt.Point;
import java.awt.Rectangle;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.IndexedGeometryArray;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;

/**
 * Used by Oblivion only, the morph land system
 * @author philip
 *
 */
public abstract class Beth32LODLandscape extends BranchGroup
{

	private int lodX = 0;

	private int lodY = 0;

	private Rectangle prevAbsBounds = new Rectangle();

	private Rectangle prevBounds = new Rectangle();

	private IndexedGeometryArray baseItsa;

	public Beth32LODLandscape(int lodX, int lodY)
	{
		this.lodX = lodX;
		this.lodY = lodY;
	}

	protected void setGeometryArray(IndexedGeometryArray baseItsa)
	{
		this.baseItsa = baseItsa;
	}

	/**
	 * these params are in obliv coords already
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

				if ((highX > 0 && lowX < 32) || (highY > 0 && lowY < 32))
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
}
