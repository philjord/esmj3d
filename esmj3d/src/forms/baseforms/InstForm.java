package forms.baseforms;

import org.jogamp.java3d.Group;
import org.jogamp.vecmath.Vector3f;
import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;
import esmj3d.data.shared.subrecords.ZString;
import tools.io.ESMByteConvert;

/**
 * Inst records will actually appear in the world, so don't need to extend RECO an Inst record will have to have it's
 * type record attached at some point
 * 
 * I don't think this needs to be common with RECO except by a wildly generic interface to allow returning it, but any
 * given interface that asks for a InstRecord can have 2 method one of Inst and one for Type with
 */
public abstract class InstForm extends Group {
	
	public int			formId	= -1;

	public int			flags1	= 0;
	
	private String		EDID;										// only loaded if we are for the CK

	//Note: Divide X,Y by 4096 to get cell location
	protected Vector3f	loc		= new Vector3f();
	protected Vector3f	rot		= new Vector3f();
	protected float		scale	= 1f;

	public InstForm(Record record) {
		formId = record.getFormID();
		flags1 = record.getRecordFlags();
	}

	public int getRecordId() {
		return formId;
	}

	public boolean isFlagSet(int flagMask) {
		return (flags1 & flagMask) > 0;
	}
	
	protected void setEDID(Subrecord sr) {
		EDID = ZString.toString(sr.getSubrecordData());
	}

	public String getEDID() {
		return EDID;
	}

	protected void extractInstData(byte[] bs) {
		loc.x = ESMByteConvert.extractFloat(bs, 0);
		loc.y = ESMByteConvert.extractFloat(bs, 4);
		loc.z = ESMByteConvert.extractFloat(bs, 8);
		rot.x = ESMByteConvert.extractFloat(bs, 12);
		rot.y = ESMByteConvert.extractFloat(bs, 16);
		rot.z = ESMByteConvert.extractFloat(bs, 20);
	}

	public Vector3f getTrans() {
		return loc;
	}

	public Vector3f getTrans(Vector3f v) {
		if (v == null)
			v = new Vector3f();

		v.set(loc);
		return v;
	}

	public Vector3f getEulerRot() {
		return getEulerRot(rot);
	}

	public Vector3f getEulerRot(Vector3f er) {
		if (er == null)
			er = new Vector3f();

		er.set(rot);
		return er;
	}

	public float getScale() {
		return scale;
	}

}
