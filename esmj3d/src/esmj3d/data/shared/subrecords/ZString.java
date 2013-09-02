package esmj3d.data.shared.subrecords;


public class ZString
{
	public String str = "";

	public ZString(byte[] bytes)
	{
		str = new String(bytes, 0, bytes.length - 1);
		//if (!Pattern.matches("[^\\p{C}[\\s]]*", str))
		//{
		//	new Throwable("Not a String!! " + str).printStackTrace();
		//}
	}
}
