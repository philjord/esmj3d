package esmj3d.j3d.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.LinearFog;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector2f;

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

	private int worldFormId = -1;
	
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
		this.worldFormId  = worldFormId;
		String newLodWorldName = j3dCellFactory.getLODWorldName(worldFormId);

		if (!this.lodWorldName.equals(newLodWorldName))
		{
			this.removeAllChildren();
			this.lodWorldName = newLodWorldName;
		}
	}

	private Point prevCharLodPoint = new Point (-999,-999);
	@Override
	public void updateGross(float charX, float charY)
	{
		if (LOD_SCOPE_EXTREMES != 0)
		{						
			Point charLodPoint = new Point((int) Math.floor(charX / J3dLAND.LAND_SIZE), (int) Math.floor(charY / J3dLAND.LAND_SIZE));
			
			if(!prevCharLodPoint.equals(charLodPoint)) {
				long start = System.currentTimeMillis();
				handleLodAtScale(charX, charY, 1, 4, null);
				handleLodAtScale(charX, charY, 4, 8, loadedGrosses4);
				handleLodAtScale(charX, charY, 8, 16, loadedGrosses8);
				handleLodAtScale(charX, charY, 16, 32, loadedGrosses16);
				handleLodAtScale(charX, charY, 32, LOD_SCOPE_EXTREMES, loadedGrosses32);
				
				// now tell the shaders about the fade dist away from the camera
				//0.4 is the central always loaded near cell, but not all of it so the edge is a bit feathered
				float nearSize = (BethRenderSettings.getNearLoadGridCount()+0.4f)*J3dLAND.LAND_SIZE;
				float cx = (charLodPoint.x+0.5f)*J3dLAND.LAND_SIZE;//note push to center of cell
				float cy = (charLodPoint.y+0.5f)*J3dLAND.LAND_SIZE;
				Vector2f minXYRemoval = new Vector2f(cx-nearSize,(cy-nearSize));
				Vector2f maxXYRemoval = new Vector2f(cx+nearSize,(cy+nearSize));
				 
	//			System.out.println("minXYRemoval " + minXYRemoval);
	//			System.out.println("maxXYRemoval " + maxXYRemoval);
				ShaderAttributeValue sav1 = (ShaderAttributeValue)MorphingLandscape.shaderAttributeSet.get("minXYRemoval");
				if(sav1 != null)
					sav1.setValue(minXYRemoval);
				ShaderAttributeValue sav2 = (ShaderAttributeValue)MorphingLandscape.shaderAttributeSet.get("maxXYRemoval");
				if(sav2 != null)
					sav2.setValue(maxXYRemoval);
				
				
				prevCharLodPoint = charLodPoint;
				
				if ((System.currentTimeMillis() - start) > 50)
					System.out.println("Beth32_4LodManager.updateGross in " + (System.currentTimeMillis() - start) + "ms");
			}
		}
	}

	int prevXmin = 0;

	int prevYmin = 0;

	int prevXmax = 0;

	int prevYmax = 0;

	private void handleLodAtScale(float charX, float charY, int scale, int nextScale, HashMap<Point, BranchGroup> store) {
		
		Point charLodXY = convertCharToLodXY(charX, charY);
		//System.out.println("Scale " + scale + "  next " + nextScale);
		//System.out.println("charLodXY " + charLodXY);
		
		int nearLods = BethRenderSettings.getNearLoadGridCount();
		if (scale == 1)	{
			// For scale 1 just give enough room to fit the nears and exit (note it a bit less than enough room)
			prevXmin = charLodXY.x - nearLods;
			prevYmin = charLodXY.y - nearLods;
			prevXmax = charLodXY.x + nearLods;
			prevYmax = charLodXY.y + nearLods;
			prevXmin = prevXmin - (prevXmin%nextScale) + nextScale;
			prevYmin = prevYmin - (prevYmin%nextScale) + nextScale;
			prevXmax = prevXmax - (prevXmax%nextScale);
			prevYmax = prevYmax - (prevYmax%nextScale);
			return;
		}
			

		ArrayList<Integer> toAttachX = new ArrayList<Integer>();
		ArrayList<Integer> toAttachY = new ArrayList<Integer>();

		//for X then for Y
		//get lower from previous
		int newXmin = doAxis(prevXmin, -scale, nextScale, toAttachX);
		int newYmin = doAxis(prevYmin, -scale, nextScale, toAttachY);

		//get upper from previous
		int newXmax = doAxis(prevXmax, scale, nextScale, toAttachX);
		int newYmax = doAxis(prevYmax, scale, nextScale, toAttachY);
				
		//System.out.println("prevXYmin " +prevXmin+" "+ prevYmin);
		//System.out.println("prevXYmax " +prevXmax+" "+ prevYmax);
		
		//System.out.println("newXYmin " +newXmin+" "+ newYmin);
		//System.out.println("newXYmax " +newXmax+" "+ newYmax);	
		

		if (store != null) {
			
			HashSet<Point> pointsToAttach = new HashSet<Point>();			
			
			for (int x = newXmin; x <= newXmax; x += scale) {
				for (int y = newYmin; y <= newYmax; y += scale) {
						
					if (!(x>=prevXmin&&y>=prevYmin&&x<prevXmax&&y<prevYmax)){
						Point key = new Point(x, y);

						if (Math.abs(key.x - charLodXY.x) < BethRenderSettings.getLOD_LOAD_DIST_MAX()
								&& Math.abs(key.y - charLodXY.y) < BethRenderSettings.getLOD_LOAD_DIST_MAX()) {
							pointsToAttach.add(key);
						}
					}
				}
			}

			//detach those now not in
			for (Point key : store.keySet()) {
				if (!pointsToAttach.contains(key)) {
					BranchGroup lod = store.get(key);
					if (lod != null && lod.getParent() != null) {
						//System.out.println("Removed lod level" + scale + " at " + key);
						lod.detach();
					}
				}
			}
			// and attach all the others
			for (Point key : pointsToAttach) {
				BranchGroup lod = store.get(key);
				if (lod == null) {
					lod = j3dCellFactory.makeLODLandscape(worldFormId, key.x, key.y, scale, lodWorldName);
					lod.setCapability(Node.ALLOW_PARENT_READ);
					store.put(key, lod);
				}

				//attach if not yet attached
				if (lod.getParent() == null) {
					//System.out.println("Added lod level" + scale + " at " + key);
					lod.compile();// better to be done not on the j3d thread?
					
					addChild(lod);
				}
			}
		}
				
		prevXmin = newXmin;
		prevYmin = newYmin;
		prevXmax = newXmax;
		prevYmax = newYmax;
	}
	
	private static int doAxis(int startValue, int scale, int nextScale, ArrayList<Integer> toAttach)
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
			val = val + scale;
			toAttach.add(val);
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
	public Rectangle getGridBounds(float charX, float charY)
	{
		int charLodX = (int) Math.floor(charX / J3dLAND.LAND_SIZE);
		charLodX -= nearGridLoadCount;
		int charLodY = (int) Math.floor(charY / J3dLAND.LAND_SIZE);
		charLodY -= nearGridLoadCount;
		int w = (nearGridLoadCount * 2) + 1;
		int h = (nearGridLoadCount * 2) + 1;

		return new Rectangle(charLodX, charLodY, w, h);
	}	 
}
