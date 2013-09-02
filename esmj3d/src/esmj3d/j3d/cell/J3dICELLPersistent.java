package esmj3d.j3d.cell;

import java.util.List;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public interface J3dICELLPersistent
{
	public J3dRECOInst makeJ3dRECO(Record record);

	public void update(float charX, float charY, float loadDist);

	public List<GridSpace> getGridSpacesToAdd(float charX, float charY, float loadDist);

	public List<GridSpace> getGridSpacesToRemove(float charX, float charY, float loadDist);

	public void handleRecordCreate(Record record);

	public void handleRecordDelete(Record record);

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord);

	public J3dRECOInst getJ3dInstRECO(int recoId);

}
