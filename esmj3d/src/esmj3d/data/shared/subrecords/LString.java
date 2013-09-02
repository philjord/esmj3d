package esmj3d.data.shared.subrecords;

//localised string, which would be apointer into a localisation file, but in my case is not
public class LString
{
	public String str = "";

	public LString(byte[] bytes)
	{
		str = new String(bytes, 0, bytes.length - 1);
		//if (!Pattern.matches("[^\\p{C}[\\s]]*", str))
		//{
		//	new Throwable("Not a String!! " + str).printStackTrace();
		//}
	}
}
