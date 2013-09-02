package esmj3d.data.shared.records;

import java.util.ArrayList;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.ZString;

public class TXST extends RECO
{
	//In Fo3, TES5

	/**
	 * Texture set, containing base, normal, glow, bump etc
	 */
	public ZString EDID = null;

	public ZString TX00;//base

	public ZString TX01;//normal

	public ZString TX02;//environment mask?

	public ZString TX03;//glow?

	public ZString TX04;//height - none in esm

	public ZString TX05;//environment

	public ZString TX06;

	public ZString TX07;

	public TXST(Record recordData)
	{
		super(recordData);
		ArrayList<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getSubrecordData();

			if (sr.getSubrecordType().equals("EDID"))
			{
				EDID = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("OBND"))
			{

			}
			else if (sr.getSubrecordType().equals("TX00"))
			{
				TX00 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX01"))
			{
				TX01 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX02"))
			{
				TX02 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX03"))
			{
				TX03 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX04"))
			{
				// none in esm 
				TX04 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX05"))
			{
				TX05 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX06"))
			{
				TX06 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("TX07"))
			{
				TX07 = new ZString(bs);
			}
			else if (sr.getSubrecordType().equals("DODT"))
			{
				// none in esm 

			}
			else if (sr.getSubrecordType().equals("DNAM"))
			{

			}
			else
			{
				System.out.println("unhandled : " + sr.getSubrecordType() + " in " + recordData);
			}

		}
	}
}
