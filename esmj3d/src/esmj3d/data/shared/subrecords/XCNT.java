package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class XCNT
{
	public int count;

	public XCNT(byte[] bytes)
	{
		count = ESMByteConvert.extractInt(bytes, 0);
	}
}
