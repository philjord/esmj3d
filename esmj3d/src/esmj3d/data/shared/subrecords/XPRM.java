package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;
import tools.io.PrimitiveBytes;

public class XPRM
{
	// note bounds is displayed *2 in the geck, also has a 1 in unused coordinate??
	public float boundsx;

	public float boundsy;

	public float boundsz;

	public float colorr;

	public float colorg;

	public float colorb;

	public float float1 = 0;

	public int int1 = 0;

	public XPRM(byte[] bytes)
	{
		boundsx = ESMByteConvert.extractFloat(bytes, 0);
		boundsy = ESMByteConvert.extractFloat(bytes, 4);
		boundsz = ESMByteConvert.extractFloat(bytes, 8);

		colorr = ESMByteConvert.extractFloat(bytes, 12);
		colorg = ESMByteConvert.extractFloat(bytes, 16);
		colorb = ESMByteConvert.extractFloat(bytes, 20);

		float1 = ESMByteConvert.extractFloat(bytes, 24);
		int1 = ESMByteConvert.extractInt(bytes, 28);
	}

	public byte[] getBytes()
	{
		byte[] bytes = new byte[32];
		PrimitiveBytes.insertFloat(bytes, boundsx, 0);
		PrimitiveBytes.insertFloat(bytes, boundsy, 4);
		PrimitiveBytes.insertFloat(bytes, boundsz, 8);

		PrimitiveBytes.insertFloat(bytes, colorr, 12);
		PrimitiveBytes.insertFloat(bytes, colorg, 16);
		PrimitiveBytes.insertFloat(bytes, colorb, 20);

		PrimitiveBytes.insertFloat(bytes, float1, 24);
		PrimitiveBytes.insertInt(bytes, int1, 28);

		return bytes;
	}

}
