package esmj3d.j3d.j3drecords.type;

import esmj3d.data.shared.records.GenericSOUN;
import esmj3d.data.shared.records.RECO;
import esmmanager.common.data.record.IRecordStore;
import nif.compound.NifKey;
import nif.niobject.NiTextKeyExtraData;
import utils.source.MediaSources;

public class J3dGeneralSOUN extends J3dRECOType
{
	public J3dGeneralSOUN(GenericSOUN soun, IRecordStore master, MediaSources mediaSources)
	{
		super(soun, null, mediaSources);
		if (soun.FNAM != null)
		{
			if (soun.FNAM.str.endsWith(".wav") || soun.FNAM.str.endsWith(".mp3"))
			{
				playSound(soun.FNAM.str, 10, -1);
			}
			else
			{
				// I presume I pick one at random, I will do for now
				//TODO: how does this work now????
				/*File[] fs = f.listFiles();
				if (fs.length > 0)
				{
					int idx = (int) (Math.random() * fs.length);
					idx = (idx == fs.length) ? 0 : idx;
					playSound(fs[idx], soun);
				}*/
			}

			//TODO: I possibly need to add resume pause methods etc?
		}
	}

	@Override
	public void setOutlined(boolean b)
	{
		//ignored
	}

	//Code found in fallout3 and tes5 for some odd reason
	public class J3dNifSound extends J3dRECOType
	{

		public J3dNifSound(RECO reco, NiTextKeyExtraData niTextKeyExtraData, IRecordStore master)
		{
			super(reco, null);
			/*if (soun.FNAM != null)
			 {
			 String soundPath = NifConstants.OBLIVION_BSA_ROOT + NifConstants.OBLIVION_SOUND_PATH + soun.FNAM.soundFileName;
			 File f = new File(soundPath);
			 
			 if (f.isFile())
			 {
			 playSound(f, soun);
			 }
			 else if (f.isDirectory())
			 {
			 // I presume I pick one at random, I will do for now
			 File[] fs = f.listFiles();
			 if (fs.length > 0)
			 {
			 int idx = (int) (Math.random() * fs.length);
			 idx = (idx == fs.length) ? 0 : idx;
			 playSound(fs[idx], soun);
			 }
			 }
			 else
			 {
			 System.out.println("what the hell is the file for the sound? " + f);
			 }*/

			NifKey[] textKeys = niTextKeyExtraData.textKeys;

			if (textKeys.length > 0)
			{

				for (int i = 0; i < textKeys.length; i++)
				{
					NifKey key = textKeys[i];

					//float time = key.time;
					String soundName = ((String) key.value);
					if (soundName.startsWith("sound:"))
					{
						//NOTE the sound name is a reference to an ESM EditorID for a sound					 
						//example farm fence open  = sound: DRSFarmFenceOpen
						//System.out.println("getting " + soundName.substring(7));
						//Record sound = master.getRecord(soundName.substring(7));

						//TODO: a proper sound system where sounds are indexed by name

						//if (sound != null)
						{
							//	GenericSOUN soun = new GenericSOUN(sound);
							//	System.out.println("SOUN " + sound.getEditorID());
						}
					}
				}

			}
		}

		@Override
		public void setOutlined(boolean b)
		{
			//ignored
		}

	}
}
