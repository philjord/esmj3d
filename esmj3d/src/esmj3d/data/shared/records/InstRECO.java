package esmj3d.data.shared.records;

import utils.ESMByteConvert;
import esmLoader.common.data.record.Record;

public abstract class InstRECO extends RECO
{
	//Note: Divide X,Y by 4096 to get cell location
	public float x = 0;

	public float y = 0;

	public float z = 0;

	public float rx = 0;

	public float ry = 0;

	public float rz = 0;

	public float scale = 1f;

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
}
