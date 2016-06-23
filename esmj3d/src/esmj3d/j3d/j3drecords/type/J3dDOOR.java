package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import esmj3d.data.shared.records.GenericDOOR;
import esmj3d.j3d.BethRenderSettings;
import nif.NifToJ3d;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;

public class J3dDOOR extends J3dRECOType
{
	private boolean isOpen = false;

	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(1.0f, 0.5f, 0f);

	public J3dDOOR(GenericDOOR reco, boolean makePhys, MediaSources mediaSources)
	{
		this(reco, makePhys, mediaSources, false);
	}

	public J3dDOOR(GenericDOOR reco, boolean makePhys, MediaSources mediaSources, boolean hasPivot)
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
				((Fadable) j3dNiAVObject).setOutline(outlineColor);
				if (!BethRenderSettings.isOutlineDoors())
					((Fadable) j3dNiAVObject).setOutline(null);
			}

			//TES3 pivot doors will add j3dNiAVObject to the pivot instead
			if (!hasPivot)
			{
				addChild(j3dNiAVObject);
			}
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
				Color3f c = BethRenderSettings.isOutlineDoors() || outlineSetOn ? outlineColor : null;
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
				Color3f c = BethRenderSettings.isOutlineDoors() || outlineSetOn ? outlineColor : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

	public void toggleOpen()
	{
		isOpen = !isOpen;
		animateDoor();
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;
		animateDoor();
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	/**
	 * called after open is set
	 */
	protected void animateDoor()
	{
		if (j3dNiAVObject.getJ3dNiControllerManager() != null)
		{
			j3dNiAVObject.getJ3dNiControllerManager().getSequence(isOpen ? "Open" : "Close").fireSequenceOnce();
		}

	}

}
