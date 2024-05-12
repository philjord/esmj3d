package esmj3d.j3d.j3drecords.type;

import org.jogamp.vecmath.Color3f;

import esmj3d.data.shared.records.GenericCONT;
import esmj3d.j3d.BethRenderSettings;
import nif.NifToJ3d;
import nif.j3d.animation.J3dNiControllerManager;
import nif.j3d.animation.J3dNiControllerSequence;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;

public class J3dCONT extends J3dRECOType
{
	private boolean isOpen = false;

	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(0.5f, 0.4f, 0f);

	public J3dCONT(GenericCONT reco, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, reco.MODL.model);

		if (makePhys)
		{
			j3dNiAVObject = NifToJ3d.loadHavok(reco.MODL.model, mediaSources.getMeshSource()).getHavokRoot();
		}
		else
		{
			j3dNiAVObject = NifToJ3d.loadShapes(reco.MODL.model, mediaSources.getMeshSource(), mediaSources.getTextureSource())
					.getVisualRoot();
		}

		if (j3dNiAVObject != null)
		{
			//prep for possible outlines later
			if (j3dNiAVObject instanceof Fadable && !makePhys)
			{
				((Fadable) j3dNiAVObject).setOutline(outlineColor);
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
		super.renderSettingsUpdated();
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = BethRenderSettings.isOutlineConts() || outlineSetOn ? outlineColor : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
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
