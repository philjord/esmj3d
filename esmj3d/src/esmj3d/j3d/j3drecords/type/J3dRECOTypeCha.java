package esmj3d.j3d.j3drecords.type;

import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.vecmath.Color3f;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import nif.character.NifCharacter;
import tools3d.utils.PhysAppearance;
import tools3d.utils.scenegraph.Fadable;

public class J3dRECOTypeCha extends J3dRECOType implements Fadable
{
	protected NifCharacter nifCharacter;

	private boolean outlineSetOn = false;

	private Color3f outlineColor = new Color3f(1.0f, 1.0f, 0f);

	public J3dRECOTypeCha(RECO reco, boolean makePhys)
	{
		super(reco, null);

		// if physics just put a box on to to make debug easier
		if (makePhys)
		{
			Cube cube = new Cube(0.5, 2, 0.5);
			cube.setAppearance(PhysAppearance.makeAppearance());
			addChild(cube);
		}
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

	public NifCharacter getNifCharacter()
	{
		return nifCharacter;
	}

	protected void addIdleAnimations(ArrayList<String> idleAnimations, List<String> filesInFolder, String[] wildcards)
	{
		for (String fireName : filesInFolder)
		{
			for(String wildcard : wildcards) {
				if (fireName.toLowerCase().contains(wildcard))
					idleAnimations.add(fireName);
			}
		}

	}

}
