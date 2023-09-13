package esmj3d.j3d;

import tools.WeakListenerList;

public class BethRenderSettings
{
	private static WeakListenerList<UpdateListener> updateListeners = new WeakListenerList<UpdateListener>();

	public static final int ACTOR_FADE_MAX = 500;

	public static final int ITEM_FADE_MAX = 1000;

	public static final int OBJECT_FADE_MAX = 1000;

	public static final int ACTOR_FADE_DEFAULT = 50;

	public static final int ITEM_FADE_DEFAULT = 200;

	public static final int OBJECT_FADE_DEFAULT = 200;
	
	public static final int FOG_DIST_MIN = 100;
	
	public static final int FOG_DIST_DEFAULT = 500;

	public static final float GLOBAL_AMB_LIGHT_LEVEL_DEFAULT = 0.5f;

	public static final float GLOBAL_DIR_LIGHT_LEVEL_DEFAULT = 0.75f;

	private static int FAR_LOAD_GRID_COUNT = 8;// int in number of cells (82 meters each)

	private static int NEAR_LOAD_GRID_COUNT = 2;// int in number of cells (82 meters each)

	private static int LOD_LOAD_DIST_MAX = 64;//in grids (82 meters)

	private static int actorFade = ACTOR_FADE_DEFAULT;//in meters

	private static int itemFade = ITEM_FADE_DEFAULT;//in meters

	private static int objectFade = OBJECT_FADE_DEFAULT;//in meters
	
	private static int fogDist = FOG_DIST_DEFAULT;//in meters

	private static float globalAmbLightLevel = GLOBAL_AMB_LIGHT_LEVEL_DEFAULT;

	private static float globalDirLightLevel = GLOBAL_DIR_LIGHT_LEVEL_DEFAULT;

	private static boolean enablePlacedLights = true;

	private static boolean showPhysics = false;

	private static boolean flipParentEnableDefault = false;

	private static boolean showEditorMarkers = false;

	private static boolean showDistantBuildings = true;

	private static boolean showDistantTrees = true;

	private static boolean isTes3 = false;

	private static boolean outlineLights = false;
	
	private static boolean outlineChars = false;

	private static boolean outlineDoors = false;

	private static boolean outlineConts = false;

	private static boolean outlineParts = false;

	private static boolean outlineFocused = true;

	private static boolean isShowPathGrid = false;

	private static boolean isFogEnabled = true;

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

	public static void setFogDist(int fogDist) {
		BethRenderSettings.fogDist = fogDist;
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
	
	public static int getFogDist() {
		return fogDist;
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
		// no fire updates as not possible after init
	}

	public static boolean isOutlineChars()
	{
		return outlineChars;
	}

	public static void setOutlineChars(boolean outlineChars)
	{
		BethRenderSettings.outlineChars = outlineChars;
		fireUpdate();
	}

	public static boolean isOutlineDoors()
	{
		return outlineDoors;
	}

	public static void setOutlineDoors(boolean outlineDoors)
	{
		BethRenderSettings.outlineDoors = outlineDoors;
		fireUpdate();
	}

	public static boolean isOutlineConts()
	{
		return outlineConts;
	}

	public static void setOutlineConts(boolean outlineConts)
	{
		BethRenderSettings.outlineConts = outlineConts;
		fireUpdate();
	}

	public static boolean isOutlineParts()
	{
		return outlineParts;
	}

	public static void setOutlineParts(boolean outlineParts)
	{
		BethRenderSettings.outlineParts = outlineParts;
		fireUpdate();
	}

	public static boolean isOutlineFocused()
	{
		return outlineFocused;
	}

	public static void setOutlineFocused(boolean outlineFocused)
	{
		BethRenderSettings.outlineFocused = outlineFocused;
		fireUpdate();
	}

	public static boolean isEnablePlacedLights()
	{
		return enablePlacedLights;
	}
	
	public static boolean isOutlineLights()
	{
		return outlineLights;
	}

	public static void setOutlineLights(boolean outlineLights)
	{
		BethRenderSettings.outlineLights = outlineLights;
		fireUpdate();
	}

	public static void setEnablePlacedLights(boolean enablePlacedLights)
	{
		BethRenderSettings.enablePlacedLights = enablePlacedLights;
		fireUpdate();
	}

	public static float getGlobalAmbLightLevel()
	{
		return globalAmbLightLevel;
	}

	public static void setGlobalAmbLightLevel(float globalAmbLightLevel)
	{
		BethRenderSettings.globalAmbLightLevel = globalAmbLightLevel;
		fireUpdate();
	}

	public static float getGlobalDirLightLevel()
	{
		return globalDirLightLevel;
	}

	public static void setGlobalDirLightLevel(float globalDirLightLevel)
	{
		BethRenderSettings.globalDirLightLevel = globalDirLightLevel;
		fireUpdate();
	}

	public static boolean isFlipParentEnableDefault()
	{
		return flipParentEnableDefault;
	}

	public static void setFlipParentEnableDefault(boolean flipParentEnableDefault)
	{
		BethRenderSettings.flipParentEnableDefault = flipParentEnableDefault;
		fireUpdate();
	}

	public static boolean isShowPathGrid()
	{
		return isShowPathGrid;
	}

	public static void setShowPathGrid(boolean isShowPathGrid)
	{
		BethRenderSettings.isShowPathGrid = isShowPathGrid;
		fireUpdate();
	}

	public static boolean isFogEnabled()
	{
		return isFogEnabled;
	}

	public static void setFogEnabled(boolean isFogEnabled)
	{
		BethRenderSettings.isFogEnabled = isFogEnabled;
		fireUpdate();
	}

}
