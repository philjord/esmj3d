package esmj3d.data.shared.records;

import java.util.List;

import esmio.common.data.record.Record;
import esmio.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.data.shared.subrecords.ZString;

public class CommonWRLD extends RECO
{
	public ZString EDID = null;

	public LString FULL = null;

	public FormID WNAM = null;//parent world

	public FormID CNAM = null;

	public FormID NAM2 = null;

	public byte DATA = 0;

	public CommonWRLD(Record recordData)
	{
		super(recordData);
		List<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getSubrecordData();

			if (sr.getSubrecordType().equals("EDID"))
			{
				EDID = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("FULL"))
			{
				FULL = new LString(bs);
			}
			else if (sr.getSubrecordType().equals("WNAM"))
			{
				WNAM = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("CNAM"))
			{
				CNAM = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("NAM2"))
			{
				NAM2 = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("DATA"))
			{
				DATA = bs[0];
			}
		}
	}

	public String showDetails()
	{
		return "WRLD : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str;
	}
}
