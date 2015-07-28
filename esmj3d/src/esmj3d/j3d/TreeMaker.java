package esmj3d.j3d;

import java.io.IOException;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderAttribute;
import javax.media.j3d.ShaderAttributeSet;
import javax.media.j3d.ShaderAttributeValue;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureUnitState;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;

import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import tools.WeakValueHashMap;
import tools3d.utils.scenegraph.LODBillBoard;
import utils.ESConfig;
import utils.source.MediaSources;
import utils.source.TextureSource;

import com.sun.j3d.utils.shader.StringIO;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOStatInst;
import esmj3d.j3d.j3drecords.type.J3dRECOTypeGeneral;

public class TreeMaker
{

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
			return J3dRECOTypeGeneral.loadNif(nifFileName, false, mediaSources);
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

				sg.addChild(createShapeX(sptFileName, billWidth, billHeight, textureSource));
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
			return createShapeX(sptFileName, billWidth, billHeight, textureSource);
		}
	}

	private static WeakValueHashMap<String, Appearance> loadedApps = new WeakValueHashMap<String, Appearance>();

	private static WeakValueHashMap<String, QuadArray> loadedGeoms = new WeakValueHashMap<String, QuadArray>();

	private static Shape3D createShapeX(String sptFileName, float billWidth, float billHeight, TextureSource textureSource)
	{
		String treeLODTextureName = sptFileName.substring(sptFileName.lastIndexOf("\\") + 1);
		treeLODTextureName = treeLODTextureName.substring(0, treeLODTextureName.indexOf(".spt")) + ".dds";

		Texture tex = textureSource.getTexture("textures\\trees\\billboards\\" + treeLODTextureName);

		String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
		Appearance app = loadedApps.get(keyString);
		QuadArray geom = loadedGeoms.get(keyString);
		if (app == null)
		{
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
			m.setLightingEnable(false);//TODO: why false lighting enable?
			app.setMaterial(m);

			loadedApps.put(keyString, app);

			geom = createGeometryX(billWidth, billHeight);
			loadedGeoms.put(keyString, geom);
		}

		Shape3D treeShape = new Shape3D();
		treeShape.setGeometry(geom);
		treeShape.setAppearance(app);

		return treeShape;
	}

	private static QuadArray createGeometryX(float rectWidth, float rectHeight)
	{
		float zPosition = 0f;

		float[] verts1 =
		{ rectWidth / 2, 0f, zPosition,//
				rectWidth / 2, rectHeight, zPosition,//
				-rectWidth / 2, rectHeight, zPosition,//
				-rectWidth / 2, 0f, zPosition//
				, //
				zPosition, 0f, rectWidth / 2,//
				zPosition, rectHeight, rectWidth / 2,//
				zPosition, rectHeight, -rectWidth / 2,//
				zPosition, 0f, -rectWidth / 2 };

		float[] texCoords =
		{ 0f, 1f, 0f, 0f, (1f), 0f, (1f), 1f//
				,//
				0f, 1f, 0f, 0f, (1f), 0f, (1f), 1f //
		};

		QuadArray rect = new QuadArray(8, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		rect.setCoordinates(0, verts1);
		rect.setTextureCoordinates(0, 0, texCoords);

		return rect;
	}

	//NOTE UNUSED
	protected static Group makeLODTreeBillboard2(String sptFileName, float billWidth, float billHeight, TextureSource textureSource)
	{
		String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
		SharedGroup sg = loadedLodXSharedGroups.get(keyString);

		if (sg == null && sptFileName.indexOf(".spt") != -1)
		{
			sg = new SharedGroup();

			String treeLODTextureName = sptFileName.substring(sptFileName.lastIndexOf("\\") + 1);
			treeLODTextureName = treeLODTextureName.substring(0, treeLODTextureName.indexOf(".spt")) + ".dds";

			Appearance app = new Appearance();

			TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
			transparencyAttributes.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
			transparencyAttributes.setTransparency(0f);

			RenderingAttributes ra = new RenderingAttributes();
			ra.setAlphaTestFunction(RenderingAttributes.GREATER);
			float threshold = 0.5f;
			ra.setAlphaTestValue(threshold);
			app.setRenderingAttributes(ra);

			app.setTransparencyAttributes(transparencyAttributes);

			Texture texture = textureSource.getTexture("textures\\trees\\billboards\\" + treeLODTextureName);
			app.setTexture(texture);

			Material m = new Material();
			m.setLightingEnable(false);
			app.setMaterial(m);

			Shape3D treeShape = new Shape3D();
			treeShape.setGeometry(createGeometry(billWidth, billHeight));
			treeShape.setAppearance(app);

			sg.addChild(treeShape);
			loadedLodXSharedGroups.put(keyString, sg);

		}

		TransformGroup billTrans = new TransformGroup();
		billTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		billTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

		if (sg != null)
		{
			Link link = new Link();
			link.setSharedGroup(sg);
			billTrans.addChild(link);
		}

		LODBillBoard billBehave = new LODBillBoard(billTrans);
		billBehave.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		billBehave.setEnable(true);
		billTrans.addChild(billBehave);

		return billTrans;
	}

	private static QuadArray createGeometry(float rectWidth, float rectHeight)
	{
		float zPosition = 0f;

		float[] verts1 =
		{ rectWidth / 2, 0f, zPosition, rectWidth / 2, rectHeight, zPosition, -rectWidth / 2, rectHeight, zPosition, -rectWidth / 2, 0f,
				zPosition };
		float[] texCoords =
		{ 0f, 1f, 0f, 0f, (1f), 0f, (1f), 1f };

		QuadArray rect = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		rect.setCoordinates(0, verts1);
		rect.setTextureCoordinates(0, 0, texCoords);

		return rect;
	}

	private static ShaderProgram shaderProgram = null;

	private static ShaderAttributeSet shaderAttributeSet = null;

	private static String vertexProgram = null;

	private static String fragmentProgram = null;

	protected static Appearance createAppearance(Texture tex)
	{
		Appearance app = null;
		if (!NifToJ3d.USE_SHADERS)
		{
			app = new Appearance();
		}
		else
		{
			app = new ShaderAppearance();

			if (shaderProgram == null)
			{
				try
				{
					vertexProgram = StringIO.readFully("./fixedpipeline.vert");
					fragmentProgram = StringIO.readFully("./fixedpipeline.frag");
				}
				catch (IOException e)
				{
					System.err.println(e);
				}

				Shader[] shaders = new Shader[2];
				shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
				shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
				final String[] shaderAttrNames =
				{ "tex" };
				final Object[] shaderAttrValues =
				{ new Integer(0) };
				shaderProgram = new GLSLShaderProgram();
				shaderProgram.setShaders(shaders);
				shaderProgram.setShaderAttrNames(shaderAttrNames);

				// Create the shader attribute set
				shaderAttributeSet = new ShaderAttributeSet();
				for (int i = 0; i < shaderAttrNames.length; i++)
				{
					ShaderAttribute shaderAttribute = new ShaderAttributeValue(shaderAttrNames[i], shaderAttrValues[i]);
					shaderAttributeSet.put(shaderAttribute);
				}

				// Create shader appearance to hold the shader program and
				// shader attributes
			}
			((ShaderAppearance) app).setShaderProgram(shaderProgram);
			((ShaderAppearance) app).setShaderAttributeSet(shaderAttributeSet);

		}

		TextureUnitState[] tus = new TextureUnitState[1];
		TextureUnitState tus0 = new TextureUnitState();
		tus0.setTexture(tex);
		tus[0] = tus0;
		app.setTextureUnitState(tus);

		app.setMaterial(J3dLAND.getLandMaterial());
		return app;
	}
}
