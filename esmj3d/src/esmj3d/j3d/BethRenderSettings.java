package esmj3d.j3d;

import tools.WeakListenerList;

public class BethRenderSettings
{
	public static final int ACTOR_FADE_MAX = 100;

	public static final int ITEM_FADE_MAX = 400;

	public static final int OBJECT_FADE_MAX = 400;

	public static final int ACTOR_FADE_DEFAULT = 50;

	public static final int ITEM_FADE_DEFAULT = 100;

	public static final int OBJECT_FADE_DEFAULT = 100;

	private static int FAR_LOAD_GRID_COUNT = 4;// int in number of cells (82 meters each)

	private static int NEAR_LOAD_GRID_COUNT = 2;// int in number of cells (82 meters each)

	private static float CHAR_MOVE_UPDATE_DIST = 2f;//in meters

	private static int actorFade = ACTOR_FADE_DEFAULT;//in meters

	private static int itemFade = ITEM_FADE_DEFAULT;//in meters

	private static int objectFade = OBJECT_FADE_DEFAULT;//in meters

	private static boolean showPhysics = true;

	private static boolean showEditorMarkers = true;

	private static boolean showDistantBuildings = true;

	private static boolean showDistantTrees = true;

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

	public static void setCHAR_MOVE_UPDATE_DIST(float f)
	{
		CHAR_MOVE_UPDATE_DIST = f;
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
		return FAR_LOAD_GRID_COUNT;
	}

	public static int getNearLoadGridCount()
	{
		return NEAR_LOAD_GRID_COUNT;
	}

	public static float getCHAR_MOVE_UPDATE_DIST()
	{
		return CHAR_MOVE_UPDATE_DIST;
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

}
