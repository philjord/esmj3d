package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public interface J3dRECOInst
{
	public int getRecordId();

	public J3dRECOType getJ3dRECOType();

	public Transform3D getLocation(Transform3D transform3d);

	/**
	 * NOTE J3d scaled (loc MUST include * ESConfig.ES_TO_METERS_SCALE multiplied)
	 */
	public void setLocation(Vector3f location, Quat4f rotation);

	public void renderSettingsUpdated();

	public InstRECO getInstRECO();
}
