package esmj3d.ai;

import javax.vecmath.Vector3f;

import esmj3d.physics.PhysicsSystemInterface;

public interface AIActor
{
	public int getActorFormId();

	public void act(Vector3f charLocation, PathGridInterface pgi, PhysicsSystemInterface clientPhysicsSystem);

	void setLocation(float x, float y, float z, float yaw, float pitch);
}
