package esmj3d.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
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
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;

import tools.WeakValueHashMap;
import tools3d.utils.scenegraph.LODBillBoard;
import utils.source.TextureSource;

public class TreeMaker
{
	private static WeakValueHashMap<String, SharedGroup> loadedSharedGroups = new WeakValueHashMap<String, SharedGroup>();

	private static WeakValueHashMap<String, Appearance> loadedApps = new WeakValueHashMap<String, Appearance>();

	public static Node makeLODTreeX(String sptFileName, float billWidth, float billHeight, TextureSource textureSource)
	{
		String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
		SharedGroup sg = loadedSharedGroups.get(keyString);

		if (sg == null && sptFileName.indexOf(".spt") != -1)
		{
			sg = new SharedGroup();

			Appearance

			app = new Appearance();

			String treeLODTextureName = sptFileName.substring(sptFileName.lastIndexOf("\\") + 1);
			treeLODTextureName = treeLODTextureName.substring(0, treeLODTextureName.indexOf(".spt")) + ".dds";

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

			Texture texture = textureSource.getTexture("textures\\trees\\billboards\\" + treeLODTextureName);
			app.setTexture(texture);

			Material m = new Material();
			m.setLightingEnable(false);
			app.setMaterial(m);

			loadedApps.put(keyString, app);

			Shape3D treeShape = new Shape3D();
			treeShape.setGeometry(createGeometryX(billWidth, billHeight));
			treeShape.setAppearance(app);

			//return treeShape;
			
			sg.addChild(treeShape);
			sg.compile();
			loadedSharedGroups.put(keyString, sg);

		}

		Group g = new Group();

		if (sg != null)
		{
			g.addChild(new Link(sg));
		}

		return g;
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
		{ 0f, -1f, 0f, 0f, (-1f), 0f, (-1f), -1f//
				,//
				0f, -1f, 0f, 0f, (-1f), 0f, (-1f), -1f //
		};

		QuadArray rect = new QuadArray(8, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		rect.setCoordinates(0, verts1);
		rect.setTextureCoordinates(0, 0, texCoords);

		return rect;
	}

	//NOTE UNUSED
	public static Group makeLODTreeBillboard(String sptFileName, float billWidth, float billHeight, TextureSource textureSource)
	{
		String keyString = sptFileName + "_" + billWidth + "_" + billHeight;
		SharedGroup sg = loadedSharedGroups.get(keyString);

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
			loadedSharedGroups.put(keyString, sg);

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
		{ 0f, -1f, 0f, 0f, (-1f), 0f, (-1f), -1f };

		QuadArray rect = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		rect.setCoordinates(0, verts1);
		rect.setTextureCoordinates(0, 0, texCoords);

		return rect;
	}

}
