package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.List;

import esmio.common.data.record.Record;
import esmio.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.CNTO;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.data.shared.subrecords.MODL;
import esmj3d.data.shared.subrecords.ZString;
import tools.io.ESMByteConvert;

public class GenericCONT extends RECO
{
	public ZString EDID;

	public LString FULL;

	public MODL MODL;

	public ArrayList<CNTO> CNTOs = new ArrayList<CNTO>();

	public int SNAM;

	public int QNAM;

	public GenericCONT(Record recordData)
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
			else if (sr.getSubrecordType().equals("MODL"))
			{
				MODL = new MODL(bs);
			}
			else if (sr.getSubrecordType().equals("MODT"))
			{
				MODL.addMODTSub(bs);
			}
			else if (sr.getSubrecordType().equals("SNAM"))
			{
				SNAM = ESMByteConvert.extractInt(bs, 0);
			}
			else if (sr.getSubrecordType().equals("QNAM"))
			{
				QNAM = ESMByteConvert.extractInt(bs, 0);
			}
			else if (sr.getSubrecordType().equals("CNTO"))
			{
				CNTOs.add(new CNTO(bs));
			}
			else
			{
				// hierachy this make no sense now
				//System.out.println("unhandled : " + sr.getSubrecordType() + " in record " + recordData + " in " + this);
			}
		}
	}

	public String showDetails()
	{
		return "CONT : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str + " : " + MODL.model;
	}

}
