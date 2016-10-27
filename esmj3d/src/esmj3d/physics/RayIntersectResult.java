package esmj3d.physics;

import org.jogamp.vecmath.Vector3f;

public class RayIntersectResult
{
	public float closestHitFraction = 1f;
	public final Vector3f hitNormalWorld = new Vector3f();
	public final Vector3f hitPointWorld = new Vector3f();
}
