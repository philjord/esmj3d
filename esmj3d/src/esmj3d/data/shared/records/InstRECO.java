package esmj3d.data.shared.records;

import javax.vecmath.Vector3f;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;

public abstract class InstRECO extends RECO
{
	//Note: Divide X,Y by 4096 to get cell location
	protected float x = 0;

	protected float y = 0;

	protected float z = 0;

	protected float rx = 0;

	protected float ry = 0;

	protected float rz = 0;

	protected float scale = 1f;

	public InstRECO(Record record)
	{
		super(record);
	}

	protected void extractInstData(byte[] bs)
	{
		x = ESMByteConvert.extractFloat(bs, 0);
		y = ESMByteConvert.extractFloat(bs, 4);
		z = ESMByteConvert.extractFloat(bs, 8);
		rx = ESMByteConvert.extractFloat(bs, 12);
		ry = ESMByteConvert.extractFloat(bs, 16);
		rz = ESMByteConvert.extractFloat(bs, 20);
	}

	public Vector3f getTrans()
	{
		return getTrans(new Vector3f());
	}

	public Vector3f getTrans(Vector3f v)
	{
		if (v == null)
			v = new Vector3f();

		v.set(x, y, z);
		return v;
	}

	public Vector3f getEulerRot()
	{
		return getEulerRot(new Vector3f());
	}

	public Vector3f getEulerRot(Vector3f er)
	{
		if (er == null)
			er = new Vector3f();

		er.set(rx, ry, rz);
		return er;
	}

	public float getScale()
	{
		return scale;
	}
}
