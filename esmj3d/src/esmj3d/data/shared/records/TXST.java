package esmj3d.data.shared.records;

import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.ZString;


/**
 * Texture set, containing base, normal, glow, bump etc
 * In Fo3, TES5, FO4
 * oddly FO4 has REFR pointing at these
 */
public class TXST extends RECO
{

	

	public String TX00;//base

	public String TX01;//normal

	public String TX02;//environment mask?

	public String TX03;//glow?

	public String TX04;//height - none in esm

	public String TX05;//environment

	public String TX06;

	public String TX07;

	public String MNAM;

	public TXST(Record recordData)
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
			else if (sr.getSubrecordType().equals("OBND"))
			{

			}
			else if (sr.getSubrecordType().equals("TX00"))
			{
				TX00 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX01"))
			{
				TX01 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX02"))
			{
				TX02 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX03"))
			{
				TX03 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX04"))
			{
				// none in esm 
				TX04 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX05"))
			{
				TX05 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX06"))
			{
				TX06 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("TX07"))
			{
				TX07 = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("DODT"))
			{
				// none in esm 

			}
			else if (sr.getSubrecordType().equals("DNAM"))
			{

			}
			else if (sr.getSubrecordType().equals("MNAM"))
			{
				//new in FO4
				MNAM = ZString.toString(bs);
			}
			else
			{
				System.out.println("unhandled : " + sr.getSubrecordType() + " in record " + recordData + " in " + this);
			}

		}
	}
}
