
package esmj3d.physics;

import org.jogamp.vecmath.Vector3f;

public interface PhysicsSystemInterface
{
	public RayIntersectResult findRayIntersectResult(Vector3f rayFrom, Vector3f rayTo);
	
	/**
	 * Used to allow ray trace to the ground without touching the collision box of the character themselves
	 */
	public RayIntersectResult findRayIntersectResult(Vector3f rayFrom, Vector3f rayTo, int characterRecordIdToIgnore);
}
