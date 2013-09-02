package esmj3d.data.shared.subrecords;

import utils.ESMByteConvert;

public class FormID
{
	public int formId;

	public FormID(byte[] bytes)
	{
		formId = ESMByteConvert.extractInt(bytes, 0);
		//if (formId < -1 || formId > 5000000)
		//{
		//	new Throwable("Odd formId? " + formId).printStackTrace();
		//}
	}
}
