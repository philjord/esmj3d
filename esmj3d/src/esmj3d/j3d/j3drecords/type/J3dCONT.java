package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import nif.NifToJ3d;
import nif.j3d.animation.J3dNiControllerManager;
import nif.j3d.animation.J3dNiControllerSequence;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;
import esmj3d.data.shared.records.GenericCONT;
import esmj3d.j3d.BethRenderSettings;

public class J3dCONT extends J3dRECOType
{
	private boolean isOpen = false;

	public J3dCONT(GenericCONT reco, boolean makePhys, MediaSources mediaSources)
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
			if (j3dNiAVObject instanceof Fadable)
			{
				((Fadable) j3dNiAVObject).setOutline(new Color3f(0.5f, 0.4f, 0f));
				if (!BethRenderSettings.isOutlineConts())
					((Fadable) j3dNiAVObject).setOutline(null);
			}

			addChild(j3dNiAVObject);
			fireIdle();
		}

	}

	@Override
	public void renderSettingsUpdated()
	{
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = BethRenderSettings.isOutlineConts() ? new Color3f(0.5f, 0.4f, 0f) : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;

		//Oblivion chest don't open!	
		J3dNiControllerManager ncm = j3dNiAVObject.getJ3dNiControllerManager();
		if (ncm != null)
		{
			J3dNiControllerSequence s = ncm.getSequence(isOpen ? "Open" : "Close");
			if (s != null)
			{
				s.fireSequenceOnce();
			}
		}
	}

	public boolean isOpen()
	{
		return isOpen;
	}
}
