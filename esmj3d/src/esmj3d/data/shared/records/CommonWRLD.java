package esmj3d.data.shared.records;

import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
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
				// 	FULL 	Name 	lstring 	The name of this worldspace used in the game
				FULL = new LString(bs);
			}
			else if (sr.getSubrecordType().equals("WNAM"))
			{
				//WNAM 	Parent worldspace 	formID 	Form ID of the parent worldspace.
				WNAM = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("CNAM"))
			{
				// 	CNAM 	Climate 	formID 	CLMT reference.
				CNAM = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("NAM2"))
			{
				// 	NAM2 	unknown 	formID 	Water WATR
				NAM2 = new FormID(bs);
			}
			else if (sr.getSubrecordType().equals("DATA"))
			{
				/*DATA 	flags 	uint8 	Flags

			    0x01 - Small World
			    0x02 - Can't Fast Travel From Here
			    0x04 ?0x04=Oblivion worldspace?
			    0x08 - No LOD Water
			    0x10 - No Landscape
			    0x20 - No Sky
			    0x40 - Fixed Dimensions
			    0x80 - No Grass */
				DATA = bs[0];
			}
		}
	}

	public String showDetails()
	{
		return "WRLD : (" + formId + "|" + Integer.toHexString(formId) + ") " + EDID.str;
	}
}
