package esmj3d.j3d.j3drecords.type;

import javax.vecmath.Color3f;

import nif.character.NifCharacter;
import tools3d.utils.scenegraph.Fadable;
import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;

public class J3dRECOTypeCha extends J3dRECOType implements Fadable
{
	protected NifCharacter nifCharacter;

	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(1.0f, 1.0f, 0f);

	public J3dRECOTypeCha(RECO reco)
	{
		super(reco, null);
	}

	@Override
	public void renderSettingsUpdated()
	{
		super.renderSettingsUpdated();
		if (nifCharacter != null)
		{
			Color3f c = BethRenderSettings.isOutlineChars() || outlineSetOn ? outlineColor : null;
			nifCharacter.setOutline(c);
		}
	}

	@Override
	public void fade(float percent)
	{
		if (nifCharacter != null)
		{
			((Fadable) nifCharacter).fade(percent);
		}
	}

	@Override
	public void setOutline(Color3f c)
	{
		if (nifCharacter != null)
		{
			((Fadable) nifCharacter).setOutline(c);
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
				Color3f c = BethRenderSettings.isOutlineChars() || outlineSetOn ? outlineColor : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}
}
