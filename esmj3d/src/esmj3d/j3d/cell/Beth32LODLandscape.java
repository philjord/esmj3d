package esmj3d.j3d.cell;

import java.awt.Rectangle;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.IndexedGeometryArray;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;

public abstract class Beth32LODLandscape extends BranchGroup
{

	private int lodX = 0;

	private int lodY = 0;

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

		System.out.println("LOD of vertex count " + baseItsa + " " + baseItsa.getIndexCount());
	}

	/**
	 * these params are in obliv coords already
	 * TODO: I really need the near grids to come in and to stich the fars into the edge of the nears
	 * but this requires much more maths and probably holding an original as well as current vertex array 
	 * 
	 * 
	 * @param charX
	 * @param charY
	 */
	public void updateVisibility(float charX, float charY)
	{
		if (baseItsa != null)
		{
			Rectangle bounds = Beth32LodManager.getBounds(charX, charY, BethRenderSettings.getNearLoadGridCount());

			//has anything happen much?
			if (!prevBounds.equals(bounds))
			{
				//adjust to this landscale x,y
				final int lowX = bounds.x - lodX;
				final int highX = bounds.x + bounds.width - lodX;
				final int lowY = bounds.y - lodY;
				final int highY = bounds.y + bounds.height - lodY;

				if ((highX > 0 && lowX < 32) || (highY > 0 && lowY < 32))
				{
					baseItsa.updateData(new GeometryUpdater()
					{
						public void updateData(Geometry geometry)
						{
							float[] coordRefFloat = baseItsa.getCoordRefFloat();

							for (int i = 0; i < coordRefFloat.length / 3; i++)
							{
								float x = coordRefFloat[(i * 3) + 0];
								float y = coordRefFloat[(i * 3) + 1];
								float z = coordRefFloat[(i * 3) + 2];

								int xSpaceIdx = (int) (x / J3dLAND.LAND_SIZE);
								int zSpaceIdx = -(int) (z / J3dLAND.LAND_SIZE);

								//notice it might start at 1000 high so only get down to just under -500
								if (xSpaceIdx >= lowX && xSpaceIdx <= highX && zSpaceIdx >= lowY && zSpaceIdx <= highY)
								{
									if (y > -500)
										coordRefFloat[(i * 3) + 1] -= 1500;
								}
								else
								{
									if (y < -500)
										coordRefFloat[(i * 3) + 1] += 1500;
								}
							}
						}
					});
				}

				prevBounds = bounds;
			}
		}
	}

}
