package esmj3d.j3d.cell;

import org.jogamp.java3d.BranchGroup;

import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;
import javaawt.Rectangle;
import javaawt.geom.Point2D;

public abstract class BethLodManager extends BranchGroup
{		
	
	public static float highPortion = (1.0f-(1.0f/6.0f));
	public static float lowPortion = (1.0f/6.0f);
	
	public BethLodManager()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public abstract void updateGross(float charX, float charY);

	public abstract void setWorldFormId(int worldFormId);
	
	public static Point convertCharToLodXY(float charX, float charY)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);
		return new Point(charLodX, charLodY);
	}
	
	public static Point2D.Float convertCharToLodXYf(float charX, float charY)
	{
		float charLodX = charX / J3dLAND.LAND_SIZE;
		float charLodY = charY / J3dLAND.LAND_SIZE;
		return new Point2D.Float(charLodX, charLodY);
	}
	
	public static Point2D.Float charDistAcrossCell(float charX, float charY)
	{
		Point charLod = convertCharToLodXY(charX, charY);
		Point2D.Float charLodFloat = BethLodManager.convertCharToLodXYf(charX, charY);		
		float xdistAcrossCell = charLodFloat.x-charLod.x;
		float ydistAcrossCell = charLodFloat.y-charLod.y;
		return new Point2D.Float(xdistAcrossCell, ydistAcrossCell);
	}
		
	/**
	 * 4 ints represent the near grid x,y locations. The don't include widths final grid (e.g. x = 2 width = 3 means grids 2,3,4  so from =2 to <5 you get 2,5 back from here not 2,4 )
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
