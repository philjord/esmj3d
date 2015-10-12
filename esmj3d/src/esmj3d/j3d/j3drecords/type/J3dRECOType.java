package esmj3d.j3d.j3drecords.type;

import javax.media.j3d.BranchGroup;

import esmj3d.data.shared.records.RECO;

public abstract class J3dRECOType extends BranchGroup
{
	private int recordId = -1;

	public String physNifFile = "";

	public int getRecordId()
	{
		return recordId;
	}

	public J3dRECOType(RECO RECO, String physNifFile)
	{
		recordId = RECO.getRecordId();
		this.physNifFile = physNifFile;
	}

	public void renderSettingsUpdated()
	{
	}

	@Override
	public String toString()
	{
		return "" + this.getClass().getSimpleName() + " id " + recordId + " : " + physNifFile;
	}

}
