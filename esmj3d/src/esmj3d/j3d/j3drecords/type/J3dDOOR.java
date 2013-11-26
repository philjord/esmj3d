package esmj3d.j3d.j3drecords.type;

import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import utils.source.MeshSource;
import utils.source.TextureSource;
import esmj3d.data.shared.records.RECO;

public class J3dDOOR extends J3dRECOType
{
	private J3dNiAVObject j3dNiAVObject;

	private boolean isOpen = false;

	public J3dDOOR(RECO reco, String nifFileName, boolean makePhys, MeshSource meshSource, TextureSource textureSource)
	{
		super(reco, nifFileName);

		if (makePhys)
		{
			j3dNiAVObject = NifToJ3d.loadHavok(nifFileName, meshSource).getHavokRoot();
		}
		else
		{
			j3dNiAVObject = NifToJ3d.loadShapes(nifFileName, meshSource, textureSource).getVisualRoot();
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
