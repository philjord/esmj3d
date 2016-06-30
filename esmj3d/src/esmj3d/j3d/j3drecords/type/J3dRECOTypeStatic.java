package esmj3d.j3d.j3drecords.type;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import utils.source.MediaSources;

public class J3dRECOTypeStatic extends J3dRECOType
{

	public J3dRECOTypeStatic(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, nifFileName);

		//ignore markers and targets for now (note only on load, not dynamic)
		if (!BethRenderSettings.isShowEditorMarkers() && nifFileName.toLowerCase().contains("marker"))
			return;

		if (makePhys)
		{
			NifJ3dHavokRoot nhr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
			if (nhr != null)
			{
				j3dNiAVObject = nhr.getHavokRoot();
				addChild(j3dNiAVObject);
				fireIdle();
			}
		}
		else
		{
			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
			if (nvr != null)
			{
				j3dNiAVObject = nvr.getVisualRoot();

				addChild(j3dNiAVObject);
				fireIdle(nvr);
			}
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
