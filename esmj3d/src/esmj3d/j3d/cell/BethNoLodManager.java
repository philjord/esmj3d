package esmj3d.j3d.cell;

import javax.vecmath.Color3f;

import org.jogamp.java3d.LinearFog;

import javaawt.Rectangle;
import tools3d.utils.Utils3D;

public class BethNoLodManager extends BethLodManager
{
	//TODO: can't I do something!!!!
	public BethNoLodManager(J3dICellFactory j3dCellFactory)
	{
		//create a hillarious distance fog
		//LinearFog, ExponentialFog
		LinearFog fog = new LinearFog(new Color3f(0.8f, 0.8f, 0.8f), 500, 3500);
		fog.addScope(this);
		fog.setInfluencingBounds(Utils3D.defaultBounds);
		addChild(fog);
	}

	@Override
	public void setWorldFormId(int worldFormId)
	{

	}

	@Override
	public void updateGross(float charX, float charY)
	{
	}

	@Override
	public Rectangle getGridBounds(float charX, float charY, int nearLoadGridCount)
	{
		return Beth32LodManager.getBounds(charX, charY, nearLoadGridCount);
	}
}
