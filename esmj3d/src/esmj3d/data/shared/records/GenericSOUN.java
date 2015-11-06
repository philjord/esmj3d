package esmj3d.data.shared.records;

import java.util.ArrayList;

import esmj3d.data.shared.records.RECO;
import esmj3d.data.shared.subrecords.ZString;
import esmmanager.common.data.record.Record;
import esmmanager.common.data.record.Subrecord;

public class GenericSOUN extends RECO
{
	public ZString EDID = null;

	public ZString FNAM = null;

	public GenericSOUN(Record recordData)
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
			else if (sr.getType().equals("FNAM"))
			{
				FNAM = new ZString(bs);
			}
		}
	}

	public String showDetails()
	{
		return "SOUN : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str;
	}

}
