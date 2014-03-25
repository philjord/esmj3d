package esmj3d.j3d.cell;

import java.util.HashMap;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import utils.source.MediaSources;
import esmLoader.common.data.record.IRecordStore;
import esmLoader.common.data.record.Record;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.Water;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public abstract class J3dCELLGeneral extends BranchGroup
{
	protected IRecordStore master;

	protected List<Record> children;

	protected HashMap<Integer, J3dRECOInst> j3dRECOs = new HashMap<Integer, J3dRECOInst>();

	protected boolean makePhys;

	protected MediaSources mediaSources;

	protected InstRECO instCell;

	protected Vector3f cellLocation;

	public J3dCELLGeneral(IRecordStore master, List<Record> children, boolean makePhys, MediaSources mediaSources)
	{
		this.master = master;
		this.children = children;
		this.makePhys = makePhys;
		this.mediaSources = mediaSources;

		this.setCapability(BranchGroup.ALLOW_DETACH);
	}

	public abstract J3dRECOInst makeJ3dRECO(Record record, boolean noFade);

	protected void setCell(InstRECO instCell)
	{
		this.instCell = instCell;
		float landSize = J3dLAND.LAND_SIZE;
		cellLocation = new Vector3f((instCell.getTrans().x * landSize) + (landSize / 2f), 0, -(instCell.getTrans().y * landSize)
				- (landSize / 2f));
	}

	public InstRECO getInstCell()
	{
		return instCell;
	}

	public HashMap<Integer, J3dRECOInst> getJ3dRECOs()
	{
		return j3dRECOs;
	}

	protected void makeWater(float waterLevel, String texture)
	{
		if (waterLevel == Float.NEGATIVE_INFINITY)
		{
			waterLevel = 0;
		}

		if (waterLevel != Float.NEGATIVE_INFINITY)
		{
			Water water = new Water(J3dLAND.LAND_SIZE, texture, mediaSources.getTextureSource());

			TransformGroup transformGroup = new TransformGroup();
			Transform3D transform = new Transform3D();

			Vector3f loc = new Vector3f(cellLocation);

			loc.y = waterLevel;
			transform.set(loc);

			transformGroup.setTransform(transform);
			transformGroup.addChild(water);
			addChild(transformGroup);
		}
	}
}
