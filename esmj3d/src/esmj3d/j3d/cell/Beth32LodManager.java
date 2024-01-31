package esmj3d.j3d.cell;

import java.util.HashMap;
import java.util.Iterator;

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

public class Beth32LodManager extends BethLodManager
{
	private static int OBLIVION_MIN_LOD = -96;

	private static int OBLIVION_MAX_LOD = 96; //NOTE < only never = nothing is 96!

	private static int SCALE_32 = 32;

	private HashMap<Point, MorphingLandscape> loadedGrosses = new HashMap<Point, MorphingLandscape>();

	private String lodWorldFormId = "";

	private J3dICellFactory j3dCellFactory;
	
	private LinearFog fog = new LinearFog(new Color3f(0.8f, 0.8f, 0.8f), 500, 3500);

	public Beth32LodManager(J3dICellFactory j3dCellFactory)
	{
		this.j3dCellFactory = j3dCellFactory;
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		//create a hilarious distance fog
		//LinearFog, ExponentialFog
		
		//fog.addScope(this);
		fog.setInfluencingBounds(Utils3D.defaultBounds);
		
		
		this.setName("Beth32LodManager");

	}

	@Override
	public void setWorldFormId(int worldFormId)
	{
		String newLodWorldFormId = j3dCellFactory.getLODWorldName(worldFormId);
		if (!this.lodWorldFormId.equals(newLodWorldFormId))
		{
			this.removeAllChildren();
			addChild(fog);
			this.lodWorldFormId = newLodWorldFormId;

			long start = System.currentTimeMillis();

			for (int x = OBLIVION_MIN_LOD; x < OBLIVION_MAX_LOD; x += SCALE_32)
			{
				for (int y = OBLIVION_MIN_LOD; y < OBLIVION_MAX_LOD; y += SCALE_32)
				{
					Point key = new Point(x, y);
					MorphingLandscape bg = (MorphingLandscape) j3dCellFactory.makeLODLandscape(x, y, SCALE_32, lodWorldFormId);
					bg.setCapability(Node.ALLOW_PARENT_READ);
					loadedGrosses.put(key, bg);
					bg.compile();// better to be done not on the j3d thread?
					//addChild(bg);// don't add yet, updateGross will do so shortly
				}
			}
			if ((System.currentTimeMillis() - start) > 50)
				System.out.println("Beth32LodManager.setWorldFormId in " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	@Override
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
				if (oblivLODLandscape.getParent() == null)
					addChild(oblivLODLandscape);
			}
			else
			{
				if (oblivLODLandscape.getParent() != null)
					removeChild(oblivLODLandscape);
			}
		}
						
		
		// now tell the shaders about the fade dist away from the camera
		//0.4 is the central always loaded near cell, but not all of it so the edge is a bit feathered
		float nearSize = (BethRenderSettings.getNearLoadGridCount()+0.4f)*J3dLAND.LAND_SIZE;
		float cx = (charPoint.x+0.5f)*J3dLAND.LAND_SIZE;//note push to center of cell
		float cy = (charPoint.y+0.5f)*J3dLAND.LAND_SIZE;
		Vector2f minXYRemoval = new Vector2f(cx-nearSize,(cy-nearSize));
		Vector2f maxXYRemoval = new Vector2f(cx+nearSize,(cy+nearSize));
		 
		//System.out.println("minXYRemoval " + minXYRemoval);
		//System.out.println("maxXYRemoval " + maxXYRemoval);
		ShaderAttributeValue sav1 = (ShaderAttributeValue)MorphingLandscape.shaderAttributeSet.get("minXYRemoval");
		if(sav1 != null)
			sav1.setValue(minXYRemoval);
		ShaderAttributeValue sav2 = (ShaderAttributeValue)MorphingLandscape.shaderAttributeSet.get("maxXYRemoval");
		if(sav2 != null)
			sav2.setValue(maxXYRemoval);

		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("Beth32LodManager.updateGross in " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * 4 ints represent the near grid x,y locations (not floats of real points)
	 * @return
	 */
	@Override
	public Rectangle getGridBounds(float charX, float charY)
	{
		return getBounds(charX, charY, this.nearGridLoadCount);
	}

	
}
