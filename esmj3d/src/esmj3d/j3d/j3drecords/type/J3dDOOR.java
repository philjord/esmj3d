package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import nif.NifToJ3d;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;
import esmj3d.data.shared.records.GenericDOOR;
import esmj3d.j3d.BethRenderSettings;

public class J3dDOOR extends J3dRECOType
{
	private boolean isOpen = false;

	public J3dDOOR(GenericDOOR reco, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, reco.MODL.model.str);

		if (makePhys)
		{
			j3dNiAVObject = NifToJ3d.loadHavok(reco.MODL.model.str, mediaSources.getMeshSource()).getHavokRoot();
		}
		else
		{
			j3dNiAVObject = NifToJ3d.loadShapes(reco.MODL.model.str, mediaSources.getMeshSource(), mediaSources.getTextureSource())
					.getVisualRoot();
		}

		if (j3dNiAVObject != null)
		{
			//prep for possible outlines later
			if (j3dNiAVObject instanceof Fadable && !makePhys)
			{
				((Fadable) j3dNiAVObject).setOutline(new Color3f(1.0f, 0.5f, 0f));
				if (!BethRenderSettings.isOutlineDoors())
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
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = BethRenderSettings.isOutlineDoors() ? new Color3f(1.0f, 0.5f, 0f) : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

	public void toggleOpen()
	{
		isOpen = !isOpen;
		j3dNiAVObject.getJ3dNiControllerManager().getSequence(isOpen ? "Open" : "Close").fireSequenceOnce();
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;
		j3dNiAVObject.getJ3dNiControllerManager().getSequence(isOpen ? "Open" : "Close").fireSequenceOnce();
	}

	public boolean isOpen()
	{
		return isOpen;
	}
}
