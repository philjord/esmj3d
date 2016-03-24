package esmj3d.j3d.cell;

import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.Group;
import javax.media.j3d.LinearFog;
import javax.vecmath.Color3f;

import tools3d.utils.Utils3D;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;
import javaawt.Rectangle;

public class Beth32LodManager extends BethLodManager
{
	private static int OBLIVION_MIN_LOD = -96;

	private static int OBLIVION_MAX_LOD = 96; //NOTE < only never = nothing is 96!

	private static int SCALE_32 = 32;

	private HashMap<Point, MorphingLandscape> loadedGrosses = new HashMap<Point, MorphingLandscape>();

	private String lodWorldFormId = "";

	private J3dICellFactory j3dCellFactory;

	public Beth32LodManager(J3dICellFactory j3dCellFactory)
	{
		this.j3dCellFactory = j3dCellFactory;
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		//create a hillarious distance fog
		//LinearFog, ExponentialFog
		LinearFog fog = new LinearFog(new Color3f(0.8f, 0.8f, 0.8f), 500, 3500);
		fog.addScope(this);
		fog.setInfluencingBounds(Utils3D.defaultBounds);
		addChild(fog);

	}

	public void setWorldFormId(int worldFormId)
	{
		String newLodWorldFormId = j3dCellFactory.getLODWorldName(worldFormId);
		if (!this.lodWorldFormId.equals(newLodWorldFormId))
		{
			this.removeAllChildren();
			this.lodWorldFormId = newLodWorldFormId;

			long start = System.currentTimeMillis();

			for (int x = OBLIVION_MIN_LOD; x < OBLIVION_MAX_LOD; x += SCALE_32)
			{
				for (int y = OBLIVION_MIN_LOD; y < OBLIVION_MAX_LOD; y += SCALE_32)
				{
					Point key = new Point(x, y);
					MorphingLandscape bg = (MorphingLandscape) j3dCellFactory.makeLODLandscape(x, y, SCALE_32, lodWorldFormId);
					loadedGrosses.put(key, bg);
					bg.compile();// better to be done not on the j3d thread?
					//addChild(bg);// don't add yet, updateGross will do so shortly
				}
			}
			if ((System.currentTimeMillis() - start) > 50)
				System.out.println("Beth32LodManager.setWorldFormId in " + (System.currentTimeMillis() - start) + "ms");
		}
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
			MorphingLandscape oblivLODLandscape = loadedGrosses.get(key);
			if (Math.abs(key.x - charPoint.x) <= BethRenderSettings.getLOD_LOAD_DIST_MAX()
					&& Math.abs(key.y - charPoint.y) <= BethRenderSettings.getLOD_LOAD_DIST_MAX())
			{
				if (!oblivLODLandscape.isLive())
					addChild(oblivLODLandscape);

				if (key.distance(charPoint) <= 64)
				{
					oblivLODLandscape.updateVisibility(charX, charY);
				}
			}
			else
			{
				if (oblivLODLandscape.isLive())
					removeChild(oblivLODLandscape);
			}
		}

		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("Beth32LodManager.updateGross in " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * 4 ints represent the near grid x,y locations (not floats of real points)
	 * @return
	 */
	public Rectangle getGridBounds(float charX, float charY, int loadGridCount)
	{
		return getBounds(charX, charY, loadGridCount);
	}

	public static Rectangle getBounds(float charX, float charY, int loadGridCount)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);

		int lowX = (charLodX - loadGridCount);
		int lowY = (charLodY - loadGridCount);

		return new Rectangle(lowX, lowY, (loadGridCount * 2), (loadGridCount * 2));
	}
}
