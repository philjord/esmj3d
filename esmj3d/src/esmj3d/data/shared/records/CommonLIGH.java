package esmj3d.data.shared.records;

import javax.vecmath.Color3f;

import esmj3d.data.shared.subrecords.MODL;
import esmmanager.common.data.record.Record;

public class CommonLIGH extends RECO
{
	
	public MODL MODL = null;
	
	public Color3f color;

	public float radius;
	
	public float fade = 0;
	
	public float falloffExponent = 0;

	public float fieldOfView = -1f;

	public CommonLIGH(Record recordData)
	{
		super(recordData);
	}

}
