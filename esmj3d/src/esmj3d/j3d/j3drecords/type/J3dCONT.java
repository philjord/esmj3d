package esmj3d.j3d.j3drecords.type;

import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import utils.source.MeshSource;
import utils.source.TextureSource;
import esmj3d.data.shared.records.GenericCONT;

public class J3dCONT extends J3dRECOType
{
	private J3dNiAVObject j3dNiAVObject;

	private boolean isOpen = false;

	public J3dCONT(GenericCONT reco, boolean makePhys, MeshSource meshSource, TextureSource textureSource)
	{
		super(reco, reco.MODL.model.str);

		if (makePhys)
		{
			j3dNiAVObject = NifToJ3d.loadHavok(reco.MODL.model.str, meshSource).getHavokRoot();
		}
		else
		{
			j3dNiAVObject = NifToJ3d.loadShapes(reco.MODL.model.str, meshSource, textureSource).getVisualRoot();
		}
		addChild(j3dNiAVObject);
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;

		//Oblivion chest don't open!		
		if (j3dNiAVObject.getJ3dNiControllerManager() != null)
		{
			j3dNiAVObject.getJ3dNiControllerManager().getSequence(isOpen ? "Open" : "Close").fireSequenceOnce();
		}
	}

	public boolean isOpen()
	{
		return isOpen;
	}
}
