package esmj3d.data.shared.records;

import java.util.ArrayList;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.MODL;
import esmj3d.data.shared.subrecords.ZString;

public class GenericDOOR extends RECO
{
	public ZString EDID;

	public MODL MODL;

	public int SNAM = -1; //open sound

	public int BNAM = -1; //loop sound

	public int ANAM = -1; //close sound

	public byte FNAM = 0; //flags

	public GenericDOOR(Record recordData)
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

			else if (sr.getType().equals("MODL"))
			{
				MODL = new MODL(bs);
			}
			else if (sr.getType().equals("MODB"))
			{
				MODL.addMODBSub(bs);
			}
			else if (sr.getType().equals("MODT"))
			{
				MODL.addMODTSub(bs);
			}
			else if (sr.getType().equals("MODS"))
			{
				MODL.addMODSSub(bs);
			}
			else if (sr.getType().equals("SNAM"))
			{
				SNAM = ESMByteConvert.extractInt(bs, 0);
			}
			else if (sr.getType().equals("BNAM"))
			{
				BNAM = ESMByteConvert.extractInt(bs, 0);
			}
			else if (sr.getType().equals("ANAM"))
			{
				ANAM = ESMByteConvert.extractInt(bs, 0);
			}
			else if (sr.getType().equals("FNAM"))
			{
				FNAM = bs[0];
			}
			else
			{
				//System.out.println("unhandled : " + sr.getSubrecordType() + " in record " + recordData + " in " + this);
			}
		}
	}

	public String showDetails()
	{
		return "DOOR : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str;
	}

}
