package esmj3d.j3d.cell;

import javax.vecmath.Vector3f;

import esmj3d.ai.AIActor;
import esmj3d.j3d.j3drecords.inst.J3dRECOChaInst;
import tools3d.utils.YawPitch;

public interface AIActorServices
{
	public void setLocationForActor(AIActor aiActor, Vector3f location, YawPitch yawPitch);

	public J3dRECOChaInst getVisualActor(AIActor aiActor);

	public J3dRECOChaInst getPhysicalActor(AIActor aiActor);

}
