package esmj3d.j3d.trees;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Vector3f;

import nif.j3d.J3dNiAVObject;
import tools.WeakValueHashMap;
import utils.ESConfig;
import utils.source.MediaSources;
import utils.source.TextureSource;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.LODNif;
import esmj3d.j3d.j3drecords.inst.J3dRECOStatInst;
import esmj3d.j3d.j3drecords.type.J3dRECOTypeGeneral;

public class TreeMaker
{

	/** far trees need to be trival TG with shape and no BG or other over heads
	 * This is ULTRA optomized, location is baked into X verts, No Groups involved
	 */
	public static Node makeTreeFar(InstRECO inst, boolean makePhys, MediaSources mediaSources, String treeName, float billBoardWidth,
			float billBoardHeight)
	{
		if (!makePhys)
		{
			if (treeName.endsWith(".spt"))
			{
				// give it the InstREco to prebake
				Node node = createShapeX(treeName, billBoardWidth * ESConfig.ES_TO_METERS_SCALE, billBoardHeight
						* ESConfig.ES_TO_METERS_SCALE, mediaSources.getTextureSource(), inst);
				return node;
			}
			else
			{
				String treeLodFlat = treeName.substring(0, treeName.indexOf(".nif")) + "_lod_flat.nif";

				if (mediaSources.getMeshSource().nifFileExists(treeLodFlat))
				{
					J3dRECOStatInst j3dinst = new J3dRECOStatInst(inst, false, makePhys);
					j3dinst.addNodeChild(new LODNif(treeLodFlat, mediaSources));
					return j3dinst;
				}
			}
		}
		else
		{
			// no physics for far trees ever!
			System.out.println("Far tree asked for with makePhys true");
		}
		return null;
	}

	public static J3dRECOStatInst makeTree(InstRECO inst, boolean makePhys, MediaSources mediaSources, String treeName,
			float billBoardWidth, float billBoardHeight, boolean far)
	{
		String nifTreeFileName = null;
		//firstly deal with spt file (speed tree) for oblic and fallouts
		if (treeName.endsWith(".spt"))
		{
			// see if we have a map of a skyrim tree
			String sptFileName = treeName;

			String skyrimReplacer = SkyrimTreeLookup.getLookup(sptFileName);
			//if we do not have the skyrim bsa or the cut down skyrimTrees bsa ignore
			if (skyrimReplacer != null && !mediaSources.getMeshSource().nifFileExists(skyrimReplacer))
				skyrimReplacer = null;

			//fars always just show original far
			if (!far && skyrimReplacer != null)
			{
				nifTreeFileName = skyrimReplacer;
			}
			else
			{
				if (!makePhys)
				{
					J3dRECOStatInst j3dinst = new J3dRECOStatInst(inst, false, makePhys);
					Node node = makeLODTreeX(sptFileName, billBoardWidth * ESConfig.ES_TO_METERS_SCALE, billBoardHeight
							* ESConfig.ES_TO_METERS_SCALE, mediaSources.getTextureSource());
					j3dinst.addNodeChild(node);
					return j3dinst;
				}
				else
				{
					// no physics for near by X style trees, sad but true
					return null;
				}
			}
		}
		else
		{
			nifTreeFileName = treeName;
		}

		if (nifTreeFileName != null)
		{
			//Attempt to make a Skyrim tree now

			//TODO: tree models themselves have cool animated and non-animated version inside,
			//must work out how to switch
			String treeLodFlat = nifTreeFileName.substring(0, nifTreeFileName.indexOf(".nif")) + "_lod_flat.nif";

			if (!makePhys && mediaSources.getMeshSource().nifFileExists(treeLodFlat))
			{
				if (!far)
				{
					J3dRECOStatInst j3dinst = new J3dRECOStatInst(inst, true, makePhys);
					j3dinst.setJ3dRECOType(//
							new J3dRECOTypeGeneral(inst, nifTreeFileName, makePhys, mediaSources),//							
							makeFlatLodTree(treeLodFlat, mediaSources));
					return j3dinst;
				}
				else
				{
					J3dRECOStatInst j3dinst = new J3dRECOStatInst(inst, false, makePhys);
					j3dinst.addNodeChild(makeFlatLodTree(treeLodFlat, mediaSources));
					return j3dinst;
				}
			}
			else
			{
				J3dRECOStatInst j3dinst = new J3dRECOStatInst(inst, true, makePhys);
				j3dinst.setJ3dRECOType(new J3dRECOTypeGeneral(inst, nifTreeFileName, makePhys, mediaSources));
				return j3dinst;
			}

		}
		else
		{
			System.err.println("bad tree name " + treeName + " makePhys " + makePhys);
		}
		return null;

	}

	//NOTE! before you enable this bad boy notice that scale factors in the instRECO may be be handled properly
	private static boolean ENABLE_SG = false;

	private static WeakValueHashMap<String, SharedGroup> loadedFlatLodSharedGroups = new WeakValueHashMap<String, SharedGroup>();

	public static BranchGroup makeFlatLodTree(String nifFileName, MediaSources mediaSources)
	{
		if (ENABLE_SG)
		{
			String keyString = nifFileName;
			SharedGroup sg = loadedFlatLodSharedGroups.get(keyString);

			if (sg == null && nifFileName.indexOf(".nif") != -1)
			{
				sg = new SharedGroup();

				J3dNiAVObject nif = J3dRECOTypeGeneral.loadNif(nifFileName, false, mediaSources);
				sg.addChild(nif);
				loadedFlatLodSharedGroups.put(keyString, sg);

			}

			BranchGroup bg = new BranchGroup();

			if (sg != null)
			{
				bg.addChild(new Link(sg));
			}

			return bg;
		}
		else
		{
			return J3dRECOTypeGeneral.loadNif(nifFileName, false, mediaSources).getRootNode();
		}
	}

	private static WeakValueHashMap<String, SharedGroup> loadedLodXSharedGroups = new WeakValueHashMap<String, SharedGroup>();

	public static Node makeLODTreeX(String sptFileName, float billWidth, float billHeight, TextureSource textureSource)
	{

		if (ENABLE_SG)
		{
			String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
			SharedGroup sg = loadedLodXSharedGroups.get(keyString);

			if (sg == null && sptFileName.indexOf(".spt") != -1)
			{
				sg = new SharedGroup();

				sg.addChild(createShapeX(sptFileName, billWidth, billHeight, textureSource, null));
				sg.compile();
				loadedLodXSharedGroups.put(keyString, sg);

			}

			Group g = new Group();

			if (sg != null)
			{
				g.addChild(new Link(sg));
			}

			return g;
		}
		else
		{
			return createShapeX(sptFileName, billWidth, billHeight, textureSource, null);
		}
	}

	private static WeakValueHashMap<String, Appearance> loadedApps = new WeakValueHashMap<String, Appearance>();

	private static Shape3D createShapeX(String sptFileName, float billWidth, float billHeight, TextureSource textureSource, InstRECO ir)
	{
		String treeLODTextureName = sptFileName.substring(sptFileName.lastIndexOf("\\") + 1);
		treeLODTextureName = treeLODTextureName.substring(0, treeLODTextureName.indexOf(".spt")) + ".dds";

		String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
		Appearance app = loadedApps.get(keyString);
		QuadArray geom = createGeometryX(billWidth, billHeight, ir);
		if (app == null)
		{
			Texture tex = textureSource.getTexture("textures\\trees\\billboards\\" + treeLODTextureName);

			app = createAppearance(tex);

			PolygonAttributes pa = new PolygonAttributes();
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			app.setPolygonAttributes(pa);

			TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
			transparencyAttributes.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
			transparencyAttributes.setTransparency(0f);

			RenderingAttributes ra = new RenderingAttributes();
			ra.setAlphaTestFunction(RenderingAttributes.GREATER);
			float threshold = 0.5f;
			ra.setAlphaTestValue(threshold);
			app.setRenderingAttributes(ra);

			app.setTransparencyAttributes(transparencyAttributes);

			Material m = new Material();
			m.setLightingEnable(false);//TODO: why false lighting enable? speed appears to be unaffected?
			app.setMaterial(m);

			loadedApps.put(keyString, app);
		}

		Shape3D treeShape = new Shape3D();
		treeShape.setGeometry(geom);
		treeShape.setAppearance(app);

		return treeShape;
	}

	/**
	 * Non null ir mean pre bake InstRECO coords into verts
	 * @param rectWidth
	 * @param rectHeight
	 * @param ir
	 * @return
	 */
	private static QuadArray createGeometryX(float rectWidth, float rectHeight, InstRECO ir)
	{

		float x = 0;
		float y = 0;
		float z = 0;
		if (ir != null)
		{
			rectWidth = rectWidth * ir.getScale();
			rectHeight = rectHeight * ir.getScale();

			Vector3f t = ir.getTrans();
			x = t.x * ESConfig.ES_TO_METERS_SCALE;
			y = t.z * ESConfig.ES_TO_METERS_SCALE;
			z = -t.y * ESConfig.ES_TO_METERS_SCALE;
		}

		float zPosition = 0f;

		float[] verts1 =
		{ x + (rectWidth / 2), y + 0f, z + zPosition,//
				x + (rectWidth / 2), y + rectHeight, z + zPosition,//
				x + (-rectWidth / 2), y + rectHeight, z + zPosition,//
				x + (-rectWidth / 2), y + 0f, z + zPosition//
				, //
				x + zPosition, y + 0f, z + (rectWidth / 2),//
				x + zPosition, y + rectHeight, z + (rectWidth / 2),//
				x + zPosition, y + rectHeight, z + (-rectWidth / 2),//
				x + zPosition, y + 0f, z + (-rectWidth / 2) };

		float[] texCoords =
		{ 0f, 1f, 0f, 0f, (1f), 0f, (1f), 1f//
				,//
				0f, 1f, 0f, 0f, (1f), 0f, (1f), 1f //
		};

		//probably should add normals too for speed?otherwise auto generated or something
		float[] normals =
		{ 0f, 0f, 1f, //
				0f, 0f, 1f, //
				0f, 0f, 1f, //
				0f, 0f, 1f, //
				1f, 0f, 0f, //
				1f, 0f, 0f, //
				1f, 0f, 0f, //
				1f, 0f, 0f, //
		};

		//TODO: should try filling in with interleaving etc
		QuadArray rect = new QuadArray(8, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.NORMALS);
		rect.setCoordinates(0, verts1);
		rect.setTextureCoordinates(0, 0, texCoords);
		rect.setNormals(0, normals);
		return rect;
	}

	private static Appearance createAppearance(Texture tex)
	{
		Appearance app = new Appearance();

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		tus[0] = tus0;
		app.setTextureUnitState(tus);

		app.setMaterial(getMaterial());
		return app;
	}

	public static Material getMaterial()
	{

		Material m = new Material();

		m.setShininess(1.0f); // trees is  very shiny, generally
		m.setDiffuseColor(0.5f, 0.6f, 0.5f);
		m.setSpecularColor(0.0f, 0.0f, 0.0f);
		m.setColorTarget(Material.AMBIENT_AND_DIFFUSE);

		return m;
	}
}
