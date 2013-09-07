package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;
import tools.io.PrimitiveBytes;

public class XMBO
{
	public float x = 0;

	public float y = 0;

	public float z = 0;

	public XMBO(byte[] bytes)
	{
		x = ESMByteConvert.extractFloat(bytes, 0);
		y = ESMByteConvert.extractFloat(bytes, 4);
		z = ESMByteConvert.extractFloat(bytes, 8);
	}

	public byte[] getBytes()
	{
		byte[] bytes = new byte[12];
		PrimitiveBytes.insertFloat(bytes, x, 0);
		PrimitiveBytes.insertFloat(bytes, y, 4);
		PrimitiveBytes.insertFloat(bytes, z, 8);
		return bytes;
	}
}
