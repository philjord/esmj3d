package esmj3d.data.shared.subrecords;

import utils.ESMByteConvert;

public class ANAM
{
	public int enchantmentPoints;

	public ANAM(byte[] bytes)
	{
		enchantmentPoints = ESMByteConvert.extractShort(bytes, 0);
	}
}
