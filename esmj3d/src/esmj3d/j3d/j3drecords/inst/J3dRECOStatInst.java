package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.DistanceLOD;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import utils.ESConfig;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class J3dRECOStatInst extends Group implements J3dRECOInst
{
	private boolean fader = true;

	private boolean makePhys = true;

	private Switch sw;

	private DistanceLOD dl;

	protected TransformGroup transformGroup = new TransformGroup();

	protected J3dRECOType j3dRECOType;

	private InstRECO instRECO = null;

	public J3dRECOStatInst(InstRECO instRECO, boolean makePhys)
	{
		this(instRECO, true, makePhys);
	}

	public J3dRECOStatInst(InstRECO instRECO, boolean _fader, boolean makePhys)
	{
		this.fader = _fader;
		this.makePhys = makePhys;
		super.addChild(transformGroup);//Note must use super here

		setLocation(instRECO);
		this.instRECO = instRECO;

		//TODO: I'm mixing faders and lod'er as one concept, change variable name?
		//TODO: this fixes teh missing stats issue sometimes, 
		//I've seen badly placed physics so perhaps some of it is a missing transfrom
		// but I've seen no non phys visuals? though on the side of high hrothgar some crazy stuff
		fader = false;
		if (fader && !makePhys)
		{
			sw = new Switch(0);
			dl = new DistanceLOD(new float[]
			{ BethRenderSettings.getItemFade() });
			sw.setCapability(Switch.ALLOW_SWITCH_WRITE);
			transformGroup.addChild(sw);
			dl.addSwitch(sw);
			transformGroup.addChild(dl);
			dl.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
			dl.setEnable(true);
		}

	}

	@Override
	public void renderSettingsUpdated()
	{
		//TODO: different render settings for  different types
		if (fader && !makePhys)
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
		if (fader && !makePhys)
		{
			sw.addChild(node);
		}
		else
		{
			transformGroup.addChild(node);
		}
	}

	public void setJ3dRECOType(J3dRECOType j3dRECOType)
	{
		setJ3dRECOType(j3dRECOType, new Group());// empty group for no rendering
	}

	public void setJ3dRECOType(J3dRECOType j3dRECOType, Group j3dRECOTypeFar)
	{
		this.j3dRECOType = j3dRECOType;
		if (fader && !makePhys)
		{
			sw.addChild(j3dRECOType);
			sw.addChild(j3dRECOTypeFar);
			sw.setWhichChild(1);
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
