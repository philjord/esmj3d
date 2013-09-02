package esmj3d.data.shared.subrecords;

public class DESC
{
	public String description = "";

	public DESC(byte[] bytes)
	{
		description = new String(bytes, 0, bytes.length - 1);
	}
}
