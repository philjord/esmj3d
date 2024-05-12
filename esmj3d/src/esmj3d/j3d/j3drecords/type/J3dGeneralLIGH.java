package esmj3d.j3d.j3drecords.type;

import java.util.Iterator;

import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingLeaf;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.SpotLight;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedTime;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import esmj3d.data.shared.records.CommonLIGH;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.j3d.NiToJ3dData;
import nif.niobject.NiAVObject;
import nif.niobject.NiNode;
import nif.niobject.NiObject;
import tools3d.utils.Utils3D;
import utils.ESConfig;
import utils.source.MediaSources;

public class J3dGeneralLIGH extends J3dRECOType
{
	private static final boolean showDebug = true;

	private Light light = null;

	private BoundingLeaf bl = new BoundingLeaf();

	private LightFlickerBehavior lightFlickerBehavior;

	private Color3f color;

	private float radius;

	private BranchGroup obg;

	public J3dGeneralLIGH(CommonLIGH ligh, boolean makePhys, MediaSources mediaSources)
	{
		super(ligh, ligh.MODL == null ? "" : ligh.MODL.model);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

		Point3f lightPosition = new Point3f(0, 0, 0);
		if (ligh.MODL != null)
		{
			String nifFileName = ligh.MODL.model;
			if (makePhys)
			{
				NifJ3dHavokRoot hr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
				if (hr != null)
				{
					j3dNiAVObject = hr.getHavokRoot();
				}
			}
			else
			{
				if (nifFileName.length() > 0)
				{
					NifJ3dVisRoot vr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
					j3dNiAVObject = vr.getVisualRoot();

					// let's find out where the attach point is
					Vector3f attachNode = findAttachLight(j3dNiAVObject.getNiAVObject(), vr.getNiToJ3dData());
					if (attachNode != null)
						lightPosition = new Point3f(attachNode);

				}
			}

			if (j3dNiAVObject != null)
			{
				addChild(j3dNiAVObject);
				fireIdle();
			}

		}

		if (!makePhys)
		{
			color = new Color3f(ligh.color.x / 255f, ligh.color.y / 255f, ligh.color.z / 255f);
			radius = ligh.radius * ESConfig.ES_TO_METERS_SCALE;
			//System.out.println("new light " + color);
			//System.out.println("falls fade " + ligh.fade + " falloffExponent " + ligh.falloffExponent + " fieldOfView " + ligh.fieldOfView);
			//System.out.println("ligh.radius " + ligh.radius + " " + (ligh.radius * ESConfig.ES_TO_METERS_SCALE));
			if (ligh.fieldOfView == -1 || ligh.fieldOfView >= 90f)
			{
				light = new PointLight(true, color, lightPosition, new Point3f(1, ligh.fade, ligh.falloffExponent));
			}
			else
			{
				light = new SpotLight(true, color, lightPosition, new Point3f(1, ligh.fade, ligh.falloffExponent), new Vector3f(0, 0, -1),
						ligh.fieldOfView, 0);
			}
			light.setCapability(Light.ALLOW_STATE_WRITE);
			light.setCapability(Light.ALLOW_COLOR_WRITE);
			bl.setRegion(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), radius));
			light.setInfluencingBoundingLeaf(bl);
			addChild(bl);
			addChild(light);

			// add the flickering effect in with a behaviour (just up and down intensity of each color randomly a bit)
			lightFlickerBehavior = new LightFlickerBehavior(light);
			lightFlickerBehavior.setSchedulingBounds(Utils3D.defaultBounds);
			addChild(lightFlickerBehavior);

			light.setEnable(BethRenderSettings.isEnablePlacedLights());
			lightFlickerBehavior.setEnable(BethRenderSettings.isEnablePlacedLights());
			setOutlineLights(BethRenderSettings.isOutlineLights());
		}

	}

	@Override
	public void setOutlined(boolean b)
	{
		//Ignored for now
	}

	public Vector3f findAttachLight(NiAVObject niAVObject, NiToJ3dData niToJ3dData)
	{
		//TODO: this should be the classic multiply up the chain gear
		if (niAVObject.name.equals("AttachLight"))
		{
			return new Vector3f(niAVObject.translation.x * ESConfig.ES_TO_METERS_SCALE,
					niAVObject.translation.z * ESConfig.ES_TO_METERS_SCALE, -niAVObject.translation.y * ESConfig.ES_TO_METERS_SCALE);
		}

		if (niAVObject instanceof NiNode)
		{
			NiNode niNode = (NiNode) niAVObject;
			for (int i = 0; i < niNode.numChildren; i++)
			{
				NiObject o = niToJ3dData.get(niNode.children[i]);
				if (o != null && o instanceof NiNode)
				{
					NiNode childNode = (NiNode) o;
					Vector3f v = findAttachLight(childNode, niToJ3dData);
					if (v != null)
						return v;

				}
			}
		}

		return null;
	}

	@Override
	public void renderSettingsUpdated()
	{
		super.renderSettingsUpdated();
		if (light != null)
		{
			light.setEnable(BethRenderSettings.isEnablePlacedLights());
			lightFlickerBehavior.setEnable(BethRenderSettings.isEnablePlacedLights());
			setOutlineLights(BethRenderSettings.isOutlineLights());
		}
	}

	private void setOutlineLights(boolean outline)
	{
		if (!outline)
		{
			if (obg != null)
			{
				obg.detach();
			}
		}
		else
		{
			if (obg == null)
			{
				obg = new BranchGroup();
				obg.setCapability(BranchGroup.ALLOW_DETACH);
				obg.setCapability(Group.ALLOW_PARENT_READ);
				//OK first issue, light bounds are interesecting with transparent tree leaves and 
				// morphable characters because those guys have a real starange bounds
				// that bounds should not be so strange, not sure how to do it fast
				// attanuation will in fact fix that issue
				// but a debug system of bounds sphere display would help a lot
				// need a simple primitive or sphere

				// for debug visualizing the radius and color 

				Sphere s = new Sphere(radius, new SimpleShaderAppearance(color));
				obg.addChild(s);
				Cube c = new Cube(0.1);
				c.setAppearance(new SimpleShaderAppearance(color));
				obg.addChild(c);
				Cube c2 = new Cube(0.05);
				c2.setAppearance(new SimpleShaderAppearance(color));
				obg.addChild(c2);
			}
			
			
			if(obg.getParent() == null)
				addChild(obg);
		}

	}

	private class LightFlickerBehavior extends Behavior
	{
		private Light lightToFlicker;
		private Color3f originalColor = new Color3f();
		private Color3f updateColor = new Color3f();

		private WakeupOnElapsedTime wakeUp;

		public LightFlickerBehavior(Light lightToFlicker)
		{
			this.lightToFlicker = lightToFlicker;
			lightToFlicker.getColor(originalColor);
			wakeUp = new WakeupOnElapsedTime(50);
		}

		@Override
		public void initialize()
		{
			wakeupOn(wakeUp);
		}

		@Override
		public void processStimulus(Iterator<WakeupCriterion> critiria)
		{
			float dr = (float) ((Math.random() * 0.2) - 0.1);
			float dg = (float) ((Math.random() * 0.2) - 0.1);
			float db = (float) ((Math.random() * 0.2) - 0.1);
			updateColor.x = originalColor.x * (1f + dr);
			updateColor.y = originalColor.y * (1f + dg);
			updateColor.z = originalColor.z * (1f + db);
			lightToFlicker.setColor(updateColor);

			//reset the wakeup
			wakeupOn(wakeUp);
		}
	}
}
