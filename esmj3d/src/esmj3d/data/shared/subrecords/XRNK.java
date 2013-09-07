package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class XRNK
{
	public int rank;

	public XRNK(byte[] bytes)
	{
		rank = ESMByteConvert.extractInt(bytes, 0);
	}
}
