package esmj3d.data.shared.subrecords;

public class ZString
{
	protected ZString() {}
	//Don't keep it it's a useless extra
	public static String toString(byte[] bytes)
	{
		String str = null;
		if(bytes.length > 0) {
			str = new String(bytes, 0, bytes.length - 1);
			//if (!Pattern.matches("[^\\p{C}[\\s]]*", str))
			//{
			//	new Throwable("Not a String!! " + str).printStackTrace();
			//}
		} else {
			new Throwable("bytes " + bytes.length).printStackTrace();
		}
		return str;
	}

 
}
