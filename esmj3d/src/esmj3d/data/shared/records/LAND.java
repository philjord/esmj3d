package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.FormID;

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

	public FormID[] VTEXs;

	public LAND(Record recordData)
	{
		super(recordData);

		ArrayList<BTXT> BTXTsv = new ArrayList<BTXT>();
		ArrayList<ATXT> ATXTsv = new ArrayList<ATXT>();
		ArrayList<FormID> VTEXsv = new ArrayList<FormID>();

		ArrayList<Subrecord> subrecords = recordData.getSubrecords();
		for (int i = 0; i < subrecords.size(); i++)
		{
			Subrecord sr = subrecords.get(i);
			byte[] bs = sr.getData();

			if (sr.getType().equals("DATA"))
			{
				DATA = new DATA(bs);
			}
			else if (sr.getType().equals("VNML"))
			{
				VNML = bs;
			}
			else if (sr.getType().equals("VHGT"))
			{
				VHGT = bs;
			}
			else if (sr.getType().equals("VCLR"))
			{
				VCLR = bs;
			}
			else if (sr.getType().equals("BTXT"))
			{
				BTXTsv.add(new BTXT(bs));
			}
			else if (sr.getType().equals("ATXT"))
			{
				ATXT atxt = new ATXT(bs);
				//TODO: use next()
				//do we have space for the next item
				if (i + 1 < subrecords.size())
				{
					Subrecord sr2 = subrecords.get(i + 1);

					if (sr2.getType().equals("VTXT"))
					{
						atxt.vtxt = new VTXT(sr2.getData());
						i++;
					}
				}
				ATXTsv.add(atxt);
			}
			else if (sr.getType().equals("VTEX"))
			{
				//VTEX (Optional): Texture FormIDs: A sequence of LTEX FormIDs. 
				//Many may be NULL values. (Variable length, but always multiples of 4 bytes).
				
				for (int f = 0; f < bs.length; f += 4)
				{
					byte[] fbs = new byte[4];
					System.arraycopy(bs, f, fbs, 0, 4);
					VTEXsv.add(new FormID(fbs));					
				}
				 
				//it's 64 ints long, is that 8 layers by 4 quadrants but last 32 are 0?
				//there is one in anvil world and I note that this land has a shape that doesn't fit it's neighbours	
				//land id 96329 it only has subs of DATA VNML, VHGT, VCLR no texture data	so therefore no VTXT for transparency

				//TODO: MegaTonWordl in fallout3 shows signs of being corrupted for some reason
				// LTEX wrong ids data is mis placing the quadrants in height
				// but mega ton does not seem to have one? and it has some quandrants

				//I notice my height data is out in the case of bad land record, but the cell location is correct

			}
			else
			{
				System.out.println("unhandled : " + sr.getType() + " in record " + recordData + " in " + this);
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

		VTEXs = new FormID[VTEXsv.size()];
		for (int j = 0; j < VTEXsv.size(); j++)
		{
			VTEXs[j] = VTEXsv.get(j);
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
