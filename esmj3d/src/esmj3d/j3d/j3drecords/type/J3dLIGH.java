package esmj3d.j3d.j3drecords.type;

import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Light;
import javax.media.j3d.PointLight;
import javax.media.j3d.SpotLight;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import nif.NifJ3dHavokRoot;
import nif.NifToJ3d;
import utils.ESConfig;
import utils.source.MediaSources;
import esmLoader.EsmFileLocations;
import esmj3d.data.shared.records.CommonLIGH;

public class J3dLIGH extends J3dRECOType
{

	public J3dLIGH(CommonLIGH ligh, boolean makePhys, MediaSources mediaSources)
	{
		super(ligh, ligh.MODL == null ? "" : ligh.MODL.model.str);

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
					j3dNiAVObject = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource())
							.getVisualRoot();
				}

				if (EsmFileLocations.ESM_MAKE_J3D_POINTLIGHTS)
				{
					Light light = null;

					if (ligh.fieldOfView == -1)
					{
						light = new PointLight(true, ligh.color, new Point3f(0, 0, 0), new Point3f(1, ligh.fade, ligh.falloffExponent));
					}
					else
					{
						light = new SpotLight(true, ligh.color, new Point3f(0, 0, 0), new Point3f(1, ligh.falloffExponent, 0),
								new Vector3f(0, 0, -1), ligh.fieldOfView, 0);
					}
					BoundingLeaf bl = new BoundingLeaf();
					bl.setRegion(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), ligh.radius * ESConfig.ES_TO_METERS_SCALE));
					light.setInfluencingBoundingLeaf(bl);
					addChild(bl);
					addChild(light);
				}

			}

			if (j3dNiAVObject != null)
			{
				addChild(j3dNiAVObject);
				fireIdle();
			}

		}

	}
}
