package esmj3d.j3d.j3drecords.type;

import org.jogamp.java3d.BoundingLeaf;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.SpotLight;
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
import utils.ESConfig;
import utils.source.MediaSources;

public class J3dGeneralLIGH extends J3dRECOType
{
	private Light light = null;

	private BoundingLeaf bl = new BoundingLeaf();

	public J3dGeneralLIGH(CommonLIGH ligh, boolean makePhys, MediaSources mediaSources)
	{
		super(ligh, ligh.MODL == null ? "" : ligh.MODL.model.str);

		Point3f lightPosition = new Point3f(0, 0, 0);
		if (ligh.MODL != null)
		{
			String nifFileName = ligh.MODL.model.str;
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

		if (!makePhys && BethRenderSettings.isEnablePlacedLights())
		{
			Color3f color = new Color3f(ligh.color.x / 255f, ligh.color.y / 255f, ligh.color.z / 255f);
			//System.out.println("new light " + color);
			//System.out.println("falls fade " + ligh.fade + " falloffExponent " + ligh.falloffExponent + " fieldOfView " + ligh.fieldOfView);
			//System.out.println("ligh.radius " + ligh.radius + " " + (ligh.radius * ESConfig.ES_TO_METERS_SCALE));
			if (ligh.fieldOfView == -1 || ligh.fieldOfView >= 90f)
			{
				light = new PointLight(true, color, lightPosition, new Point3f(1, ligh.fade, ligh.falloffExponent));
			}
			else
			{
				light = new SpotLight(true, color, lightPosition, new Point3f(1, ligh.fade, ligh.falloffExponent), new Vector3f(0, 0, -1), ligh.fieldOfView, 0);
			}
			light.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
			bl.setRegion(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), ligh.radius * ESConfig.ES_TO_METERS_SCALE));
			light.setInfluencingBoundingLeaf(bl);
			addChild(bl);
			addChild(light);
			
			// for debug visualizing the radius and color (badly)
			//Cube c =  new Cube(ligh.radius * ESConfig.ES_TO_METERS_SCALE );
			//c.setAppearance(new SimpleShaderAppearance(color));			
			//addChild(c);
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
			return new Vector3f(niAVObject.translation.x * ESConfig.ES_TO_METERS_SCALE, niAVObject.translation.z * ESConfig.ES_TO_METERS_SCALE,
					-niAVObject.translation.y * ESConfig.ES_TO_METERS_SCALE);
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
			if (BethRenderSettings.isEnablePlacedLights())
			{
				light.setInfluencingBoundingLeaf(bl);
			}
			else
			{
				light.setInfluencingBoundingLeaf(null);
			}
		}
	}
}
