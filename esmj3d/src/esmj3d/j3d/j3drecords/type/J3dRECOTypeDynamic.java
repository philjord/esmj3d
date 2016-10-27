package esmj3d.j3d.j3drecords.type;

import org.jogamp.vecmath.Color3f;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;

public class J3dRECOTypeDynamic extends J3dRECOType
{
	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(0.5f, 0.4f, 0.6f);

	public J3dRECOTypeDynamic(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, nifFileName);

		//ignore markers and targets for now (note only on load, not dynamic)
		if (!BethRenderSettings.isShowEditorMarkers() && nifFileName.toLowerCase().contains("marker"))
			return;

		j3dNiAVObject = loadNif(nifFileName, makePhys, mediaSources);
		if (j3dNiAVObject != null)
		{

			//prep for possible outlines later
			if (j3dNiAVObject instanceof Fadable && !makePhys)
			{
				((Fadable) j3dNiAVObject).setOutline(outlineColor);
				((Fadable) j3dNiAVObject).setOutline(null);
			}

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
		outlineSetOn = b;
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = outlineSetOn ? outlineColor : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

}
