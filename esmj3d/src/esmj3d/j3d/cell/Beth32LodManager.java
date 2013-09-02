package esmj3d.j3d.cell;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.Group;

import esmj3d.j3d.j3drecords.inst.J3dLAND;

public class Beth32LodManager extends Group
{
	private static int OBLIVION_MIN_LOD = -96;

	private static int OBLIVION_MAX_LOD = 96; //NOTE < only never = nothing is 96!

	private static int SCALE_32 = 32;

	private HashMap<Point, Beth32LODLandscape> loadedGrosses = new HashMap<Point, Beth32LODLandscape>();

	private int worldFormId;

	private String worldFormName;

	public Beth32LodManager(int worldFormId, String worldFormName, J3dICellFactory j3dCellFactory)
	{
		this.worldFormId = worldFormId;
		this.worldFormName = worldFormName;

		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		long start = System.currentTimeMillis();

		for (int x = OBLIVION_MIN_LOD; x < OBLIVION_MAX_LOD; x += SCALE_32)
		{
			for (int y = OBLIVION_MIN_LOD; y < OBLIVION_MAX_LOD; y += SCALE_32)
			{
				Point key = new Point(x, y);
				Beth32LODLandscape bg = (Beth32LODLandscape) j3dCellFactory.makeLODLandscape(x, y, SCALE_32, worldFormId, worldFormName);
				loadedGrosses.put(key, bg);
				addChild(bg);
			}
		}
		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("Beth32LodManager.init " + (System.currentTimeMillis() - start));

	}

	public void updateGross(float charX, float charY)
	{
		long start = System.currentTimeMillis();
		Point charPoint = new Point((int) Math.floor(charX / J3dLAND.LAND_SIZE), (int) Math.floor(charY / J3dLAND.LAND_SIZE));
		// now to tell each one that's loaded to update themselves
		Iterator<Point> keys = loadedGrosses.keySet().iterator();
		while (keys.hasNext())
		{
			Point key = keys.next();
			if (key.distance(charPoint) <= 64)
			{
				Beth32LODLandscape oblivLODLandscape = loadedGrosses.get(key);
				oblivLODLandscape.updateVisibility(charX, charY);
			}
		}

		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("Beth32LodManager.updateGross " + (System.currentTimeMillis() - start));
	}

	/**
	 * 4 ints represent teh near grid x,y locations (not floats of real points)
	 * @return
	 */
	public static Rectangle getBounds(float charX, float charY, int loadGridCount)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);

		int lowX = (charLodX - loadGridCount);
		int lowY = (charLodY - loadGridCount);

		return new Rectangle(lowX, lowY, loadGridCount * 2, loadGridCount * 2);
	}
}
