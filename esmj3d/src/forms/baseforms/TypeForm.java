package forms.baseforms;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.ZString;

/* Notice a TypeForm may contain enough info to create an sub tree of the 3d tree, a BranchGroup, but does not itself extend Group
 * This sort of TypeForm has some commonalities
 * note that a 3d instantiable group might use a shared group for something like a STAT or a new and distinct instance for an ACHR
 */
public abstract class TypeForm {
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

	public static int	Deleted_Flag				= 0x00000020;

	public static int	CastsShadows_Flag			= 0x00000200;

	public static int	QuestItem_Flag				= 0x00000400;

	public static int	PersistentReference_Flag	= 0x00000400;

	public static int	InitiallyDisabled_Flag		= 0x00000800;

	public static int	Ignored_Flag				= 0x00001000;

	public static int	VisibleWhenDistant_Flag		= 0x00008000;

	public static int	Dangerous_Flag				= 0x00020000;

	public static int	OffLimits_Flag				= 0x00020000;

	public static int	DataIsCompressed_Flag		= 0x00040000;

	public static int	CantWait_Flag				= 0x00080000;

	public static int	DisabledByDefault_Flag		= 0x00080000;

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
	public static int	HasTreeLOD_Flag				= 0x00000040;

	public static int	IsMarker_Flag				= 0x00800000;

	public int			formId						= -1;

	public int			flags1;

	private String		EDID;										// only loaded if we are for the CK

	public TypeForm(Record recordData) {
		formId = recordData.getFormID();
		flags1 = recordData.getRecordFlags();
	}

	protected void setEDID(Subrecord sr) {
		EDID = ZString.toString(sr.getSubrecordData());
	}

	public String getEDID() {
		return EDID;
	}

	public int getRecordId() {
		return formId;
	}

	public boolean isFlagSet(int flagMask) {
		return (flags1 & flagMask) > 0;
	}

	public String showDetails() {
		return toString();
	}

	@Override
	public String toString() {
		return ""	+ this.getClass() + " : (" + formId + "|" + Integer.toHexString(formId) + ") "
				+ (EDID != null ? EDID : "");
	}
}
