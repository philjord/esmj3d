package esmj3d.j3d.j3drecords.inst;

import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.type.J3dRECOTypeCha;
import tools3d.utils.Utils3D;
import tools3d.utils.leafnode.Cube;
import tools3d.utils.scenegraph.BetterDistanceLOD;
import utils.ESConfig;

public class J3dRECOChaInst extends BranchGroup implements BethRenderSettings.UpdateListener, J3dRECOInst
{
	public static boolean SHOW_FADE_OUT_MARKER = false;

	private BetterDistanceLOD dl;

	private ArrayList<BranchGroup> myNodes = new ArrayList<BranchGroup>();

	protected TransformGroup transformGroup = new TransformGroup();
	private Transform3D transform = new Transform3D();// for performance

	protected J3dRECOTypeCha j3dRECOType;

	private InstRECO instRECO = null;

	//Notice fader is ALWAYS true and physics is ALWAYS false
	public J3dRECOChaInst(InstRECO instRECO)
	{
		this.instRECO = instRECO;
		this.setCapability(BranchGroup.ALLOW_DETACH);
		transformGroup.clearCapabilities();
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		//MAGIC cut down for performance

		// for syda neen other people
		/*	if( doSkip(new Vector3f(-142, 3, 903)) )return;
			if( doSkip(new Vector3f(-141, 3, 898)) )return;
			if( doSkip(new Vector3f(-144,3,889)) )return;
			if( doSkip(new Vector3f(-145,4,866)) )return;
			if( doSkip(new Vector3f(-145,4,860)) )return;*/

		super.addChild(transformGroup);//Note must use super here

		setLocation(instRECO);

		BethRenderSettings.addUpdateListener(this);

	}

	private boolean doSkip(Vector3f v)
	{
		Vector3f t = instRECO.getTrans();
		t.set(t.x * ESConfig.ES_TO_METERS_SCALE, t.z * ESConfig.ES_TO_METERS_SCALE, -t.y * ESConfig.ES_TO_METERS_SCALE);
		t.sub(v);
		return t.length() < 5;
	}

	@Override
	public InstRECO getInstRECO()
	{
		return instRECO;
	}

	@Override
	public int getRecordId()
	{
		return instRECO.getRecordId();
	}

	@Override
	public void addChild(Node node)
	{
		System.out.println("Oi! no addchild on " + this);
	}

	@Override
	public void renderSettingsUpdated()
	{
		if (dl != null)
		{
			dl.setDistance(0, BethRenderSettings.getActorFade());
		}

		if (j3dRECOType != null)
		{
			j3dRECOType.renderSettingsUpdated();
		}
	}

	public void setJ3dRECOType(J3dRECOTypeCha j3dRECOType)
	{
		this.j3dRECOType = j3dRECOType;

		BranchGroup far = new BranchGroup();// empty group for no rendering
		far.clearCapabilities();
		far.addChild(SHOW_FADE_OUT_MARKER ? new Cube(0.1) : new BranchGroup());

		myNodes.add(j3dRECOType);
		myNodes.add(far);
		Group parent = new Group();
		parent.clearCapabilities();
		transformGroup.addChild(parent);
		dl = new BetterDistanceLOD(parent, myNodes, new float[] { BethRenderSettings.getActorFade() });
		transformGroup.addChild(dl);
		dl.setSchedulingBounds(Utils3D.defaultBounds);
		dl.setEnable(true);
	}

	@Override
	public J3dRECOTypeCha getJ3dRECOType()
	{
		return j3dRECOType;
	}

	/**
	 * Note this MUST be the ESM form with Z up and it will be translated
	 * @param x
	 * @param y
	 * @param z
	 * @param rx
	 * @param ry
	 * @param rz
	 * @param scale
	 */
	public void setLocation(float x, float y, float z, float rx, float ry, float rz, float scale)
	{
		setLocation(toJ3d(x, y, z, rx, ry, rz, scale));
	}

	public void setLocation(Transform3D t)
	{
		transform.set(t);
		transformGroup.setTransform(transform);
	}

	public void setLocation(InstRECO ir)
	{
		Vector3f t = ir.getTrans();
		Vector3f er = ir.getEulerRot();
		setLocation(t.x, t.y, t.z, er.x, er.y, er.z, ir.getScale());
	}

	@Override
	public void setLocation(Vector3f loc, Quat4f rotation)
	{
		transform.setTranslation(loc);
		transformGroup.setTransform(transform);
	}

	@Override
	public Transform3D getLocation(Transform3D out)
	{
		out.set(transform);
		return out;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " from " + instRECO;
	}

	public static Transform3D toJ3d(float x, float y, float z, float rx, float ry, float rz, float scale)
	{
		Transform3D t = new Transform3D();

		Transform3D xrotT = new Transform3D();
		xrotT.rotX(-rx);
		Transform3D zrotT = new Transform3D();
		zrotT.rotZ(ry);
		Transform3D yrotT = new Transform3D();
		yrotT.rotY(-rz);

		xrotT.mul(zrotT);
		xrotT.mul(yrotT);

		t.set(xrotT);

		t.setTranslation(new Vector3f(x * ESConfig.ES_TO_METERS_SCALE, z * ESConfig.ES_TO_METERS_SCALE, -y * ESConfig.ES_TO_METERS_SCALE));

		t.setScale(scale);

		return t;

	}
}
