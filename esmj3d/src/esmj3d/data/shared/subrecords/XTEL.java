package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class XTEL
{
	public int doorFormId;

	public float x = 0;

	public float y = 0;

	public float z = 0;

	public float rx = 0;

	public float ry = 0;

	public float rz = 0;

	public XTEL(byte[] bytes)
	{
		//normal xtel or TES3 DODT version (doorFormId set later)
		if (bytes.length < 28)
		{
			x = ESMByteConvert.extractFloat(bytes, 0);
			y = ESMByteConvert.extractFloat(bytes, 4);
			z = ESMByteConvert.extractFloat(bytes, 8);
			rx = ESMByteConvert.extractFloat(bytes, 12);
			ry = ESMByteConvert.extractFloat(bytes, 16);
			rz = ESMByteConvert.extractFloat(bytes, 20);
		}
		else
		{
			doorFormId = ESMByteConvert.extractInt3(bytes, 0);
			x = ESMByteConvert.extractFloat(bytes, 4);
			y = ESMByteConvert.extractFloat(bytes, 8);
			z = ESMByteConvert.extractFloat(bytes, 12);
			rx = ESMByteConvert.extractFloat(bytes, 16);
			ry = ESMByteConvert.extractFloat(bytes, 20);
			rz = ESMByteConvert.extractFloat(bytes, 24);
			// one more int for TES5 uint32 - flag: 0x01 No alarm

		}
	}
}
