package esmj3d.j3d.cell;

import java.util.List;

import javax.vecmath.Vector3f;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.physics.PhysicsSystemInterface;
import esmmanager.common.data.record.IRecordStore;
import esmmanager.common.data.record.Record;

public abstract class AICellGeneral implements AIActorLocator
{
	protected IRecordStore master;

	protected List<Record> children;

	protected InstRECO instCell;

	protected Vector3f cellLocation;

	protected int cellId;

	public AICellGeneral(IRecordStore master, int cellId, List<Record> children)
	{
		this.master = master;
		this.cellId = cellId;
		this.children = children;
	}

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

	public Vector3f getCellLocation()
	{
		return cellLocation;
	}

	public abstract void unloadCell();

	public abstract void doAllThoughts(Vector3f charLocation, PhysicsSystemInterface clientPhysicsSystem);

	public abstract void doAllActions(Vector3f charLocation, PhysicsSystemInterface clientPhysicsSystem);

}
