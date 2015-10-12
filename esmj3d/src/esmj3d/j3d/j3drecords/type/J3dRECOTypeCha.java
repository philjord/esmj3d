package esmj3d.j3d.j3drecords.type;

import nif.character.NifCharacter;
import tools3d.utils.scenegraph.Fadable;
import esmj3d.data.shared.records.RECO;

public class J3dRECOTypeCha extends J3dRECOType implements Fadable
{
	protected NifCharacter nifCharacter;

	public J3dRECOTypeCha(RECO reco)
	{
		super(reco, null);
	}

	@Override
	public void renderSettingsUpdated()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void fade(float percent)
	{
		if (nifCharacter != null)
		{
			((Fadable) nifCharacter).fade(percent);
		}

	}		
}
