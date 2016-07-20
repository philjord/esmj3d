package esmj3d.j3d.cell;

import javax.vecmath.Vector3f;

import esmj3d.ai.AIActor;
import tools3d.utils.YawPitch;

public interface AIActorLocator
{
	public void setLocationForActor(AIActor aiActor, Vector3f location, YawPitch yawPitch);
}
