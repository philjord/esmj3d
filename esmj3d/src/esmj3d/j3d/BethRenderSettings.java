package esmj3d.j3d;

import tools.WeakListenerList;

public class BethRenderSettings
{
	public static final int ACTOR_FADE_MAX = 500;

	public static final int ITEM_FADE_MAX = 1000;

	public static final int OBJECT_FADE_MAX = 1000;

	public static final int ACTOR_FADE_DEFAULT = 50;

	public static final int ITEM_FADE_DEFAULT = 200;

	public static final int OBJECT_FADE_DEFAULT = 200;

	private static int FAR_LOAD_GRID_COUNT = 8;// int in number of cells (82 meters each)

	private static int NEAR_LOAD_GRID_COUNT = 2;// int in number of cells (82 meters each)

	private static int LOD_LOAD_DIST_MAX = 64;//in grids (82 meters)

	private static int actorFade = ACTOR_FADE_DEFAULT;//in meters

	private static int itemFade = ITEM_FADE_DEFAULT;//in meters

	private static int objectFade = OBJECT_FADE_DEFAULT;//in meters

	private static boolean showPhysics = true;

	private static boolean showEditorMarkers = true;

	private static boolean showDistantBuildings = true;

	private static boolean showDistantTrees = true;

	private static boolean isTes3 = false;

	private static WeakListenerList<UpdateListener> updateListeners = new WeakListenerList<UpdateListener>();

	public static interface UpdateListener
	{
		public void renderSettingsUpdated();
	}

	public static void addUpdateListener(UpdateListener updateListener)
	{
		updateListeners.add(updateListener);
	}

	public static void removeUpdateListener(UpdateListener updateListener)
	{
		updateListeners.remove(updateListener);
	}

	private static void fireUpdate()
	{
		for (UpdateListener updateListener : updateListeners)
		{
			updateListener.renderSettingsUpdated();
		}

	}

	public static void setLOD_LOAD_DIST_MAX(int i)
	{
		LOD_LOAD_DIST_MAX = i;
		fireUpdate();
	}

	public static void setFarLoadGridCount(int c)
	{
		FAR_LOAD_GRID_COUNT = c;
		fireUpdate();
	}

	public static void setNearLoadGridCount(int c)
	{
		NEAR_LOAD_GRID_COUNT = c;
		fireUpdate();
	}

	public static void setActorFade(int actorFadePercent)
	{
		BethRenderSettings.actorFade = actorFadePercent;
		fireUpdate();
	}

	public static void setItemFade(int itemFadePercent)
	{
		BethRenderSettings.itemFade = itemFadePercent;
		fireUpdate();
	}

	public static void setObjectFade(int objectFadePercent)
	{
		BethRenderSettings.objectFade = objectFadePercent;
		fireUpdate();
	}

	public static void setShowPhysics(boolean showPhysicsTick)
	{
		BethRenderSettings.showPhysics = showPhysicsTick;
		fireUpdate();
	}

	public static void setShowEditorMarkers(boolean showEditorMarkers)
	{
		BethRenderSettings.showEditorMarkers = showEditorMarkers;
		fireUpdate();
	}

	public static void setShowDistantBuildings(boolean showDistantBuildingsTick)
	{
		BethRenderSettings.showDistantBuildings = showDistantBuildingsTick;
		fireUpdate();
	}

	public static void setShowDistantTrees(boolean showDistantTreesTick)
	{
		BethRenderSettings.showDistantTrees = showDistantTreesTick;
		fireUpdate();
	}

	public static boolean isShowPhysic()
	{
		return showPhysics;
	}

	public static boolean isShowEditorMarkers()
	{
		return showEditorMarkers;
	}

	public static boolean isShowDistantBuildings()
	{
		return showDistantBuildings;
	}

	public static boolean isShowDistantTrees()
	{
		return showDistantTrees;
	}

	public static int getFarLoadGridCount()
	{
		return isTes3() ? FAR_LOAD_GRID_COUNT / 2 : FAR_LOAD_GRID_COUNT;
	}

	public static int getNearLoadGridCount()
	{
		return isTes3() ? NEAR_LOAD_GRID_COUNT / 2 : NEAR_LOAD_GRID_COUNT;
	}

	public static int getLOD_LOAD_DIST_MAX()
	{
		return LOD_LOAD_DIST_MAX;
	}

	public static int getActorFade()
	{
		return actorFade;
	}

	public static int getItemFade()
	{
		return itemFade;
	}

	public static int getObjectFade()
	{
		return objectFade;
	}

	public static boolean isTes3()
	{
		return isTes3;
	}

	/** 
	 * true halves the grid counts returned
	 * 
	 * @param isTes3
	 */
	public static void setTes3(boolean isTes3)
	{
		BethRenderSettings.isTes3 = isTes3;
	}

}
