package esmj3d.j3d;

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import esmj3d.data.shared.records.InstRECO;

import utils.ESConfig;

public class J3dUtils
{
	public static TransformGroup makeTransform(float x, float y, float z, float rx, float ry, float rz, float scale, Node lowerNode)
	{
		TransformGroup transformGroup = new TransformGroup();
		Transform3D transform = new Transform3D();

		Transform3D xrotT = new Transform3D();
		xrotT.rotX(-rx);
		Transform3D zrotT = new Transform3D();
		zrotT.rotZ(ry);
		Transform3D yrotT = new Transform3D();
		yrotT.rotY(-rz);

		xrotT.mul(zrotT);
		xrotT.mul(yrotT);

		transform.set(xrotT);

		transform.setTranslation(new Vector3f(x * ESConfig.ES_TO_METERS_SCALE, z * ESConfig.ES_TO_METERS_SCALE, -y
				* ESConfig.ES_TO_METERS_SCALE));

		transform.setScale(scale);

		transformGroup.setTransform(transform);

		transformGroup.addChild(lowerNode);
		return transformGroup;
	}

	public static Transform3D setTransform(InstRECO reco, Transform3D transform)
	{

		Transform3D xrotT = new Transform3D();
		xrotT.rotX(-reco.rx);
		Transform3D zrotT = new Transform3D();
		zrotT.rotZ(reco.ry);
		Transform3D yrotT = new Transform3D();
		yrotT.rotY(-reco.rz);

		xrotT.mul(zrotT);
		xrotT.mul(yrotT);

		transform.set(xrotT);

		transform.setTranslation(new Vector3f(reco.x * ESConfig.ES_TO_METERS_SCALE, reco.z * ESConfig.ES_TO_METERS_SCALE, -reco.y
				* ESConfig.ES_TO_METERS_SCALE));

		transform.setScale(reco.scale);

		return transform;
	}
}
