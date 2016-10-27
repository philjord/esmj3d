package esmj3d.ai;

import org.jogamp.vecmath.Vector3f;

import esmj3d.physics.PhysicsSystemInterface;

public interface AIThinker
{
	public void think(Vector3f charLocation, PathGridInterface pgi, PhysicsSystemInterface clientPhysicsSystem);
}
