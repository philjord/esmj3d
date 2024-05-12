package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.CNTO;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.data.shared.subrecords.MODL;
import tools.io.ESMByteConvert;

public class GenericCONT extends RECO
{
	

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
				setEDID(bs);
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

	@Override
	public String showDetails()
	{
		return super.showDetails() + " : " + (MODL != null ? MODL.model : "");
	}

}
