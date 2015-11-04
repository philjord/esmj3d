package esmj3d.j3d.j3drecords.type;

import javax.media.j3d.Alpha;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;

import nif.NifToJ3d;
import tools3d.utils.TimedRunnableBehavior;
import tools3d.utils.scenegraph.Fadable;
import utils.source.MediaSources;
import esmj3d.data.shared.records.GenericDOOR;
import esmj3d.j3d.BethRenderSettings;

public class J3dDOOR extends J3dRECOType
{
	private boolean isOpen = false;

	private Alpha alpha;

	private TimedRunnableBehavior pivotBehavior = new TimedRunnableBehavior(10);

	TransformGroup doorPivot = new TransformGroup();

	public J3dDOOR(GenericDOOR reco, boolean makePhys, MediaSources mediaSources)
	{
		super(reco, reco.MODL.model.str);

		if (makePhys)
		{
			j3dNiAVObject = NifToJ3d.loadHavok(reco.MODL.model.str, mediaSources.getMeshSource()).getHavokRoot();
		}
		else
		{
			j3dNiAVObject = NifToJ3d.loadShapes(reco.MODL.model.str, mediaSources.getMeshSource(), mediaSources.getTextureSource())
					.getVisualRoot();
		}

		if (j3dNiAVObject != null)
		{
			//prep for possible outlines later
			if (j3dNiAVObject instanceof Fadable && !makePhys)
			{
				((Fadable) j3dNiAVObject).setOutline(new Color3f(1.0f, 0.5f, 0f));
				if (!BethRenderSettings.isOutlineDoors())
					((Fadable) j3dNiAVObject).setOutline(null);
			}

			//TES3 has no animations for doors, so I'ma just pivot around Y!
			if (j3dNiAVObject.getJ3dNiControllerManager() == null)
			{
				doorPivot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				doorPivot.addChild(j3dNiAVObject);
				addChild(doorPivot);

				doorPivot.addChild(pivotBehavior);
			}
			else
			{
				addChild(j3dNiAVObject);

			}
			fireIdle();
		}

	}

	@Override
	public void renderSettingsUpdated()
	{
		super.renderSettingsUpdated();
		if (j3dNiAVObject != null)
		{
			if (j3dNiAVObject instanceof Fadable)
			{
				Color3f c = BethRenderSettings.isOutlineDoors() ? new Color3f(1.0f, 0.5f, 0f) : null;
				((Fadable) j3dNiAVObject).setOutline(c);
			}
		}
	}

	public void toggleOpen()
	{
		isOpen = !isOpen;
		animateDoor();
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;
		animateDoor();
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	/**
	 * called after open is set
	 */
	private void animateDoor()
	{
		if (j3dNiAVObject.getJ3dNiControllerManager() != null)
			j3dNiAVObject.getJ3dNiControllerManager().getSequence(isOpen ? "Open" : "Close").fireSequenceOnce();
		else
		{
			//wow TES3 door have no animation, they look like they just artifically pivot around 
			alpha = new Alpha(1, 500);
			alpha.setStartTime(System.currentTimeMillis());

			Runnable callback = new Runnable()
			{
				public void run()
				{
					Transform3D t = new Transform3D();
					double a = (Math.PI / 2f) * alpha.value();
					a = isOpen ? a : (Math.PI / 2f) - a;
					t.rotY(a);
					doorPivot.setTransform(t);
				}
			};
			pivotBehavior.start(50, callback);
		}
	}

}
