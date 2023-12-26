package esmj3d.data.shared.records;

import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.ZString;

public class GenericSOUN extends RECO
{
	public ZString EDID = null;

	public ZString FNAM = null;

	public GenericSOUN(Record recordData)
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
			else if (sr.getSubrecordType().equals("FNAM"))
			{
				FNAM = new ZString(bs);
			}
		}
	}

	@Override
	public String showDetails()
	{
		return "SOUN : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str;
	}

}
