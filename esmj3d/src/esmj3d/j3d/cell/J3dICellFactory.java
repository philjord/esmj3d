package esmj3d.j3d.cell;

import javax.media.j3d.BranchGroup;

public interface J3dICellFactory
{

	public String getMainESMFileName();

	public boolean isWRLD(int formId);

	public J3dICELLPersistent makeBGWRLDPersistent(int formId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDTemporary(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDTemporary(int wrldFormId, int x, int y, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDDistant(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGWRLDDistant(int wrldFormId, int x, int y, boolean makePhys);

	public J3dICELLPersistent makeBGInteriorCELLPersistent(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGInteriorCELLTemporary(int cellId, boolean makePhys);

	public J3dCELLGeneral makeBGInteriorCELLDistant(int cellId, boolean makePhys);

	public BranchGroup makeLODLandscape(int lodX, int lodY, int scale, int worldFormId, String worldFormName);

}
