package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;

public class J3dRECOTypeActionable extends J3dRECOType
{
	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(0.5f, 0.4f, 0.9f);

	public J3dRECOTypeActionable(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
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

				//prep for possible outlines later
				if (j3dNiAVObject instanceof Fadable && !makePhys)
				{
					((Fadable) j3dNiAVObject).setOutline(outlineColor);
					((Fadable) j3dNiAVObject).setOutline(null);
				}

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
		outlineSetOn = b;
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = BethRenderSettings.isOutlineConts() || outlineSetOn ? outlineColor : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

}
