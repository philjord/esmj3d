package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.LString;
import tools.io.ESMByteConvert;

public class CommonCELL extends InstRECO
{
	public LString FULL;

	public FormID XOWN;

	public byte[] XCLL = null; //Lighting for interior cell

	public ArrayList<FormID> XCLRs = new ArrayList<FormID>(); // region ids

	/* Non-ocean water-height in cell, is used for rivers, ponds etc., ocean-water is globally defined elsewhere.
	0x7F7FFFFF reserved as ID for "no water present", it is also the maximum positive float.
	0x4F7FFFC9 is a bug in the CK, this is the maximum unsigned integer 2^32-1 cast to a float and means the same as above
	0xCF000000 could be a bug as well, this is the maximum signed negative integer -2^31 cast to a float
	
	//6.8056466E36 is weird skyrim water level (possibly meaning use default from WRLD)
	 */
	public float XCLW = Float.NEGATIVE_INFINITY; //water height if not 0

	public FormID XCWT;

	public FormID XCCM;

	public CommonCELL(Record recordData)
	{
		super(recordData);

		List<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getSubrecordData();

			if (sr.getSubrecordType().equals("EDID"))
			{
				setEDID(bs);
			}
			else if (sr.getSubrecordType().equals("FULL"))
			{
				FULL = new LString(bs);
			}
			else if (sr.getSubrecordType().equals("XCLC"))
			{
				x = ESMByteConvert.extractInt(bs, 0);
				y = ESMByteConvert.extractInt(bs, 4);
				if (bs.length == 12)
				{
					/*	uint32 - flags (high bits look random)

					    0x1 - Force Hide Land Quad 1
					    0x2 - Force Hide Land Quad 2
					    0x4 - Force Hide Land Quad 3
					    0x8 - Force Hide Land Quad 4 */
				}
			}
			else if (sr.getSubrecordType().equals("XOWN"))
			{
				//XOWN = new FormID(bs); //12 bytes in FO4
			}
			else if (sr.getSubrecordType().equals("XCLL"))
			{
				XCLL = bs;
			}
			else if (sr.getSubrecordType().equals("XCLR"))
			{
				//				XCLRs.add(new FormID(bs));
			}
			else if (sr.getSubrecordType().equals("XCLW"))
			{
				XCLW = ESMByteConvert.extractFloat(bs, 0);
			}
			else if (sr.getSubrecordType().equals("XCWT"))
			{
				XCWT = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("XCCM"))
			{
				XCCM = new FormID(bs);
			}

		}
	}
}
