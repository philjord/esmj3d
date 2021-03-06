package esmj3d.j3d.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.LinearFog;
import org.jogamp.java3d.Node;
import org.jogamp.vecmath.Color3f;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;
import javaawt.Rectangle;
import tools3d.utils.Utils3D;

public class Beth32_4LodManager extends BethLodManager
{
	private static int SKYRIM_LOD_SCOPE = 96;

	private static int FO3_LOD_SCOPE = 64;

	private static int LOD_SCOPE_EXTREMES = SKYRIM_LOD_SCOPE;//NOTE  nothing is +96 or +64!

	private HashMap<Point, BranchGroup> loadedGrosses32 = new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup> loadedGrosses16 = new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup> loadedGrosses8 = new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup> loadedGrosses4 = new HashMap<Point, BranchGroup>();

	private String lodWorldName = "";

	private J3dICellFactory j3dCellFactory;

	public Beth32_4LodManager(J3dICellFactory j3dCellFactory)
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

		if (j3dCellFactory.getMainESMFileName().toLowerCase().contains("fallout"))
		{
			LOD_SCOPE_EXTREMES = FO3_LOD_SCOPE;
		}

	}

	@Override
	public void setWorldFormId(int worldFormId)
	{
		String newLodWorldName = j3dCellFactory.getLODWorldName(worldFormId);

		if (!this.lodWorldName.equals(newLodWorldName))
		{
			this.removeAllChildren();
			this.lodWorldName = newLodWorldName;
		}
	}

	@Override
	public void updateGross(float charX, float charY)
	{
		if (LOD_SCOPE_EXTREMES != 0)
		{
			long start = System.currentTimeMillis();

			handleLodAtScale(charX, charY, 1, 4, null);
			handleLodAtScale(charX, charY, 4, 8, loadedGrosses4);
			handleLodAtScale(charX, charY, 8, 16, loadedGrosses8);
			handleLodAtScale(charX, charY, 16, 32, loadedGrosses16);
			handleLodAtScale(charX, charY, 32, LOD_SCOPE_EXTREMES, loadedGrosses32);

			// now to tell each 4 that's loaded to update themselves
			Point charPoint = convertCharToLodXY(charX, charY);
			Iterator<Point> keys = loadedGrosses4.keySet().iterator();
			while (keys.hasNext())
			{
				Point key = keys.next();
				if (key.distance(charPoint) <= 64)
				{
					MorphingLandscape oblivLODLandscape = (MorphingLandscape) loadedGrosses4.get(key);
					oblivLODLandscape.updateVisibility(charX, charY);
				}
			}

			if ((System.currentTimeMillis() - start) > 50)
				System.out.println("Beth32_4LodManager.updateGross in " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	int prevXmin = 0;

	int prevYmin = 0;

	int prevXmax = 0;

	int prevYmax = 0;

	private void handleLodAtScale(float charX, float charY, int scale, int nextScale, HashMap<Point, BranchGroup> store)
	{
		Point charLodXY = convertCharToLodXY(charX, charY);
		//	System.out.println("Scale " + scale);
		//	System.out.println("charLodXY " + charLodXY);

		if (scale == 1)
		{
			//TODO: this nearLoad is important, basically near load min
			int nearLoad = 0;//2;
			prevXmin = charLodXY.x - nearLoad;
			prevYmin = charLodXY.y - nearLoad;
			prevXmax = charLodXY.x + nearLoad;
			prevYmax = charLodXY.y + nearLoad;
		}

		ArrayList<Integer> toAttachX = new ArrayList<Integer>();
		ArrayList<Integer> toAttachY = new ArrayList<Integer>();

		//for X then for Y
		//get lower from previous
		prevXmin = doAxisDown(prevXmin, scale, nextScale, toAttachX);
		prevYmin = doAxisDown(prevYmin, scale, nextScale, toAttachY);

		//get upper from previous
		prevXmax = doAxisUp(prevXmax, scale, nextScale, toAttachX);
		prevYmax = doAxisUp(prevYmax, scale, nextScale, toAttachY);

		//TODO: debug a like? should do actual nears too?
		if (store != null)
		{
			HashSet<Point> pointsToAttach = new HashSet<Point>();

			//  2 x's and 2 y's will only result in 4 points
			// but in fact it should be the entire square's edge tiles
			int minX = 999;
			int maxX = -999;
			for (Integer x : toAttachX)
			{
				minX = x < minX ? x : minX;
				maxX = x > maxX ? x : maxX;
			}
			int minY = 999;
			int maxY = -999;
			for (Integer y : toAttachY)
			{

				minY = y < minY ? y : minY;
				maxY = y > maxY ? y : maxY;
			}

			for (int x = minX; x <= maxX; x += scale)
			{
				for (int y = minY; y <= maxY; y += scale)
				{
					if (toAttachX.contains(x) || toAttachY.contains(y))
					{
						Point key = new Point(x, y);

						if (Math.abs(key.x - charLodXY.x) < BethRenderSettings.getLOD_LOAD_DIST_MAX()
								&& Math.abs(key.y - charLodXY.y) < BethRenderSettings.getLOD_LOAD_DIST_MAX())
						{
							pointsToAttach.add(key);
						}
					}
				}
			}

			//detach those now not in
			for (Point key : store.keySet())
			{
				if (!pointsToAttach.contains(key))
				{
					BranchGroup lod = store.get(key);
					if (lod != null && lod.getParent() != null)
					{
						//System.out.println("Removed lod level" + scale + " at " + key);
						lod.detach();
					}
				}
			}
			// and attach all the others
			for (Point key : pointsToAttach)
			{
				BranchGroup lod = store.get(key);
				if (lod == null)
				{
					lod = j3dCellFactory.makeLODLandscape(key.x, key.y, scale, lodWorldName);
					lod.setCapability(Node.ALLOW_PARENT_READ);
					store.put(key, lod);
				}

				//attach if not yet attached
				if (lod.getParent() == null)
				{
					//System.out.println("Added lod level" + scale + " at " + key);
					lod.compile();// better to be done not on the j3d thread?
					addChild(lod);
				}
			}
		}

	}

	private static int doAxisDown(int startValue, int scale, int nextScale, ArrayList<Integer> toAttach)
	{
		//sub1 (scale)
		//add it
		//is that next scale up border?
		//yes stop
		//no then repeat
		//mark next scale up
		int val = startValue;
		while (true)
		{
			val = val - scale;
			toAttach.add(val);
			if (val % nextScale == 0)
				return val;
		}
	}

	private static int doAxisUp(int startValue, int scale, int nextScale, ArrayList<Integer> toAttach)
	{
		//add1 (scale)
		//add it
		//no border check
		//add1 (scale)
		//is that border
		//yes stop
		//no then repeat
		//mark for next scale up
		int val = startValue;

		while (true)
		{
			toAttach.add(val);
			val = val + scale;
			if (val % nextScale == 0)
				return val;
		}
	}

	public Point convertCharToLodXY(float charX, float charY)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);
		return new Point(charLodX, charLodY);
	}

	@Override
	public Rectangle getGridBounds(float charX, float charY, int nearLoadGridCount)
	{
		// the 4x4 limit removed, morph like oblivion

		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		charLodX -= nearLoadGridCount;
		//		while (charLodX % 4 != 0)
		//			charLodX--;
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);
		charLodY -= nearLoadGridCount;
		//		while (charLodY % 4 != 0)
		//			charLodY--;
		int w = (nearLoadGridCount * 2) + 1;
		//		while (w % 4 != 0)
		//			w++;
		int h = (nearLoadGridCount * 2) + 1;
		//		while (h % 4 != 0)
		//			h++;

		//System.out.println("near is "+ new Rectangle(charLodX, charLodY, w - 1, h - 1));

		//because teh mod check allow for getting to the mod value and we wnat one less
		//	return new Rectangle(charLodX, charLodY, w - 1, h - 1);
		return new Rectangle(charLodX, charLodY, w, h);
	}

}
