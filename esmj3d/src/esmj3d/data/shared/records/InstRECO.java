package esmj3d.data.shared.records;

import org.jogamp.vecmath.Vector3f;

import esmio.common.data.record.IRecordStore;
import esmio.common.data.record.Record;
import esmj3d.data.shared.subrecords.XESP;
import tools.io.ESMByteConvert;

public abstract class InstRECO extends RECO
{
	//Note: Divide X,Y by 4096 to get cell location
	protected float x = 0;

	protected float y = 0;

	protected float z = 0;

	protected float rx = 0;

	protected float ry = 0;

	protected float rz = 0;

	protected float scale = 1f;

	public XESP xesp;

	public InstRECO(Record record)
	{
		super(record);
	}

	protected void extractInstData(byte[] bs)
	{
		x = ESMByteConvert.extractFloat(bs, 0);
		y = ESMByteConvert.extractFloat(bs, 4);
		z = ESMByteConvert.extractFloat(bs, 8);
		rx = ESMByteConvert.extractFloat(bs, 12);
		ry = ESMByteConvert.extractFloat(bs, 16);
		rz = ESMByteConvert.extractFloat(bs, 20);
	}

	public Vector3f getTrans()
	{
		return getTrans(new Vector3f());
	}

	public Vector3f getTrans(Vector3f v)
	{
		if (v == null)
			v = new Vector3f();

		v.set(x, y, z);
		return v;
	}

	public Vector3f getEulerRot()
	{
		return getEulerRot(new Vector3f());
	}

	public Vector3f getEulerRot(Vector3f er)
	{
		if (er == null)
			er = new Vector3f();

		er.set(rx, ry, rz);
		return er;
	}

	public float getScale()
	{
		return scale;
	}

	public static boolean getParentEnable(InstRECO ir, IRecordStore master)
	{
		boolean isParentEnable = false;
		boolean opp = false;

		//TODO: I have a piece of megaton that is not getting turned off by no parent

		//more/other flags for skyrim?

		// appears to be "disabled" flag in the Obliv wrlds bits 110000000000
		// used at least by oblivion gates
		// formid - Parent reference (Object to take enable state from)
		if (ir.xesp != null)
		{
			//System.out.println("********** refr has parent XESP ");

			/*		Record baseRecord = master.getRecord(refr.NAME.formId);
			
					if (baseRecord.getRecordType().equals("STAT"))
					{
						System.out.println("0STAT of " + new STAT(baseRecord).MODL.model.str);
					}
					else if (baseRecord.getRecordType().equals("DOOR"))
					{
						System.out.println("0DOOR of " + new DOOR(baseRecord).MODL.model.str);
					}
					else
						System.out.println("0refer type " + baseRecord);
					int level = 1;*/

			XESP xesp = ir.xesp;

			while (xesp != null)
			{
				//	System.out.println("" + (level - 1) + "xesp " + xesp.parentId + " opposite? " + (xesp.flags & XESP.ENABLE_OPPOSITE_FLAG));

				// flip opp flag if required
				opp = ((xesp.flags & XESP.ENABLE_OPPOSITE_FLAG) != 0) ? !opp : opp;

				CommonREFR p1REFR = new CommonREFR(master.getRecord(xesp.parentId));

				//isOblivionEnable is just equal to the highest/last parent
				isParentEnable = p1REFR.isFlagSet(RECO.InitiallyDisabled_Flag);

				//	System.out.println("" + level + "refr " + p1REFR);
				//	System.out.println("" + level + "flags " + p1REFR.flags1 + " " + Integer.toBinaryString(p1REFR.flags1));

				/*		Record baseRecord2 = master.getRecord(p1REFR.NAME.formId);
				
						if (baseRecord2.getRecordType().equals("STAT"))
						{
							System.out.println("STAT of " + new STAT(baseRecord2).MODL.model.str);
						}
						else if (baseRecord2.getRecordType().equals("DOOR"))
						{
							System.out.println("DOOR of " + new DOOR(baseRecord2).MODL.model.str);
						}
						else
							System.out.println("" + level + "refer type " + baseRecord2);
				
						level++;*/
				xesp = p1REFR.xesp;
			}

		}

		// flip it if opp is set
		return opp ? !isParentEnable : isParentEnable;

	}
}
