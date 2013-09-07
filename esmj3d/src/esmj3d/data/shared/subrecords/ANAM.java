package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class ANAM
{
	public int enchantmentPoints;

	public ANAM(byte[] bytes)
	{
		enchantmentPoints = ESMByteConvert.extractShort(bytes, 0);
	}
}
