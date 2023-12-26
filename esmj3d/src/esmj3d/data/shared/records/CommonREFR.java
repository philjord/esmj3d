package esmj3d.data.shared.records;

import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FNAM;
import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.data.shared.subrecords.TNAM;
import esmj3d.data.shared.subrecords.XACT;
import esmj3d.data.shared.subrecords.XCNT;
import esmj3d.data.shared.subrecords.XESP;
import esmj3d.data.shared.subrecords.XLCM;
import esmj3d.data.shared.subrecords.XLOC;
import esmj3d.data.shared.subrecords.XLOD;
import esmj3d.data.shared.subrecords.XRNK;
import esmj3d.data.shared.subrecords.XTEL;
import esmj3d.data.shared.subrecords.ZString;
import tools.io.ESMByteConvert;

public class CommonREFR extends InstRECO
{
	public ZString EDID;

	public FormID NAME;

	public LString FULL;

	public XTEL XTEL;

	public boolean defaultsOpen = false;

	public boolean XMRK = false;

	public FormID XOWN;

	public FormID XGLB;//tes4, tes5, not fallout3

	public FormID XRTM;//tes4, tes5, not fallout3

	public XRNK XRNK;//tes4, tes5, not fallout3

	public FormID XTRG;

	public FNAM FNAM;

	public XLOC XLOC;

	public TNAM TNAM;


	public XLCM XLCM;

	public XACT XACT;

	public XCNT XCNT;

	public XLOD XLOD;//tes4, fallout3, not tes5

	protected CommonREFR(Record recordData)
	{
		this(recordData, true);
	}

	// no Load for tes3 as NAME is a string not formId
	public CommonREFR(Record recordData, boolean load)
	{
		super(recordData);
		if (load)
		{
			List<Subrecord> subrecords = recordData.getSubrecords();
			for (int i = 0; i < subrecords.size(); i++)
			{
				Subrecord sr = subrecords.get(i);
				byte[] bs = sr.getSubrecordData();

				if (sr.getSubrecordType().equals("NAME"))
				{
					NAME = new FormID(bs);
				}
				else if (sr.getSubrecordType().equals("EDID"))
				{
					EDID = new ZString(bs);
				}
				else if (sr.getSubrecordType().equals("XTEL"))
				{
					XTEL = new XTEL(bs);
				}
				else if (sr.getSubrecordType().equals("ONAM"))
				{
					defaultsOpen = true;
				}
				else if (sr.getSubrecordType().equals("XACT"))
				{
					XACT = new XACT(bs);
				}
				else if (sr.getSubrecordType().equals("XMRK"))
				{
					XMRK = true;
				}
				else if (sr.getSubrecordType().equals("XOWN"))
				{
					//XOWN = new FormID(bs); //12 bytes in FO4
				}
				else if (sr.getSubrecordType().equals("XGLB"))
				{
					XGLB = new FormID(bs);
				}
				else if (sr.getSubrecordType().equals("XTRG"))
				{
					XTRG = new FormID(bs);
				}
				else if (sr.getSubrecordType().equals("XSCL"))
				{
					scale = ESMByteConvert.extractFloat(bs, 0);
				}
				else if (sr.getSubrecordType().equals("DATA"))
				{
					this.extractInstData(bs);
				}
				else if (sr.getSubrecordType().equals("FULL"))
				{
					FULL = new LString(bs);
				}
				else if (sr.getSubrecordType().equals("XRNK"))
				{
					XRNK = new XRNK(bs);
				}
				else if (sr.getSubrecordType().equals("FNAM"))
				{
					FNAM = new FNAM(bs);
				}
				else if (sr.getSubrecordType().equals("XLOC"))
				{
					XLOC = new XLOC(bs);
				}
				else if (sr.getSubrecordType().equals("TNAM"))
				{
					TNAM = new TNAM(bs);
				}
				else if (sr.getSubrecordType().equals("XESP"))
				{
					xesp = new XESP(bs);
				}
				else if (sr.getSubrecordType().equals("XLCM"))
				{
					XLCM = new XLCM(bs);
				}
				else if (sr.getSubrecordType().equals("XCNT"))
				{
					XCNT = new XCNT(bs);
				}
				else if (sr.getSubrecordType().equals("XLOD"))
				{
					XLOD = new XLOD(bs);
				}
			}
		}
	}

 
	

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " : " + (EDID != null ? EDID : NAME != null ? NAME.formId : "");
	}
}
