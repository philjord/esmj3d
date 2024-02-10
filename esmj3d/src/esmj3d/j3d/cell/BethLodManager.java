package esmj3d.j3d.cell;

import org.jogamp.java3d.BranchGroup;

import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Rectangle;
import javaawt.geom.Rectangle2D;

public abstract class BethLodManager extends BranchGroup
{	
	protected int nearGridLoadCount = 1;
	
	public BethLodManager()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public abstract void updateGross(float charX, float charY);

	public abstract void setWorldFormId(int worldFormId);

	public abstract Rectangle getGridBounds(float charX, float charY);
		
	// FIXME!! this is identical to getGridBounds in Beth32_4LodManager
	public static Rectangle getBounds(float charX, float charY, int count)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);

		int lowX = (charLodX - count);
		int lowY = (charLodY - count);
		//+1 cos 1 size is 3 wide the center plus 1 on each side, see getGridBounds
		int w = (count * 2) + 1;
		int h = (count * 2) + 1;
		return new Rectangle(lowX, lowY, w, h);
	}
	
	public int getNearGridLoadCount() {
		return nearGridLoadCount;
	}

	public void setNearGridLoadCount(int nearGridLoadCount) {
		this.nearGridLoadCount = nearGridLoadCount;
	}
}
