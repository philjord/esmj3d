package esmj3d.j3d.j3drecords.inst;

import javax.media.j3d.Transform3D;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public interface J3dRECOInst
{
	public int getRecordId();

	public J3dRECOType getJ3dRECOType();

	public Transform3D getLocation(Transform3D transform3d);

	public void renderSettingsUpdated();

	public InstRECO getInstRECO();
}
