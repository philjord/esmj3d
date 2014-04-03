package esmj3d.j3d.j3drecords.type;

import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import nif.j3d.animation.J3dNiControllerManager;
import nif.j3d.animation.J3dNiControllerSequence;
import utils.source.MediaSources;
import esmj3d.data.shared.records.GenericCONT;

public class J3dCONT extends J3dRECOType
{
	private J3dNiAVObject j3dNiAVObject;

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
		addChild(j3dNiAVObject);
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
