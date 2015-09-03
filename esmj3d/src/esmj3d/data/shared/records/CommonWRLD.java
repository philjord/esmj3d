package esmj3d.data.shared.records;

import java.util.ArrayList;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
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
			else if (sr.getType().equals("WNAM"))
			{
				WNAM = new FormID(bs);
			}
			else if (sr.getType().equals("CNAM"))
			{
				CNAM = new FormID(bs);
			}
			else if (sr.getType().equals("NAM2"))
			{
				NAM2 = new FormID(bs);
			}
			else if (sr.getType().equals("DATA"))
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
