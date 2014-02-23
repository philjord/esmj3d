package esmj3d.j3d.j3drecords.type;

import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import utils.source.MeshSource;
import utils.source.TextureSource;
import esmj3d.data.shared.records.GenericDOOR;

public class J3dDOOR extends J3dRECOType
{
	private J3dNiAVObject j3dNiAVObject;

	private boolean isOpen = false;

	public J3dDOOR(GenericDOOR reco, boolean makePhys, MeshSource meshSource, TextureSource textureSource)
	{
		super(reco, reco.MODL.model.str);

		if (makePhys)
		{
			NifJ3dHavokRoot nhr = NifToJ3d.loadHavok(reco.MODL.model.str, meshSource);
			if (nhr != null)
				j3dNiAVObject = nhr.getHavokRoot();
		}
		else
		{
			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(reco.MODL.model.str, meshSource, textureSource);
			if (nvr != null)
				j3dNiAVObject = nvr.getVisualRoot();
		}
		addChild(j3dNiAVObject);
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
