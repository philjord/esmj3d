package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class CNTO
{
	public int itemFormId;

	public int count;

	public CNTO(byte[] bytes)
	{
		itemFormId = ESMByteConvert.extractInt3(bytes, 0);
		count = ESMByteConvert.extractInt(bytes, 4);

	}
}
