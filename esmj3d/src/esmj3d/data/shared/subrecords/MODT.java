package esmj3d.data.shared.subrecords;


public class MODT
{
	public byte[] bs;

	public int count = 0;

	public String str;

	public String[] parts;

	public MODT(byte[] bytes)
	{
		bs = bytes;
		//count = ESMByteConvert.extractInt(bytes, 0);;
		//str = new String(bytes, 0, bytes.length - 1);

		//parts = str.split("\\\\");

	}

}
