package esmj3d.j3d.j3drecords.type;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.WeakHashMap;

import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.SharedGroup;

import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import tools.WeakValueHashMap;
import tools3d.utils.scenegraph.SharedGroupCapableChecker;
import utils.source.MeshSource;
import utils.source.TextureSource;
import esmj3d.data.shared.records.RECO;

public class J3dRECOTypeGeneral extends J3dRECOType
{

	public J3dRECOTypeGeneral(RECO reco, String nifFileName, boolean makePhys, MeshSource meshSource, TextureSource textureSource)
	{
		super(reco, nifFileName);
		Node node = loadSharedGroup(nifFileName, makePhys, meshSource, textureSource);
		addChild(node);
	}

	private static WeakValueHashMap<String, SharedGroup> loadedSharedGroups = new WeakValueHashMap<String, SharedGroup>();

	public static Node loadSharedGroup(String nifFileName, boolean makePhys, MeshSource meshSource, TextureSource textureSource)
	{
		String keyString = nifFileName + (makePhys ? "_phys" : "");// keep phys seperate

		SharedGroup sg = loadedSharedGroups.get(keyString);

		if (sg != null)
		{
			Link l = new Link();
			l.setSharedGroup(sg);
			return l;
		}
		else
		{
			J3dNiAVObject j3dNiAVObject;

			if (makePhys)
			{
				j3dNiAVObject = NifToJ3d.loadHavok(nifFileName, meshSource).getHavokRoot();
			}
			else
			{
				j3dNiAVObject = NifToJ3d.loadShapes(nifFileName, meshSource, textureSource).getVisualRoot();
			}

			if (SharedGroupCapableChecker.canBeShared(j3dNiAVObject))
			{
				sg = new SharedGroup();

				sg.addChild(j3dNiAVObject);
				loadedSharedGroups.put(keyString, sg);

				Link l = new Link();
				l.setSharedGroup(sg);
				return l;

			}
			else
			{
				setupDemoControllerTrigger(j3dNiAVObject);
				return j3dNiAVObject;
			}

		}

	}

	private static WeakHashMap<J3dNiAVObject, J3dNiAVObjectAnimationThread> threads = new WeakHashMap<J3dNiAVObject, J3dNiAVObjectAnimationThread>();

	public static void setupDemoControllerTrigger(J3dNiAVObject j3dNiAVObject)
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

	@Override
	public void renderSettingsUpdated()
	{
		// TODO Auto-generated method stub
		
	}
}
