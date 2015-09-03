package esmj3d.data.shared.records;

import java.util.ArrayList;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.data.shared.subrecords.ZString;

public class CommonCELL extends InstRECO
{
	public ZString EDID;

	public LString FULL;

	public FormID XOWN;

	public byte[] XCLL = null; //Lighting for interior cell

	public ArrayList<FormID> XCLRs = new ArrayList<FormID>(); // region ids

	/* Non-ocean water-height in cell, is used for rivers, ponds etc., ocean-water is globally defined elsewhere.
	0x7F7FFFFF reserved as ID for "no water present", it is also the maximum positive float.
	0x4F7FFFC9 is a bug in the CK, this is the maximum unsigned integer 2^32-1 cast to a float and means the same as above
	0xCF000000 could be a bug as well, this is the maximum signed negative integer -2^31 cast to a float
	 */
	public float XCLW = Float.NEGATIVE_INFINITY; //water height if not 0

	public FormID XCWT;

	public FormID XCCM;

	public CommonCELL(Record recordData)
	{
		super(recordData);

		ArrayList<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getData();

			if (sr.getType().equals("EDID"))
			{
				EDID = new ZString(bs);
			}
			else if (sr.getType().equals("FULL"))
			{
				FULL = new LString(bs);
			}
			else if (sr.getType().equals("XCLC"))
			{
				x = ESMByteConvert.extractInt(bs, 0);
				y = ESMByteConvert.extractInt(bs, 4);
			}
			else if (sr.getType().equals("XOWN"))
			{
				XOWN = new FormID(bs);
			}
			else if (sr.getType().equals("XCLL"))
			{
				XCLL = bs;
			}
			else if (sr.getType().equals("XCLR"))
			{
//				XCLRs.add(new FormID(bs));
			}
			else if (sr.getType().equals("XCLW"))
			{
				XCLW = ESMByteConvert.extractFloat(bs, 0);
			}
			else if (sr.getType().equals("XCWT"))
			{
				XCWT = new FormID(bs);
			}
			else if (sr.getType().equals("XCCM"))
			{
				XCCM = new FormID(bs);
			}

		}
	}
}
