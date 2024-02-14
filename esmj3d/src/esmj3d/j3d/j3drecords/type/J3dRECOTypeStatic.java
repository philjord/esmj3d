package esmj3d.j3d.j3drecords.type;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.jogamp.java3d.AlternateAppearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingLeaf;
import org.jogamp.java3d.Clip;
import org.jogamp.java3d.Fog;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.ModelClip;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.SharedGroup;
import org.jogamp.java3d.Soundscape;
import org.jogamp.java3d.ViewPlatform;

import esmj3d.data.shared.records.RECO;
import esmj3d.j3d.BethRenderSettings;
import nif.NifJ3dHavokRoot;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.character.NifJ3dSkeletonRoot;
import tools.WeakValueHashMap;
import utils.source.MediaSources;

public class J3dRECOTypeStatic extends J3dRECOType {
	public static boolean					SHARE_MODELS	= true;
	//FIXME: for all static I should now, hold the loaded visualroot and use a shared node to place it in the world
	// might make a bunch of things faster and better?

	private static Map<String, SharedGroup>	loadedFiles		= Collections
			.synchronizedMap(new WeakValueHashMap<String, SharedGroup>());
	static int								hit				= 0;
	static int								miss			= 0;

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
							System.out.println("hit " + hit + " miss " + miss);

						addChild(new Link(sg));
					}
				}
			}

		}

	}

	private static boolean checkTreeForSharable(Node node) {
		/*
				 * An IllegalSharingException is thrown if any of the following leaf nodes
				 * appear in a shared subgraph:<P>
				 * <UL>
				 * <LI>AlternateAppearance</LI>
				 * <LI>Background</LI>
				 * <LI>Behavior</LI>
				 * <LI>BoundingLeaf</LI>
				 * <LI>Clip</LI>
				 * <LI>Fog</LI>
				 * <LI>ModelClip</LI>
				 * <LI>Soundscape</LI>
				 * <LI>ViewPlatform</LI></UL>*/

		if (node instanceof AlternateAppearance || node instanceof Background || node instanceof Behavior
			|| node instanceof BoundingLeaf || node instanceof Clip || node instanceof Fog || node instanceof ModelClip
			|| node instanceof Soundscape || node instanceof ViewPlatform) {
			return false;
		} else {
			if (node instanceof Group) {
				Iterator<Node> it = ((Group)node).getAllChildren();
				while (it.hasNext()) {
					Node nex = it.next();
					boolean sharableChildren = checkTreeForSharable(nex);
					if (!sharableChildren)
						return false;
				}
			}
		}

		return true;
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
