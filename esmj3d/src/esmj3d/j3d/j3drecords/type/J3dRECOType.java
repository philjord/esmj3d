package esmj3d.j3d.j3drecords.type;

import javax.media.j3d.BranchGroup;

import nif.j3d.J3dNiAVObject;
import nif.j3d.animation.J3dNiControllerSequence;
import nif.j3d.particles.J3dNiParticleSystem;
import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;

public abstract class J3dRECOType extends BranchGroup
{
	private int recordId = -1;

	public String physNifFile = "";

	protected J3dNiAVObject j3dNiAVObject;

	public int getRecordId()
	{
		return recordId;
	}

	public J3dRECOType(RECO RECO, String physNifFile)
	{
		recordId = RECO.getRecordId();
		this.physNifFile = physNifFile;
	}

	public void renderSettingsUpdated()
	{
		J3dNiParticleSystem.setSHOW_DEBUG_LINES(BethRenderSettings.isOutlineParts());
	}

	protected void fireIdle()
	{
		//TODO: some texture transforms appear to be a bit shakey?
		//fire the first idle
		if (j3dNiAVObject.getJ3dNiControllerManager() != null)
		{
			String[] seqNames = j3dNiAVObject.getJ3dNiControllerManager().getAllSequences();
			for (String seqName : seqNames)
			{
				if (seqName.toLowerCase().indexOf("idle") != -1)
				{
					J3dNiControllerSequence seq = j3dNiAVObject.getJ3dNiControllerManager().getSequence(seqName);
					if (seq.isNotRunning())
						seq.fireSequence();
					else
						System.out.println("refiring " + seqName + " for " + j3dNiAVObject + " : "
								+ j3dNiAVObject.getNiAVObject().nVer.fileName);
					break;
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "" + this.getClass().getSimpleName() + " id " + recordId + " : " + physNifFile;
	}

}
