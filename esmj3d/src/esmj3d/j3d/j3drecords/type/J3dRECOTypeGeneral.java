package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;
import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;

public class J3dRECOTypeGeneral extends J3dRECOType implements Fadable
{
	public J3dRECOTypeGeneral(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
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
	public void fade(float percent)
	{
		if (j3dNiAVObject != null && j3dNiAVObject instanceof Fadable)
		{
			((Fadable) j3dNiAVObject).fade(percent);
		}

	}

	@Override
	public void setOutline(Color3f c)
	{
		if (j3dNiAVObject != null && j3dNiAVObject instanceof Fadable)
		{
			((Fadable) j3dNiAVObject).setOutline(c);
		}
	}

	public static J3dNiAVObject loadNif(String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		J3dNiAVObject j3dNiAVObject;

		if (makePhys)
		{
			NifJ3dHavokRoot nhr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
			if (nhr == null)
				return null;
			j3dNiAVObject = nhr.getHavokRoot();
		}
		else
		{
			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
			if (nvr == null)
				return null;
			j3dNiAVObject = nvr.getVisualRoot();
		}

		return j3dNiAVObject;

	}
}
