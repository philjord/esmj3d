package esmj3d.j3d.j3drecords.type;

import java.io.File;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;

import nif.compound.NifKey;
import nif.niobject.NiTextKeyExtraData;
import utils.source.SoundSource;
import esmj3d.data.shared.records.GenericSOUN;
import esmj3d.data.shared.records.RECO;
import esmmanager.common.data.record.IRecordStore;
import esmmanager.common.data.record.Record;

public class J3dSOUN extends J3dRECOType
{
	public J3dSOUN(GenericSOUN soun, IRecordStore master, SoundSource soundSource)
	{
		super(soun, null);
		if (soun.FNAM != null)
		{

			if (soun.FNAM.str.endsWith(".wav") || soun.FNAM.str.endsWith(".mp3"))
			{
				playSound(soun, soundSource);
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

	public void playSound(GenericSOUN soun, SoundSource soundSource)
	{
		//System.out.println("playing sound " + file);

		/*PointSound ps = new PointSound();

		ps.setSoundData(soundSource.getMediaContainer(soun.FNAM.str));
		ps.setPosition(new Point3f(0, 0, 0));
			if (soun.SNDX != null)
			{
				float maxGain = soun.SNDX.staticAttenuation / 100f;
				ps.setInitialGain(0);
				ps.setDistanceGain(new float[]
				{ 0, soun.SNDX.minimumAttenuationDistance * 5 * ESConfig.ES_TO_METERS_SCALE,
						soun.SNDX.maximumAttenuationDistance * 100 * ESConfig.ES_TO_METERS_SCALE }, new float[]
				{ maxGain, maxGain, 0 });
				ps.setEnable(true);
				ps.setPause(false);

				ps.setSchedulingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
				ps.setLoop(soun.SNDX.isLoop() ? -1 : 1);
				ps.setContinuousEnable(soun.SNDX.isLoop());
				addChild(ps);
			}
			else
			{
				PointSound ps = new PointSound();

		ps.setSoundData(soundSource.getMediaContainer(soun.FNAM.str));
		ps.setPosition(new Point3f(0, 0, 0));
		 
		ps.setEnable(true);
		ps.setPause(false);

		ps.setSchedulingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
		ps.setLoop(1);
		ps.setContinuousEnable(true);
			}*/

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
						Record sound = master.getRecord(soundName.substring(7));
						if (sound != null)
						{
							GenericSOUN soun = new GenericSOUN(sound);
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

		private void playSound(File file, GenericSOUN soun)
		{
			//System.out.println("playing sound " + file);
			/*		FileInputStream fis;
					try
					{
						PointSound ps = new PointSound();

						fis = new FileInputStream(file);

						ps.setSoundData(new MediaContainer(fis));
						ps.setPosition(new Point3f(0, 0, 0));
						float maxGain = soun.SNDX.staticAttenuation / 100f;
						ps.setInitialGain(0);
						ps.setDistanceGain(new float[]
						{ 0, soun.SNDX.minimumAttenuationDistance * 5 * ESConfig.ES_TO_METERS_SCALE,
								soun.SNDX.maximumAttenuationDistance * 100 * ESConfig.ES_TO_METERS_SCALE }, new float[]
						{ maxGain, maxGain, 0 });
						ps.setEnable(true);
						ps.setPause(false);

						ps.setSchedulingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
						ps.setLoop(soun.SNDX.isLoop() ? -1 : 1);
						ps.setContinuousEnable(soun.SNDX.isLoop());
						addChild(ps);
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}*/
		}

		private class SoundBehavior extends Behavior
		{
			private WakeupCriterion wakeupCriterion = new WakeupOnElapsedFrames(0);

			public void initialize()
			{
				wakeupOn(wakeupCriterion);
			}

			@SuppressWarnings(
			{ "unchecked", "rawtypes" })
			@Override
			public void processStimulus(Enumeration criteria)
			{

				wakeupOn(wakeupCriterion);
			}
		}
	}
}
