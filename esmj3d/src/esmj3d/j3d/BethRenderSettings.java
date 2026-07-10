package esmj3d.j3d;

import tools.WeakListenerList;

public class BethRenderSettings {
	private static WeakListenerList<UpdateListener>	updateListeners					= new WeakListenerList<UpdateListener>();

	public static final int							ACTOR_FADE_MAX					= 500;

	public static final int							ITEM_FADE_MAX					= 1000;

	public static final int							OBJECT_FADE_MAX					= 1000;

	public static final int							ACTOR_FADE_DEFAULT				= 50;

	public static final int							ITEM_FADE_DEFAULT				= 200;

	public static final int							OBJECT_FADE_DEFAULT				= 200;

	public static final int							FOG_DIST_MIN					= 100;

	public static final int							FOG_DIST_DEFAULT				= 500;

	public static final float						GLOBAL_AMB_LIGHT_LEVEL_DEFAULT	= 0.5f;

	public static final float						GLOBAL_DIR_LIGHT_LEVEL_DEFAULT	= 0.75f;

	private static int								FAR_LOAD_GRID_COUNT				= 8;										// int in number of cells (82 meters each)

	private static int								NEAR_LOAD_GRID_COUNT			= 2;										// int in number of cells (82 meters each)

	private static int								LOD_LOAD_DIST_MAX				= 64;										//in grids (82 meters)

	private static int								actorFade						= ACTOR_FADE_DEFAULT;						//in meters

	private static int								itemFade						= ITEM_FADE_DEFAULT;						//in meters

	private static int								objectFade						= OBJECT_FADE_DEFAULT;						//in meters

	private static int								fogDist							= FOG_DIST_DEFAULT;							//in meters

	private static float							globalAmbLightLevel				= GLOBAL_AMB_LIGHT_LEVEL_DEFAULT;

	private static float							globalDirLightLevel				= GLOBAL_DIR_LIGHT_LEVEL_DEFAULT;

	private static boolean							enableDirLight					= true;

	private static boolean							enablePlacedLights				= true;

	private static boolean							enableTorchLight				= false;

	private static boolean							showPhysics						= false;

	private static boolean							flipParentEnableDefault			= false;

	private static boolean							showEditorMarkers				= false;

	private static boolean							showDistantBuildings			= true;

	private static boolean							showDistantTrees				= true;

	private static boolean							isTes3							= false;

	private static boolean							outlineLights					= false;

	private static boolean							outlineChars					= false;

	private static boolean							outlineDoors					= false;

	private static boolean							outlineConts					= false;

	private static boolean							outlineParts					= false;

	private static boolean							outlineFocused					= true;

	private static boolean							isShowPathGrid					= false;

	private static boolean							isFogEnabled					= true;

	public static interface UpdateListener {
		public void renderSettingsUpdated();
	}

	public static void addUpdateListener(UpdateListener updateListener) {
		BethRenderSettings.updateListeners.add(updateListener);
	}

	public static void removeUpdateListener(UpdateListener updateListener) {
		BethRenderSettings.updateListeners.remove(updateListener);
	}

	private static void fireUpdate() {
		for (UpdateListener updateListener : updateListeners) {
			updateListener.renderSettingsUpdated();
		}

	}

	public static void setLOD_LOAD_DIST_MAX(int i) {
		System.out.println("BethRenderSettings.LOD_LOAD_DIST_MAX: " + i);
		BethRenderSettings.LOD_LOAD_DIST_MAX = i;
		fireUpdate();
	}

	public static int getLOD_LOAD_DIST_MAX() {
		return LOD_LOAD_DIST_MAX;
	}

	public static void setFarLoadGridCount(int c) {
		System.out.println("BethRenderSettings.FAR_LOAD_GRID_COUNT: " + c);
		BethRenderSettings.FAR_LOAD_GRID_COUNT = c;
		fireUpdate();
	}

	public static int getFarLoadGridCount() {
		return isTes3() ? FAR_LOAD_GRID_COUNT / 2 : FAR_LOAD_GRID_COUNT;
	}

	public static void setNearLoadGridCount(int c) {
		System.out.println("BethRenderSettings.NEAR_LOAD_GRID_COUNT: " + c);
		BethRenderSettings.NEAR_LOAD_GRID_COUNT = c;
		fireUpdate();
	}

	public static int getNearLoadGridCount() {
		return isTes3() ? NEAR_LOAD_GRID_COUNT / 2 : NEAR_LOAD_GRID_COUNT;
	}

	public static void setActorFade(int actorFadePercent) {
		System.out.println("BethRenderSettings.actorFade: " + actorFadePercent);
		BethRenderSettings.actorFade = actorFadePercent;
		fireUpdate();
	}

	public static int getActorFade() {
		return actorFade;
	}

	public static void setItemFade(int itemFadePercent) {
		System.out.println("BethRenderSettings.itemFade: " + itemFadePercent);
		BethRenderSettings.itemFade = itemFadePercent;
		fireUpdate();
	}

	public static int getItemFade() {
		return itemFade;
	}

	public static void setObjectFade(int objectFadePercent) {
		System.out.println("BethRenderSettings.objectFade: " + objectFadePercent);
		BethRenderSettings.objectFade = objectFadePercent;
		fireUpdate();
	}

	public static int getObjectFade() {
		return objectFade;
	}

	public static void setShowPhysics(boolean showPhysicsTick) {
		System.out.println("BethRenderSettings.showPhysics: " + showPhysicsTick);
		BethRenderSettings.showPhysics = showPhysicsTick;
		fireUpdate();
	}

	public static boolean isShowPhysic() {
		return showPhysics;
	}

	public static void setShowEditorMarkers(boolean showEditorMarkers) {
		System.out.println("BethRenderSettings.showEditorMarkers: " + showEditorMarkers);
		BethRenderSettings.showEditorMarkers = showEditorMarkers;
		fireUpdate();
	}

	public static boolean isShowEditorMarkers() {
		return showEditorMarkers;
	}

	public static void setShowDistantBuildings(boolean showDistantBuildingsTick) {
		System.out.println("BethRenderSettings.showDistantBuildings: " + showDistantBuildingsTick);
		BethRenderSettings.showDistantBuildings = showDistantBuildingsTick;
		fireUpdate();
	}

	public static boolean isShowDistantBuildings() {
		return showDistantBuildings;
	}

	public static void setShowDistantTrees(boolean showDistantTreesTick) {
		System.out.println("BethRenderSettings.showDistantTrees: " + showDistantTreesTick);
		BethRenderSettings.showDistantTrees = showDistantTreesTick;
		fireUpdate();
	}

	public static boolean isShowDistantTrees() {
		return showDistantTrees;
	}

	public static void setFogDist(int fogDist) {
		System.out.println("BethRenderSettings.fogDist: " + fogDist);
		BethRenderSettings.fogDist = fogDist;
		fireUpdate();
	}

	public static int getFogDist() {
		return fogDist;
	}

	/** 
	 * true halves the grid counts returned
	 * 
	 * @param isTes3
	 */
	public static void setTes3(boolean isTes3) {
		System.out.println("BethRenderSettings.isTes3: " + isTes3);
		BethRenderSettings.isTes3 = isTes3;
		// no fire updates as not possible after init
	}

	public static boolean isTes3() {
		return isTes3;
	}

	public static void setOutlineChars(boolean outlineChars) {
		System.out.println("BethRenderSettings.outlineChars: " + outlineChars);
		BethRenderSettings.outlineChars = outlineChars;
		fireUpdate();
	}

	public static boolean isOutlineChars() {
		return outlineChars;
	}

	public static void setOutlineDoors(boolean outlineDoors) {
		System.out.println("BethRenderSettings.outlineDoors: " + outlineDoors);
		BethRenderSettings.outlineDoors = outlineDoors;
		fireUpdate();
	}

	public static boolean isOutlineDoors() {
		return outlineDoors;
	}

	public static void setOutlineConts(boolean outlineConts) {
		System.out.println("BethRenderSettings.outlineConts: " + outlineConts);
		BethRenderSettings.outlineConts = outlineConts;
		fireUpdate();
	}

	public static boolean isOutlineConts() {
		return outlineConts;
	}

	public static void setOutlineParts(boolean outlineParts) {
		System.out.println("BethRenderSettings.outlineParts: " + outlineParts);
		BethRenderSettings.outlineParts = outlineParts;
		fireUpdate();
	}

	public static boolean isOutlineParts() {
		return outlineParts;
	}

	public static void setOutlineFocused(boolean outlineFocused) {
		System.out.println("BethRenderSettings.outlineFocused: " + outlineFocused);
		BethRenderSettings.outlineFocused = outlineFocused;
		fireUpdate();
	}

	public static boolean isOutlineFocused() {
		return outlineFocused;
	}

	public static void setOutlineLights(boolean outlineLights) {
		System.out.println("BethRenderSettings.outlineLights: " + outlineLights);
		BethRenderSettings.outlineLights = outlineLights;
		fireUpdate();
	}

	public static boolean isOutlineLights() {
		return outlineLights;
	}

	public static void setGlobalDirLightEnabled(boolean enableDirLight) {
		System.out.println("BethRenderSettings.enableDirLight: " + enableDirLight);
		BethRenderSettings.enableDirLight = enableDirLight;
		fireUpdate();
	}

	public static boolean isEnableDirLight() {
		return enableDirLight;
	}

	public static void setEnablePlacedLights(boolean enablePlacedLights) {
		System.out.println("BethRenderSettings.enablePlacedLights: " + enablePlacedLights);
		BethRenderSettings.enablePlacedLights = enablePlacedLights;
		fireUpdate();
	}

	public static boolean isEnablePlacedLights() {
		return enablePlacedLights;
	}

	public static void setEnableTorchLight(boolean enableTorchLight) {
		System.out.println("BethRenderSettings.enableTorchLight: " + enableTorchLight);
		BethRenderSettings.enableTorchLight = enableTorchLight;
		fireUpdate();
	}

	public static boolean isEnableTorchLight() {
		return enableTorchLight;
	}

	public static void setGlobalAmbLightLevel(float globalAmbLightLevel) {
		System.out.println("BethRenderSettings.globalAmbLightLevel: " + globalAmbLightLevel);
		BethRenderSettings.globalAmbLightLevel = globalAmbLightLevel;
		fireUpdate();
	}

	public static float getGlobalAmbLightLevel() {
		return globalAmbLightLevel;
	}

	public static void setGlobalDirLightLevel(float globalDirLightLevel) {
		System.out.println("BethRenderSettings.globalDirLightLevel: " + globalDirLightLevel);
		BethRenderSettings.globalDirLightLevel = globalDirLightLevel;
		fireUpdate();
	}

	public static float getGlobalDirLightLevel() {
		return globalDirLightLevel;
	}

	public static void setFlipParentEnableDefault(boolean flipParentEnableDefault) {
		System.out.println("BethRenderSettings.flipParentEnableDefault: " + flipParentEnableDefault);
		BethRenderSettings.flipParentEnableDefault = flipParentEnableDefault;
		fireUpdate();
	}

	public static boolean isShowPathGrid() {
		return isShowPathGrid;
	}

	public static void setShowPathGrid(boolean isShowPathGrid) {
		System.out.println("BethRenderSettings.isShowPathGrid: " + isShowPathGrid);
		BethRenderSettings.isShowPathGrid = isShowPathGrid;
		fireUpdate();
	}

	public static boolean isFlipParentEnableDefault() {
		return flipParentEnableDefault;
	}

	public static void setFogEnabled(boolean isFogEnabled) {
		System.out.println("BethRenderSettings.isFogEnabled: " + isFogEnabled);
		BethRenderSettings.isFogEnabled = isFogEnabled;
		fireUpdate();
	}

	public static boolean isFogEnabled() {
		return isFogEnabled;
	}
}
