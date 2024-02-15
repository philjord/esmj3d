package esmj3d.j3d.j3drecords.type;

import org.jogamp.java3d.Link;
import org.jogamp.java3d.SharedGroup;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.character.NifJ3dSkeletonRoot;
import utils.source.MediaSources;

public class J3dRECOTypeStatic extends J3dRECOType {

	public J3dRECOTypeStatic(RECO reco, String nifFileName, boolean makePhys, MediaSources mediaSources) {
		super(reco, nifFileName, mediaSources);

		//ignore markers and targets for now (note only on load, not dynamic)
		if (!BethRenderSettings.isShowEditorMarkers() && nifFileName.toLowerCase().contains("marker"))
			return;

		if (makePhys) {
			NifJ3dHavokRoot nhr = NifToJ3d.loadHavok(nifFileName, mediaSources.getMeshSource());
			if (nhr != null) {
				j3dNiAVObject = nhr.getHavokRoot();
				addChild(j3dNiAVObject);
				fireIdle();
			}
		} else {
			// start with the shared option check cache
			if (SHARE_MODELS) {
				SharedGroup sg = loadedFiles.get(nifFileName);
				if (sg != null) {
					hit++;
					addChild(new Link(sg));
					return;
				}
			}

			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(),
					mediaSources.getTextureSource());
			if (nvr != null) {
				j3dNiAVObject = nvr.getVisualRoot();

				if (j3dNiAVObject != null) {

					boolean isSharable = checkTreeForSharable(j3dNiAVObject);

					if (!SHARE_MODELS	|| !isSharable || NifJ3dSkeletonRoot.isSkeleton(nvr.getNiToJ3dData())
						|| j3dNiAVObject.getJ3dNiControllerManager() != null) {
						addChild(j3dNiAVObject);
						fireIdle(nvr);
					} else {
						// cache miss has already happened
						SharedGroup sg = new SharedGroup();
						sg.addChild(j3dNiAVObject);
						loadedFiles.put(nifFileName, sg);
						miss++;
						if (miss % 100 == 0)
							System.out.println("J3dRECOTypeStatic hit " + hit + " miss " + miss);

						addChild(new Link(sg));
					}
				}
			}

		}

	}

	

	@Override
	public void renderSettingsUpdated() {
		super.renderSettingsUpdated();
	}

	@Override
	public void setOutlined(boolean b) {
		//ignored
	}

}
