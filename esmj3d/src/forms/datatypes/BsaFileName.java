package forms.datatypes;

import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.ZString;

public class BsaFileName extends ZString {
	// this is just a ZString in the correct form to be able to be used to access a file inside a bsa file
	// mainly used for sanity checks on incoming data
	public final String bsaFileName;

	public BsaFileName(byte[] bytes) {
		this.bsaFileName = toString(bytes);
		if(FormID.DO_CHECKS) {
			// TODO: 
			// prolly should end with nif or dds or bgsm or stuff
			System.out.println("I need a BsaFileNAme end check for this name please : " + bsaFileName);
		}
	}
}
