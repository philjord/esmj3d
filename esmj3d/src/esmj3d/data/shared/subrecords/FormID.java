package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;
import tools.io.PrimitiveBytes;

public class FormID {

	//used by analyzersd and edits to ensure incoming data is square, but falsed for actual game loading
	public static boolean	DO_CHECKS	= false;

	public final int		formId;

	public FormID(byte[] bytes) {
		if (bytes.length < 4 || bytes.length > 4)
			new Throwable("bad bytes for FormID " + new String(bytes)).printStackTrace();

		formId = ESMByteConvert.extractInt3(bytes, 0);
		if (DO_CHECKS) {
			if (formId < -1 || formId > 15000000) {
				new Throwable("Odd FormID? " + formId).printStackTrace();
			}
		}
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[4];
		PrimitiveBytes.insertInt(bytes, formId, 0);
		return bytes;
	}
}
