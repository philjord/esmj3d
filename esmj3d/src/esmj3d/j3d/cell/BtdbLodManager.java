package esmj3d.j3d.cell;

import java.util.HashMap;
import java.util.HashSet;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.ShaderAttributeSet;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.vecmath.Vector2f;

import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import javaawt.Point;

public class BtdbLodManager extends BethLodManager {

	public static final int				WIDTH_LOD1		= 1;
	public static final int				WIDTH_LOD2		= 2;
	public static final int				WIDTH_LOD3		= 2;
	private HashMap<Point, BranchGroup>	loadedLod4		= new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup>	loadedLod3		= new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup>	loadedLod2		= new HashMap<Point, BranchGroup>();

	private HashMap<Point, BranchGroup>	loadedLod1		= new HashMap<Point, BranchGroup>();

	private int							worldFormId		= -1;

	private String						lodWorldName	= "";

	private J3dICellFactory				j3dCellFactory;

	// statically set the fixed values in the parent
	{
		J3dLAND.LAND_SIZE = 52.0192f; // make it fixed 4096 nif unit per land = this many meters, regardless of grid size
	}

	// to be used by the J3dLANDBtdbFar to let lod4's be morphable (hole able)
	public static ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();

	public BtdbLodManager(J3dICellFactory j3dCellFactory) {
		this.j3dCellFactory = j3dCellFactory;

		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	}

	@Override
	public void setWorldFormId(int worldFormId) {
		this.worldFormId = worldFormId;
		String newLodWorldName = j3dCellFactory.getLODWorldName(worldFormId);

		if (!this.lodWorldName.equals(newLodWorldName)) {
			this.removeAllChildren();
			this.lodWorldName = newLodWorldName;
		}
	}

	@Override
	public void updateGross(float charX, float charY) {
		
		long start = System.currentTimeMillis();
		handleLod(charX, charY, 0, 0, null);// makes a min/max hole for the later
		handleLod(charX, charY, 1, WIDTH_LOD1, loadedLod1);
		handleLod(charX, charY, 2, WIDTH_LOD2, loadedLod2);
		handleLod(charX, charY, 3, WIDTH_LOD3, loadedLod3);
		handleLod(charX, charY, 4, 0, loadedLod4);

		Point charLodPoint = new Point((int)Math.floor(charX / J3dLAND.LAND_SIZE),
				(int)Math.floor(charY / J3dLAND.LAND_SIZE));
		// now tell the shaders about the hole for nears the character
		//0.4 is the central always loaded near cell, but not all of it so the edge is a bit feathered
		float nearSize = (BethRenderSettings.getNearLoadGridCount() + WIDTH_LOD1 + WIDTH_LOD2 + WIDTH_LOD3 + 0.4f)
							* J3dLAND.LAND_SIZE;
		float cx = (charLodPoint.x - 0.0f) * J3dLAND.LAND_SIZE;//note no push to center of cell
		float cy = (charLodPoint.y - 0.0f) * J3dLAND.LAND_SIZE;
		Vector2f minXYRemoval = new Vector2f(cx - nearSize, (cy - nearSize));
		Vector2f maxXYRemoval = new Vector2f(cx + nearSize, (cy + nearSize));

		//			System.out.println("minXYRemoval " + minXYRemoval);
		//			System.out.println("maxXYRemoval " + maxXYRemoval);
		ShaderAttributeValue sav1 = (ShaderAttributeValue)shaderAttributeSet.get("minXYRemoval");
		if (sav1 != null)
			sav1.setValue(minXYRemoval);
		ShaderAttributeValue sav2 = (ShaderAttributeValue)shaderAttributeSet.get("maxXYRemoval");
		if (sav2 != null)
			sav2.setValue(maxXYRemoval);

		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("BtdbLodManager.updateGross in " + (System.currentTimeMillis() - start) + "ms");

	}

	int	prevXmin	= 0;
	int	prevYmin	= 0;
	int	prevXmax	= 0;
	int	prevYmax	= 0;

	private void handleLod(float charX, float charY, int lod, int span, HashMap<Point, BranchGroup> store) {

		Point charLodXY = convertCharToLodXY(charX, charY);
		//System.out.println("lod " + lod + "  span " + span);
		//System.out.println("charLodXY " + charLodXY + " derived from an actual of "+ charX+", "+charY);

		if (lod == 0) {
			int nearLods = BethRenderSettings.getNearLoadGridCount();
			// For lod0 just give enough room to fit the nears and exit
			prevXmin = charLodXY.x - nearLods;
			prevYmin = charLodXY.y - nearLods;
			prevXmax = charLodXY.x + nearLods;
			prevYmax = charLodXY.y + nearLods;
		} else {

			//new system, go from lower -span, to upper + span, but don't add over prev
			int newXmin = prevXmin - span;
			int newYmin = prevYmin - span;
			int newXmax = prevXmax + span;
			int newYmax = prevYmax + span;

			//System.out.println("prevXYmin " + prevXmin + ", " + prevYmin);
			//System.out.println("prevXYmax " + prevXmax + ", " + prevYmax);
			//System.out.println("newXYmin " + newXmin + ", " + newYmin);
			//System.out.println("newXYmax " + newXmax + ", " + newYmax);

			if (store != null) {
				HashSet<Point> pointsToAttach = new HashSet<Point>();

				if (lod < 4) {
					// now we use a step of 1 because my btdb lods are 1 cell big each
					for (int x = newXmin; x <= newXmax; x++) {
						for (int y = newYmin; y <= newYmax; y++) {
							if (!(x >= prevXmin && y >= prevYmin && x <= prevXmax && y <= prevYmax)) {
								Point key = new Point(x, y);

								if (Math.abs(key.x - charLodXY.x) < BethRenderSettings.getLOD_LOAD_DIST_MAX()
									&& Math.abs(key.y - charLodXY.y) < BethRenderSettings.getLOD_LOAD_DIST_MAX()) {
									//System.out.println("adding lod" + lod + " " + key);
									pointsToAttach.add(key);
								}
							}
						}
					}
				} else {
					// lod 4 needs to be aligned to 8s, but if you don't go for 0,8,16,24 then you'll reload the far constantly
					int outer = BethRenderSettings.getLOD_LOAD_DIST_MAX();
					newXmin = charLodXY.x - outer;
					newYmin = charLodXY.y - outer;
					newXmax = charLodXY.x + outer;
					newYmax = charLodXY.y + outer;
					// set bounds, FIXME: these bounds should come from the BtdbReader
					newXmin = newXmin < -100 ? -100 : newXmin;
					newYmin = newYmin < -100 ? -100 : newYmin;
					newXmax = newXmax > 100 ? 100 : newXmax;
					newYmax = newYmax > 100 ? 100 : newYmax;

					//I want to align to 8 so 0-7=0, 8-15=8 etc 
					//using the 0:201 span, not the -100:+100
					newXmin -= ((newXmin + 100) % 8);
					newYmin -= ((newYmin + 100) % 8);
					newXmax -= ((newXmin + 100) % 8);
					newYmax -= ((newXmin + 100) % 8);

					//System.out.println("newXYmin " + newXmin + ", " + newYmin);
					//System.out.println("newXYmax " + newXmax + ", " + newYmax);

					// it's not cool to realign each time as we get a lot of loading, so better to have 
					// all and the shader can chop the hole
					for (int x = newXmin; x < newYmax; x += 8) {
						for (int y = newYmin; y < newYmax; y += 8) {
							Point key = new Point(x, y);
							//System.out.println("adding lod" + lod + " " + key);
							pointsToAttach.add(key);
						}
					}
				}

				//detach those now not in
				for (Point key : store.keySet()) {
					if (!pointsToAttach.contains(key)) {
						BranchGroup lodBG = store.get(key);
						if (lodBG != null && lodBG.getParent() != null) {
							//System.out.println("Removed lod level" + scale + " at " + key);
							lodBG.detach();
						}
					}
				}
				// and attach all the others
				for (Point key : pointsToAttach) {
					BranchGroup lodBG = store.get(key);
					if (lodBG == null) {
						lodBG = j3dCellFactory.makeLODLandscape(worldFormId, key.x, key.y, lod, lodWorldName);
						lodBG.setCapability(Node.ALLOW_PARENT_READ);
						store.put(key, lodBG);
					}

					//attach if not yet attached
					if (lodBG.getParent() == null) {
						//							System.out.println("Added lod level" + scale + " at " + key);
						lodBG.compile();// better to be done not on the j3d thread?
						addChild(lodBG);
					}
				}
			}

			prevXmin = newXmin;
			prevYmin = newYmin;
			prevXmax = newXmax;
			prevYmax = newYmax;
		}
	}

}
