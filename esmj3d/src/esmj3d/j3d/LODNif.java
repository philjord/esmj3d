package esmj3d.j3d;

import org.jogamp.java3d.Group;

import nif.NifFile;
import nif.NifToJ3d;
import nif.j3d.J3dBSTriShape;
import nif.j3d.J3dNiTriBasedGeom;
import nif.j3d.J3dNiTriShape;
import nif.j3d.J3dNiTriStrips;
import nif.j3d.NiToJ3dData;
import nif.niobject.NiAVObject;
import nif.niobject.NiNode;
import nif.niobject.NiObject;
import nif.niobject.NiTriShape;
import nif.niobject.NiTriStrips;
import nif.niobject.bs.BSFadeNode;
import nif.niobject.bs.BSLODTriShape;
import nif.niobject.bs.BSTriShape;
import utils.source.MediaSources;
import utils.source.TextureSource;

/**
 * This class assumes a trival nif file suitable for a lod object and attempts to optomise rendering (no groups, no
 * transforms, no interesting appearence)
 * 
 * @author phil
 *
 */
public class LODNif extends Group
{
	public LODNif(String nifFileName, MediaSources mediaSources)
	{
		// TODO: can I optomise this like the X form ones
		// need to make a generic lod nif loading system, fallout uses this for all lod things
		// return J3dRECOTypeGeneral.loadNif(nifFileName, false, mediaSources);
		TextureSource textureSource = mediaSources.getTextureSource();
		NifFile nifFile = NifToJ3d.loadNiObjects(nifFileName, mediaSources.getMeshSource());

		if (nifFile != null)
		{
			NiObject root = nifFile.blocks.root();
			if (root instanceof BSFadeNode || root instanceof NiNode)
			{
				NiNode niNode = (NiNode) root;

				NiToJ3dData niToJ3dData = new NiToJ3dData(nifFile.blocks);

				
				createChildren(niNode, niToJ3dData, nifFileName, textureSource );
			}
			else
			{
				System.out.println("LodBSFadeNode not rooted by fade node! " + root);
			}
		}
	}
	
	public void createChildren(NiNode niNode, NiToJ3dData niToJ3dData, String nifFileName, TextureSource textureSource) 
	{
		if (niNode != null)
		{
			for (int i = 0; i < niNode.numChildren; i++)
			{
				NiAVObject child = (NiAVObject) niToJ3dData.get(niNode.children[i]);
				if (child != null)
				{
					J3dNiTriBasedGeom ntbg = null;

					if (child instanceof NiTriShape)
					{
						NiTriShape niTriShape = (NiTriShape) child;
						ntbg = new J3dNiTriShape(niTriShape, niToJ3dData, textureSource);
					}
					else if (child instanceof BSLODTriShape)
					{
						BSLODTriShape bSLODTriShape = (BSLODTriShape) child;
						ntbg = new J3dNiTriShape(bSLODTriShape, niToJ3dData, textureSource);
					}
					else if (child instanceof NiTriStrips)
					{
						NiTriStrips niTriStrips = (NiTriStrips) child;
						ntbg = new J3dNiTriStrips(niTriStrips, niToJ3dData, textureSource);
					}
					else if (child instanceof BSTriShape)
					{
						BSTriShape bsTriShape = (BSTriShape) child;
						ntbg = new J3dBSTriShape(bsTriShape, niToJ3dData, textureSource);
					}					
					else if (child instanceof NiNode)
					{
						NiNode c = (NiNode)child;					
						if (c.translation.x != 0 || c.translation.y != 0 || c.translation.z != 0)
							System.out.println("non identity child ninode trans for lod nif "	+ child + " " + c.translation + " "
												+ nifFileName);	
						
						createChildren(c, niToJ3dData,nifFileName, textureSource);
					}
					else
					{
						System.out.println("bad child type for lod nif " + child + " " + nifFileName);						
					}

					if (ntbg != null)
					{
						ntbg.compact();
						addChild(ntbg);
					}
				}
			}
		}
			
	}
}
