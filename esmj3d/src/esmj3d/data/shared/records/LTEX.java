package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FormID;
import esmj3d.data.shared.subrecords.ZString;
import tools.io.ESMByteConvert;

public class LTEX extends RECO
{
	

	//FO3 onwards
	public int textureSetId = -1;

	//TES4
	public String ICON = null;

	public byte[] HNAM = null;

	public byte[] SNAM = null;

	public FormID[] GNAMs; //grass in priority order
	
	//FO76 onwards
	public String materialName;
	public int materialTypeId;

	public LTEX(Record recordData)
	{
		super(recordData);

		ArrayList<FormID> GNAMsl = new ArrayList<FormID>();

		List<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getSubrecordData();

			if (sr.getSubrecordType().equals("EDID"))
			{
				setEDID(bs);
			}
			else if (sr.getSubrecordType().equals("TNAM"))
			{
				textureSetId = ESMByteConvert.extractInt(bs, 0);// 4 bytes pointer to texture set?
			}
			else if (sr.getSubrecordType().equals("ICON"))
			{
				ICON = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("HNAM"))
			{
				HNAM = bs; //3 bytes
				//Havok Data This section allows you to choose the sound type for the selected landscape 
				//texture from the Material Type dialogue, as well as the Friction and Restitution 
			}
			else if (sr.getSubrecordType().equals("SNAM"))
			{
				SNAM = bs; //1 byte	 
				//Texture Specular Exponent, not supported
			}
			else if (sr.getSubrecordType().equals("GNAM"))
			{
				// not mandatory
				GNAMsl.add(new FormID(bs));
			}
			else if (sr.getSubrecordType().equals("MNAM"))
			{
				//FO76 possible decode
				materialTypeId = ESMByteConvert.extractInt(bs, 0); 
			}
			//TES3 only
			else if (sr.getSubrecordType().equals("NAME"))
			{
				setEDID(bs);
			}
			else if (sr.getSubrecordType().equals("INTV"))
			{
				//ltexid = ESMByteConvert.extractInt(bs, 0);//used in the edid
			}
			else if (sr.getSubrecordType().equals("DATA"))
			{
				ICON = ZString.toString(bs);
			}
			
			
			//FO76 upwards has  
			/*
			LTEX Land Texture count=150
			C________	Subrecord	Type______	Info
			1	EDID	ZString	0-0. Count=150 After Head(150), Followed by BNAM(49), MNAM(100), TNAM(1), . Editor ID, used only by consturction kit, not loaded at runtime
			0-1	BNAM	ZString	0-1opt. Count=49 After EDID(49), Followed by MNAM(49), .
			0-1	TNAM	FormId	1-opt. Count=1 After EDID(1), Followed by MNAM(1), . Texture Set Pointer.
			1	MNAM	FormId	2-0. Count=150 After BNAM(49), EDID(100), TNAM(1), Followed by ONAM(30), HNAM(120), . Material Type Pointer.
			0-1	ONAM	FormId	2-1opt. Count=30 After MNAM(30), Followed by HNAM(30), . Ground Cover? Pointer.
			1	HNAM	byte[] 2	3-0. Count=150 After ONAM(30), MNAM(120), Followed by Last(149), SNAM(1), .
			0-1	SNAM	byte[] 1	3-1opt. Count=1 After HNAM(1), Followed by GNAM(1), .
			0+ (0-3)	GNAM	FormId	4-opt. Count=3 After SNAM(1), GNAM(2), Followed by GNAM(2), Last(1), . Pointers to REFR. Pointers to GRAS.
			 */
			else if (sr.getSubrecordType().equals("BNAM"))
			{
				//materials bgsm name
				materialName = ZString.toString(bs);
			}
			else if (sr.getSubrecordType().equals("ONAM"))
			{

			}
			
			
			else
			{
				System.out.println("unhandled : " + sr.getSubrecordType() + " in record " + recordData + " in " + this);
			}

			// transfer to arrays
			GNAMs = new FormID[GNAMsl.size()];
			GNAMsl.toArray(GNAMs);
		}
	}

	@Override
	public String showDetails()
	{
		return super.showDetails() + " : TextureSet " + textureSetId;
	}

}
