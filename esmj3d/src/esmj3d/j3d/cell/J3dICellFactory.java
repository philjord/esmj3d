package esmj3d.j3d.cell;

import javax.media.j3d.BranchGroup;

import utils.source.MediaSources;
import esmmanager.common.data.plugin.IMaster;
import esmmanager.common.data.record.IRecordStore;

public interface J3dICellFactory
{
	public void setSources(IMaster esmManager2, IRecordStore esmManager22, MediaSources mediaSources);

	public String getMainESMFileName();

	public J3dICELLPersistent makeBGWRLDPersistent(int formId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDTemporary(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDTemporary(int wrldFormId, int x, int y, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDDistant(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDDistant(int wrldFormId, int x, int y, boolean makePhys);

	public J3dICELLPersistent makeBGInteriorCELLPersistent(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGInteriorCELLTemporary(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGInteriorCELLDistant(int cellId, boolean makePhys);

	public String getLODWorldName(int worldFormId);

	public BranchGroup makeLODLandscape(int lodX, int lodY, int scale, String lodWorldName);

	public boolean isWRLD(int worldFormId);

}
