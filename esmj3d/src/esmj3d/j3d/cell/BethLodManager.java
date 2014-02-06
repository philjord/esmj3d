package esmj3d.j3d.cell;

import javax.media.j3d.BranchGroup;

public abstract class BethLodManager extends BranchGroup
{
	public BethLodManager()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public abstract void updateGross(float charX, float charY);

	public abstract void setWorldFormId(int worldFormId);
}
