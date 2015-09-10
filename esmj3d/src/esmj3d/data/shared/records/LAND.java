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
	public boolean tes3 = false;

	public DATA DATA = null;

	public byte[] VNML = null;

	public byte[] VCLR = null;

	public byte[] VHGT = null;

	//TES3
	public int[] VTEXshorts;

	//VTEX (512 bytes) optional
	//A 16x16 array of short texture indices (from a LTEX record I think).

	//TES4+
	public BTXT[] BTXTs = new BTXT[4];

	public ATXT[] ATXTs;

	public FormID[] VTEXids;

	public LAND(Record recordData)
	{
		this(recordData, false);
	}

	public LAND(Record recordData, boolean tes3)
	{
		super(recordData);
		this.tes3 = tes3;

		ArrayList<BTXT> BTXTsv = new ArrayList<BTXT>();
		ArrayList<ATXT> ATXTsv = new ArrayList<ATXT>();
		ArrayList<FormID> VTEXidsv = new ArrayList<FormID>();

		ArrayList<Integer> VTEXshortsv = new ArrayList<Integer>();

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
			else if (tes3 && sr.getType().equals("INTV"))
			{
				//landX = ESMByteConvert.extractInt(bs, 0);
				//landY = ESMByteConvert.extractInt(bs, 4);
			}
			else if (tes3 && sr.getType().equals("VTEX"))
			{
				// these occur in oblivion on rare occasion
				for (int f = 0; f < bs.length; f += 2)
				{
					VTEXshortsv.add(ESMByteConvert.extractShort(bs, f));
				}
			}
			else if (tes3 && sr.getType().equals("WNAM"))
			{

			}
			else if (!tes3 && sr.getType().equals("BTXT"))
			{
				BTXTsv.add(new BTXT(bs));
			}
			else if (!tes3 && sr.getType().equals("ATXT"))
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
			else if (!tes3 && sr.getType().equals("VTEX"))
			{
				//VTEX (Optional): Texture FormIDs: A sequence of LTEX FormIDs. 
				//Many may be NULL values. (Variable length, but always multiples of 4 bytes).

				for (int f = 0; f < bs.length; f += 4)
				{
					byte[] fbs = new byte[4];
					System.arraycopy(bs, f, fbs, 0, 4);
					VTEXidsv.add(new FormID(fbs));
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

		if (VTEXshortsv.size() > 0)
		{
			//I'm going to unroll them here because it's painful later on 
			//IIRC the textures are not in a 16x16 grid, but in a 4x4 grid in a 4x4 grid.

			VTEXshorts = new int[VTEXshortsv.size()];
			for (int a = 0; a < VTEXshortsv.size(); a++)
			{
				int lqbx = (a / 16) % 4;
				int lqix = a % 4;

				int lqby = a / (4 * 16);
				int lqiy = (a % 16) / 4;

				//each y little box moves me 4 rows worth(4x16)
				//each y little inner moves by 1 row (16)
				//each x little box moves me across 4
				//each x little inner moves me 1
				int quadrant = (lqby * 4 * 16) + (lqiy * 16) + (lqbx * 4) + lqix;
				VTEXshorts[quadrant] = VTEXshortsv.get(a);
			}
		}
		if (!tes3)
		{

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

			VTEXids = new FormID[VTEXidsv.size()];
			for (int j = 0; j < VTEXidsv.size(); j++)
			{
				VTEXids[j] = VTEXidsv.get(j);
			}

			Arrays.sort(ATXTs, new Comparator<ATXT>()
			{
				public int compare(ATXT a1, ATXT a2)
				{
					return a1.layer < a2.layer ? -1 : a1.layer == a2.layer ? 0 : 1;
				}
			});
		}
	}

	public String showDetails()
	{
		return "LAND : (" + formId + "|" + Integer.toHexString(formId) + ") ";
	}

	public static class DATA
	{
		public byte[] data;

		private DATA(byte[] bytes)
		{
			data = bytes;
		}
	}

	public static class ATXT
	{
		public int textureFormID = 0;

		public int quadrant;

		public byte unknown;

		public int layer;

		public VTXT vtxt;

		//for tes3 faked up
		public ATXT()
		{

		}

		public ATXT(byte[] bytes)
		{
			textureFormID = ESMByteConvert.extractInt(bytes, 0);
			quadrant = bytes[4];
			unknown = bytes[5];
			layer = ESMByteConvert.extractShort(bytes, 6);
		}
	}

	public static class BTXT
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

	public static class VTXT
	{
		public int count = 0;

		public int[] position;

		public byte[] unknownByte1;

		public byte[] unknownByte2;

		public float[] opacity;

		//for tes3 faked up
		public VTXT()
		{

		}

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
