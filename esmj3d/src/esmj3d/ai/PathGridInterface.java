package esmj3d.ai;

import org.jogamp.vecmath.Vector3f;

public interface PathGridInterface
{
	public Vector3f getNearestNode(Vector3f from);

	public PathGridPathway getPathway(Vector3f from);
}
