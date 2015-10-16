package esmj3d.j3d.j3drecords.type;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.WeakHashMap;

import javax.vecmath.Color3f;

import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;
import esmj3d.data.shared.records.RECO;

public class J3dRECOTypeGeneral extends J3dRECOType implements Fadable
{

	public J3dRECOTypeGeneral(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, nifFileName);
		j3dNiAVObject = loadNif(nifFileName, makePhys, mediaSources);
		if (j3dNiAVObject != null)
		{
			addChild(j3dNiAVObject);
			fireIdle();
		}
	}

	@Override
	public void renderSettingsUpdated()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void fade(float percent)
	{
		if (j3dNiAVObject != null && j3dNiAVObject instanceof Fadable)
		{
			((Fadable) j3dNiAVObject).fade(percent);
		}

	}
	
	@Override
	public void setOutline(Color3f c)
	{
		if (j3dNiAVObject != null && j3dNiAVObject instanceof Fadable)
		{
			((Fadable) j3dNiAVObject).setOutline(c);
		}
	}

	public static J3dNiAVObject loadNif(String nifFileName, boolean makePhys, MediaSources mediaSources)
	{
		J3dNiAVObject j3dNiAVObject;

		if (makePhys)
		{
			NifJ3dHavokRoot nhr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
			if (nhr == null)
				return null;
			j3dNiAVObject = nhr.getHavokRoot();
		}
		else
		{
			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
			if (nvr == null)
				return null;
			j3dNiAVObject = nvr.getVisualRoot();
		}

		setupDemoControllerTrigger(j3dNiAVObject);
		return j3dNiAVObject;

	}

	private static WeakHashMap<J3dNiAVObject, J3dNiAVObjectAnimationThread> threads = new WeakHashMap<J3dNiAVObject, J3dNiAVObjectAnimationThread>();

	private static void setupDemoControllerTrigger(J3dNiAVObject j3dNiAVObject)
	{
		// TODO: take this out it is for demo only
		if (j3dNiAVObject.getJ3dNiControllerManager() != null)
		{
			J3dNiAVObjectAnimationThread dat = threads.get(j3dNiAVObject);
			if (dat != null)
			{
				dat.addJ3dNiAVObject(j3dNiAVObject);
			}
			else
			{
				dat = new J3dNiAVObjectAnimationThread(j3dNiAVObject);
				threads.put(j3dNiAVObject, dat);
				dat.start();
			}
		}
	}

	private static class J3dNiAVObjectAnimationThread extends Thread
	{
		private WeakReference<J3dNiAVObject> masterJ3dNiAVObjectRef;

		private ArrayList<J3dNiAVObject> niToJ3dData = new ArrayList<J3dNiAVObject>();

		private String[] seqNames;

		public J3dNiAVObjectAnimationThread(J3dNiAVObject m)
		{
			setName("J3dRECOTypeGeneralAnimThread");
			setDaemon(true);
			masterJ3dNiAVObjectRef = new WeakReference<J3dNiAVObject>(m);
			seqNames = m.getJ3dNiControllerManager().getAllSequences();
		}

		public void addJ3dNiAVObject(J3dNiAVObject a)
		{
			niToJ3dData.add(a);
		}

		public void run()
		{
			try
			{
				//wait for go live
				while (masterJ3dNiAVObjectRef.get() != null && !masterJ3dNiAVObjectRef.get().isLive())
				{
					Thread.sleep(1000);
				}

				while (masterJ3dNiAVObjectRef.get() != null && !masterJ3dNiAVObjectRef.get().isLive())
				{
					for (int i = 0; i < seqNames.length; i++)
					{
						Thread.sleep((long) (Math.random() * 5000));
						String seqName = seqNames[i];
						masterJ3dNiAVObjectRef.get().getJ3dNiControllerManager().getSequence(seqName).fireSequenceOnce();
						for (J3dNiAVObject j3dNiAVObject : niToJ3dData)
						{
							j3dNiAVObject.getJ3dNiControllerManager().getSequence(seqName).fireSequenceOnce();
							//	 J3dNifSound J3dNifSound = new J3dNifSound(seq.getJ3dNiTextKeyExtraData().niTextKeyExtraData, master2);
						}
						Thread.sleep(masterJ3dNiAVObjectRef.get().getJ3dNiControllerManager().getSequence(seqName).getLengthMS());
					}
				}
			}
			catch (InterruptedException e)
			{
			}
		}
	}

}
