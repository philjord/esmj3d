package esmj3d.data.shared.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import esmj3d.data.shared.subrecords.FormID;
import esmmanager.common.data.record.Record;
import esmmanager.common.data.record.Subrecord;
import tools.io.ESMByteConvert;

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

	public int landX;

	public int landY;

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

		List<Subrecord> subrecords = recordData.getSubrecords();
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
			else if (sr.getSubrecordType().equals("MPCD"))
			{
				// new in FO4
			}
			else if (tes3 && sr.getSubrecordType().equals("INTV"))
			{
				//Note this is not used except for debug output
				landX = ESMByteConvert.extractInt(bs, 0);
				landY = ESMByteConvert.extractInt(bs, 4);
			}
			else if (tes3 && sr.getSubrecordType().equals("VTEX"))
			{
				// An array made up of 4x4 sets of 4x4 short texture ids
				for (int f = 0; f < bs.length; f += 2)
				{
					VTEXshortsv.add(ESMByteConvert.extractShort(bs, f));
				}
			}
			else if (tes3 && sr.getSubrecordType().equals("WNAM"))
			{
				// low-LOD heightmap (used for rendering the global map)
				//signed char mWnam[81];
			}
			else if (!tes3 && sr.getSubrecordType().equals("BTXT"))
			{
				BTXTsv.add(new BTXT(bs));
			}
			else if (!tes3 && sr.getSubrecordType().equals("ATXT"))
			{
				ATXT atxt = new ATXT(bs);
				//TODO: use next()
				//do we have space for the next item
				if (i + 1 < subrecords.size())
				{
					Subrecord sr2 = subrecords.get(i + 1);

					if (sr2.getSubrecordType().equals("VTXT"))
					{
						atxt.vtxt = new VTXT(sr2.getSubrecordData());
						i++;
					}
				}
				ATXTsv.add(atxt);
			}
			else if (!tes3 && sr.getSubrecordType().equals("VTEX"))
			{
				//TODO: I think using parent world has made this redundant entriely
				//VTEX (Optional): Texture FormIDs: A sequence of LTEX FormIDs. 
				//Many may be NULL values. (Variable length, but always multiples of 4 bytes).

				for (int f = 0; f < bs.length; f += 4)
				{
					byte[] fbs = new byte[4];
					System.arraycopy(bs, f, fbs, 0, 4);
					VTEXidsv.add(new FormID(fbs));
				}

				// Notice for bad world land data the parent world data is accurate 

			}
			else
			{
				System.out.println("unhandled : " + sr.getSubrecordType() + " in record " + recordData + " in " + this);
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

			Arrays.sort(ATXTs, new Comparator<ATXT>() {
				public int compare(ATXT a1, ATXT a2)
				{
					return a1.layer < a2.layer ? -1 : a1.layer == a2.layer ? 0 : 1;
				}
			});

			//possibly remove entirely?
			if (VTEXidsv.size() > 0)
			{
				VTEXids = new FormID[VTEXidsv.size()];
				for (int j = 0; j < VTEXidsv.size(); j++)
				{
					VTEXids[j] = VTEXidsv.get(j);
				}
			}
		}

	//	HashSet<Integer> allTexIds = new HashSet<Integer>();
		/*for (int j = 0; j < BTXTsv.size(); j++)
		{
			allTexIds.add(BTXTsv.get(j).textureFormID);
		}
		for (int j = 0; j < ATXTsv.size(); j++)
		{
			allTexIds.add(ATXTsv.get(j).textureFormID);
		}*/

		//ok no more unrolling the outer 4x4 of tes3
		// I need to just do the inner 4x4 

	/*	for (int a = 0; a < 16; a++)
		{
			//allTexIds.clear();
			for (int b = 0; b < 16; b++)
			{
				allTexIds.add(VTEXshortsv.get((a * 16) + b));
			}

			//System.out.println("Total count of VTEXshortsv texture Ids for " + this + " " + a + " " + allTexIds.size());
		}*/
	//	System.out.println(
	//			"Total count of VTEXshortsv texture Ids for " + this + " " + allTexIds.size() + " landX " + landX + " landY " + landY);
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

		public byte[] unknownByte1;//Unknown 	1 byte 	Unknown.

		public byte[] unknownByte2;//Unknown 	1 byte 	Unknown. Frequently (but not always) the same value as the previous byte. They look to be independent scalars in any case.

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
