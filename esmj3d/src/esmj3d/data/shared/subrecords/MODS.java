package esmj3d.data.shared.subrecords;

import nif.ByteConvert;

/** 
 * Scale for fun 255/ this = scale
 * no because bloated float sign (3 times too big) has smae vale as the tavern
 * @author philip
 *
 */
public class MODS
{
	public short scale;

	public MODS(byte[] bytes)
	{

		scale = ByteConvert.byteToUnsigned(bytes[0]);
	}

}
