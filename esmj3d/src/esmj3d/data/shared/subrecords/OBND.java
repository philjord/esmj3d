package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;
/**
 * Used by fallout 3 ACTI and skyrim generally not oblivion
 * @author philip
 *
 */
public class OBND
{

	public int x1;

	public int y1;

	public int z1;

	public int x2;

	public int y2;

	public int z2;

	public OBND(byte[] bytes)
	{
		x1 = ESMByteConvert.extractShort(bytes, 0);
		y1 = ESMByteConvert.extractShort(bytes, 2);
		z1 = ESMByteConvert.extractShort(bytes, 4);
		x2 = ESMByteConvert.extractShort(bytes, 6);
		y2 = ESMByteConvert.extractShort(bytes, 8);
		z2 = ESMByteConvert.extractShort(bytes, 10);
	}
}
