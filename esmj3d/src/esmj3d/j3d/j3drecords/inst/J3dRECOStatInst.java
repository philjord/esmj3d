package esmj3d.j3d.j3drecords.inst;

import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.ColorCube;

import tools3d.utils.Utils3D;
import tools3d.utils.scenegraph.BetterDistanceLOD;
import utils.ESConfig;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class J3dRECOStatInst extends Group implements J3dRECOInst
{
	public static boolean SHOW_FADE_OUT_MARKER = false;

	private boolean fader = false;

	private boolean makePhys = true;

	private BetterDistanceLOD dl;

	private ArrayList<BranchGroup> myNodes = new ArrayList<BranchGroup>();

	protected TransformGroup transformGroup = new TransformGroup();

	protected J3dRECOType j3dRECOType;

	private InstRECO instRECO = null;

	public J3dRECOStatInst(InstRECO instRECO, boolean makePhys)
	{
		this(instRECO, true, makePhys);
	}

	public J3dRECOStatInst(InstRECO instRECO, boolean enableSimpleFade, boolean makePhys)
	{
		this.fader = enableSimpleFade && !makePhys;// no fader ever for phys
		this.makePhys = makePhys;
		super.addChild(transformGroup);//Note must use super here

		setLocation(instRECO);
		this.instRECO = instRECO;
	}

	@Override
	public void renderSettingsUpdated()
	{
		//TODO: different render settings for  different types
		if (fader)
		{
			dl.setDistance(0, BethRenderSettings.getItemFade());
		}
		if (j3dRECOType != null)
		{
			j3dRECOType.renderSettingsUpdated();
		}
	}

	public void addNodeChild(Node node)
	{
		if (fader)
		{
			throw new UnsupportedOperationException("addNodeChild with fader! " + this);
		}
		else
		{
			transformGroup.addChild(node);
		}
	}

	public void setJ3dRECOType(J3dRECOType j3dRECOType)
	{
		BranchGroup bg = new BranchGroup();// empty group for no rendering
		bg.addChild(SHOW_FADE_OUT_MARKER ? new ColorCube(0.1) : new BranchGroup());
		setJ3dRECOType(j3dRECOType, bg);
	}

	public void setJ3dRECOType(J3dRECOType j3dRECOType, BranchGroup j3dRECOTypeFar)
	{
		this.j3dRECOType = j3dRECOType;
		if (fader && !makePhys)
		{
			myNodes.add(j3dRECOType);
			myNodes.add(j3dRECOTypeFar);
			Group parent = new Group();
			transformGroup.addChild(parent);
			dl = new BetterDistanceLOD(parent, myNodes, new float[]
			{ BethRenderSettings.getItemFade() });
			transformGroup.addChild(dl);//Note must use super here
			dl.setSchedulingBounds(Utils3D.defaultBounds);
			dl.setEnable(true);

		}
		else
		{
			transformGroup.addChild(j3dRECOType);
		}
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

	public J3dRECOType getJ3dRECOType()
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
	private void setLocation(float x, float y, float z, float rx, float ry, float rz, float scale)
	{
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
	}

	private void setLocation(InstRECO ir)
	{
		Vector3f t = ir.getTrans();
		Vector3f er = ir.getEulerRot();
		setLocation(t.x, t.y, t.z, er.x, er.y, er.z, ir.getScale());
	}

	/**
	 * NOTE MUSt be pre * ESConfig.ES_TO_METERS_SCALE multiplied
	 * @param loc
	 */
	public void setLocation(Vector3f loc)
	{
		Transform3D transform = new Transform3D();
		transform.setTranslation(loc);
		transformGroup.setTransform(transform);
	}

	@Override
	public Transform3D getLocation(Transform3D out)
	{
		transformGroup.getTransform(out);
		return out;
	}

}
