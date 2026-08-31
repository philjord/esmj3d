package esmj3d.j3d.j3drecords.type;

import org.jogamp.vecmath.Color3f;

import esmj3d.data.shared.records.GenericCONT;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
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

		String nifFileName = reco.MODL.model;
		if (makePhys)
		{
			NifJ3dHavokRoot hr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
			if (hr != null)
			{
				j3dNiAVObject = hr.getHavokRoot();
			}
		}
		else
		{
			if (nifFileName.length() > 0)
			{
				NifJ3dVisRoot vr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
				// not found messages will have already been published
				if (vr != null) {
					j3dNiAVObject = vr.getVisualRoot();
				}
			}			
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

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;

		//Oblivion chest don't open!	
		J3dNiControllerManager ncm = j3dNiAVObject.getJ3dNiControllerManager();
		if (ncm != null) {
			J3dNiControllerSequence s = ncm.getSequence(isOpen ? "Open" : "Close");
			if (s != null) {
				s.fireSequenceOnce();
			} else {
				System.out.println("Container open ahs no animation");
				System.out.println("looked for " + (isOpen ? "Open" : "Close"));
				String[] allSeq = ncm.getAllSequences();
				if (allSeq != null && allSeq.length > 0) {
					for (int i = 0; i < allSeq.length; i++) {
						System.out.println("Seq: " + allSeq[i]);
					}
				}
			}
		}
	}
	
 
	public void toggleOpen()
	{
		isOpen = !isOpen;
		setOpen(isOpen);
	}
	
	public boolean isOpen()
	{
		return isOpen;
	}

}
