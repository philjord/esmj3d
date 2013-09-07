package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;

/**
 * Note no location data for these ones
 * @author philip
 *
 */
public class LAND extends InstRECO
{
	public DATA DATA = null;

	public byte[] VNML = null;

	public byte[] VCLR = null;

	public byte[] VHGT = null;

	public BTXT[] BTXTs = new BTXT[4];

	public ATXT[] ATXTs;

	public LAND(Record recordData)
	{
		super(recordData);

		ArrayList<BTXT> BTXTsv = new ArrayList<BTXT>();
		ArrayList<ATXT> ATXTsv = new ArrayList<ATXT>();

		ArrayList<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getSubrecordData();

			if (sr.getSubrecordType().equals("DATA"))
			{
				DATA = new DATA(bs);
			}
			else if (sr.getSubrecordType().equals("VNML"))
			{
				VNML = bs;
			}
			else if (sr.getSubrecordType().equals("VHGT"))
			{
				VHGT = bs;
			}
			else if (sr.getSubrecordType().equals("VCLR"))
			{
				VCLR = bs;
			}
			else if (sr.getSubrecordType().equals("BTXT"))
			{
				BTXTsv.add(new BTXT(bs));
			}
			else if (sr.getSubrecordType().equals("ATXT"))
			{
				ATXT atxt = new ATXT(bs);

				i++;
				Subrecord sr2 = subrecords.get(i);

				atxt.vtxt = new VTXT(sr2.getSubrecordData());
				ATXTsv.add(atxt);
			}
			else if (sr.getSubrecordType().equals("VTEX"))
			{
				//TODO: why the hell is there a VTEX??  what is it??  it's 256 long
				//there is one in anvil world and I note that a land in anvil is stuffed with totally wrong shape	
				//land id 96329 it only has subs of DATA VNML, VHGT, VCLR no texture data	
			}
			else
			{
				System.out.println("unhandled : " + sr.getSubrecordType() + " in " + recordData);
			}

		}

		//now make the BTXT and ATXT ordered
		for (int j = 0; j < BTXTsv.size(); j++)
		{
			BTXT btxt = BTXTsv.get(j);
			BTXTs[btxt.quadrant] = btxt;
		}

		ATXTs = new ATXT[ATXTsv.size()];
		for (int j = 0; j < ATXTsv.size(); j++)
		{
			ATXTs[j] = ATXTsv.get(j);
		}

		Arrays.sort(ATXTs, new Comparator<ATXT>()
		{
			public int compare(ATXT a1, ATXT a2)
			{
				return a1.layer < a2.layer ? -1 : a1.layer == a2.layer ? 0 : 1;
			}
		});

	}

	public String showDetails()
	{
		return "LAND : (" + formId + "|" + Integer.toHexString(formId) + ") ";
	}

	public class DATA
	{
		public byte[] data;

		private DATA(byte[] bytes)
		{
			data = bytes;
		}
	}

	public class ATXT
	{
		public int textureFormID = 0;

		public int quadrant;

		public byte unknown;

		public int layer;

		public VTXT vtxt;

		public ATXT(byte[] bytes)
		{
			textureFormID = ESMByteConvert.extractInt(bytes, 0);
			quadrant = bytes[4];
			unknown = bytes[5];
			layer = ESMByteConvert.extractShort(bytes, 6);
		}
	}

	public class BTXT
	{
		public int textureFormID = 0;

		public int quadrant;

		public byte unknown1;

		public byte unknown2;

		public byte unknown3;

		public BTXT(byte[] bytes)
		{
			textureFormID = ESMByteConvert.extractInt(bytes, 0);
			quadrant = bytes[4];
			unknown1 = bytes[5];
			unknown2 = bytes[6];
			unknown3 = bytes[7];
		}
	}

	public class VTXT
	{
		public int count = 0;

		public int[] position;

		public byte[] unknownByte1;

		public byte[] unknownByte2;

		public float[] opacity;

		public VTXT(byte[] bytes)
		{
			count = bytes.length / 8;
			position = new int[count];
			unknownByte1 = new byte[count];
			unknownByte2 = new byte[count];
			opacity = new float[count];

			for (int i = 0; i < count; i++)
			{
				position[i] = ESMByteConvert.extractShort(bytes, (i * 8) + 0);
				unknownByte1[i] = bytes[(i * 8) + 2];
				unknownByte2[i] = bytes[(i * 8) + 3];
				opacity[i] = ESMByteConvert.extractFloat(bytes, (i * 8) + 4);
			}
		}
	}
}
