package esmj3d.data.shared.subrecords;

import java.util.regex.Pattern;

public class ZString {
	protected ZString() {
	}

	//Don't keep it it's a useless extra
	public static String toString(byte[] bytes) {
		String str = null;
		if (bytes.length > 0) {
			str = new String(bytes, 0, bytes.length - 1);
			if (FormID.DO_CHECKS) {
				if (!Pattern.matches("[^\\p{C}[\\s]]*", str)) {
					new Throwable("Not a String!! " + str).printStackTrace();
				}
			}
		} else {
			//presumably just a null string
			str = ""; // to avoid null checks elsewhere
			//new Throwable("bytes " + bytes.length).printStackTrace();
		}
		return str;
	}

}
