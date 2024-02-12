package esmj3d.j3d.cell;

import java.util.List;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Vector3f;

import com.frostwire.util.SparseArray;

import esfilemanager.common.data.record.IRecordStore;
import esfilemanager.common.data.record.Record;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.water.Water;
import esmj3d.j3d.water.WaterApp;
import utils.ESConfig;
import utils.source.MediaSources;

public abstract class J3dCELLGeneral extends BranchGroup
{
	protected IRecordStore master;

	protected List<Record> children;

	protected SparseArray<J3dRECOInst> j3dRECOs = new SparseArray<J3dRECOInst>();

	protected boolean makePhys;

	protected MediaSources mediaSources;

	protected InstRECO instCell;

	protected Vector3f cellLocation;

	protected int worldId;

	public J3dCELLGeneral(IRecordStore master, int worldId, List<Record> children, boolean makePhys, MediaSources mediaSources)
	{
		this.setName(this.getClass().getSimpleName());
		this.master = master;
		this.worldId = worldId;
		this.children = children;
		this.makePhys = makePhys;
		this.mediaSources = mediaSources;
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		
		//memoryUseList.add(this);
	}

	
	public abstract Node makeJ3dRECOFar(Record record);

	public abstract J3dRECOInst makeJ3dRECO(Record record);

	protected void setCell(InstRECO instCell)
	{
		this.instCell = instCell;
		float landSize = J3dLAND.LAND_SIZE;
		//we don't use instCell.getTrans().z even if set
		cellLocation = new Vector3f((instCell.getTrans().x * landSize) + (landSize / 2f), 0,
				-(instCell.getTrans().y * landSize) - (landSize / 2f));
	}

	public InstRECO getInstCell()
	{
		return instCell;
	}

	public SparseArray<J3dRECOInst> getJ3dRECOs()
	{
		return j3dRECOs;
	}

	protected static float getWaterLevel(float cellWaterLevel)
	{

		if (cellWaterLevel == Float.NEGATIVE_INFINITY)
		{
			return 0;
		}
		else if (cellWaterLevel > 100000)
		{
			//6.8056466E36 is weird skyrim water level (possibly meaning use default) but == no work for floats
			// default in WRLD record but by testing here it is
			return -280;
		}
		else if (cellWaterLevel != Float.POSITIVE_INFINITY && cellWaterLevel != 0x7F7FFFFF && cellWaterLevel != 0x4F7FFFC9)
		{

			return cellWaterLevel * ESConfig.ES_TO_METERS_SCALE;
		}

		return Float.POSITIVE_INFINITY;

	}

	protected Group makeWater(float waterLevel, WaterApp waterApp)
	{
		if (waterLevel != Float.POSITIVE_INFINITY)
		{
			Water water = new Water(J3dLAND.LAND_SIZE, waterApp);

			TransformGroup transformGroup = new TransformGroup();
			Transform3D transform = new Transform3D();

			Vector3f loc = new Vector3f(cellLocation);

			// Notice we don't need to longer center like this loc.x -= (J3dLAND.LAND_SIZE / 2f);loc.z -= (J3dLAND.LAND_SIZE / 2f);

			loc.y = waterLevel;
			transform.set(loc);

			transformGroup.setTransform(transform);
			transformGroup.addChild(water);

			return transformGroup;
		}
		return null;
	}

	public abstract J3dLAND getJ3dLAND();
}
