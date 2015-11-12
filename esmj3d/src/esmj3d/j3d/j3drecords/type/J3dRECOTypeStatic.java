package esmj3d.j3d.j3drecords.type;

import utils.source.MediaSources;
import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;

public class J3dRECOTypeStatic extends J3dRECOType 
{
	public J3dRECOTypeStatic(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, nifFileName);

		//ignore markers and targets for now (note only on load, not dynamic)
		if (!BethRenderSettings.isShowEditorMarkers() && nifFileName.toLowerCase().contains("marker"))
			return;

		j3dNiAVObject = loadNif(nifFileName, makePhys, mediaSources);
		if (j3dNiAVObject != null)
		{
			addChild(j3dNiAVObject);
			fireIdle();
		}
	}

	@Override
	public void renderSettingsUpdated()
	{
		super.renderSettingsUpdated();
	}

	

	
	@Override
	public void setOutlined(boolean b)
	{
		//ignored
	}

}
