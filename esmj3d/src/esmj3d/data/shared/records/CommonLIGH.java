package esmj3d.data.shared.records;

import org.jogamp.vecmath.Color3f;

import esfilemanager.common.data.record.Record;
import esmj3d.data.shared.subrecords.MODL;

public abstract class CommonLIGH extends RECO
{

	public MODL MODL = null;

	public Color3f color = new Color3f(255, 255, 255);// white by esm system of 255

	public float radius;

	public float fade = 0;

	public float falloffExponent = 0;

	public float fieldOfView = -1f;

	public CommonLIGH(Record recordData)
	{
		super(recordData);
	}

}
