package esmj3d.data.shared.records;

import java.util.ArrayList;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;

public abstract class RECO
{
	public static int Deleted_Flag = 0x00000020;

	public static int CastsShadows_Flag = 0x00000200;

	public static int QuestItem_Flag = 0x00000400;

	public static int PersistentReference_Flag = 0x00000400;

	public static int InitiallyDisabled_Flag = 0x00000800;

	public static int Ignored_Flag = 0x00001000;

	public static int VisibleWhenDistant_Flag = 0x00008000;

	public static int Dangerous_Flag = 0x00020000;

	public static int OffLimits_Flag = 0x00020000;

	public static int DataIsCompressed_Flag = 0x00040000;

	public static int CantWait_Flag = 0x00080000;

	public static int DisabledByDefault_Flag = 0x00080000;

	/* FROM TES5
	 Has Tree LOD = 0x00000040
	 Not On Local Map = 0x00000200
	 Has Distance LOD = 0x00008000
	 High Detail LOD Texture = 0x00020000
	 Has Currents = 0x00080000
	 Is Marker = 0x00800000
	 Obstacle = 0x02000000
	 Show On World Map = 0x10000000
	 */

	/*	header flags
		0x00000001 	 ESM file. (TES4.HEDR record only.)
		0x00000020 	Deleted
		0x00000200 	Casts shadows
		0x00000400 	Quest item / Persistent reference
		0x00000800 	Initially disabled
		0x00001000 	Ignored
		0x00008000 	Visible when distant
		0x00020000 	Dangerous / Off limits (Interior cell)
		0x00040000 	Data is compressed
		0x00080000 	Can't wait
		0x00080000 	Disabled By Default 
		*/

	public int formId = -1;

	public int flags1;

	public RECO(Record recordData)
	{
		formId = recordData.getFormID();
		flags1 = recordData.getRecordFlags1();
	}

	public int getRecordId()
	{
		return formId;
	}

	public String showDetails()
	{
		return "" + this.getClass() + " : (" + formId + "|" + Integer.toHexString(formId) + ") ";
	}

	public boolean isFlagSet(int flagMask)
	{
		return (flags1 & flagMask) > 0;
	}

	private int sri = 0;

	protected Subrecord next(ArrayList<Subrecord> subrecords)
	{
		return subrecords.get(sri++);
	}
}
