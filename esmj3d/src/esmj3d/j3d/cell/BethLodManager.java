package esmj3d.j3d.cell;

import org.jogamp.java3d.BranchGroup;

import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;
import javaawt.Rectangle;

public abstract class BethLodManager extends BranchGroup
{		
	public BethLodManager()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public abstract void updateGross(float charX, float charY);

	public abstract void setWorldFormId(int worldFormId);
	
	public Point convertCharToLodXY(float charX, float charY)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);
		return new Point(charLodX, charLodY);
	}
		
	/**
	 * 4 ints represent the near grid x,y locations. The count of grids to include (e.g. x = 2 width = 4 means grids 2,3,4,5,6 that's 5! grids)
	 * @return
	 */
	public static Rectangle getGridBounds(float charX, float charY, int gridCount)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);

		int lowX = (charLodX - gridCount);
		int lowY = (charLodY - gridCount);
		//+1 cos 1 size is 3 wide the center plus 1 on each side 
		int w = (gridCount * 2) + 1;
		int h = (gridCount * 2) + 1;
		return new Rectangle(lowX, lowY, w, h);
	}
	

}
