package esmj3d.j3d;

import esmj3d.j3d.j3drecords.type.J3dRECOType;
import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import utils.source.MediaSources;

public class J3dEffectNode extends J3dRECOType
{

	public NifJ3dVisRoot nvr;

	public J3dEffectNode(String nifFileName, MediaSources mediaSources)
	{
		super(null, null);

		nvr = NifToJ3d.loadShapes(nifFileName, mediaSources.getMeshSource(), mediaSources.getTextureSource());
		if (nvr != null)
		{
			j3dNiAVObject = nvr.getVisualRoot();

			addChild(j3dNiAVObject);
			fireIdle(nvr);

			//addChild(new Cube(0.2));
		}
		else
		{
			System.out.println("bad name dude " + nifFileName);
		}

	}

	@Override
	public void renderSettingsUpdated()
	{
		super.renderSettingsUpdated();
	}

	@Override
	public void setOutlined(boolean b)
	{
		//ignored
	}

}
