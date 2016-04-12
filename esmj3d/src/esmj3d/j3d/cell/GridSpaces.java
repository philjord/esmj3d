package esmj3d.j3d.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

import com.frostwire.util.SparseArray;

import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmmanager.common.data.record.Record;
import esmmanager.common.data.record.Subrecord;
import javaawt.Point;
import javaawt.Rectangle;
import utils.ESConfig;

public class GridSpaces extends BranchGroup
{
	public static int BUCKET_RANGE = (int) J3dLAND.LAND_SIZE;

	private HashMap<Point, GridSpace> allGridSpaces = new HashMap<Point, GridSpace>();

	private HashMap<Point, GridSpace> attachedGridSpaces = new HashMap<Point, GridSpace>();

	private SparseArray<Record> recordsById = new SparseArray<Record>();

	private SparseArray<GridSpace> gridSpaceByRecordId = new SparseArray<GridSpace>();

	private J3dCELLGeneral j3dCELL;

	public GridSpaces(J3dCELLGeneral j3dCELL)
	{
		this.j3dCELL = j3dCELL;
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
	}

	public void sortOutBucket(InstRECO reco, Record record)
	{

		float recordX = reco.getTrans().x * ESConfig.ES_TO_METERS_SCALE;
		float recordY = reco.getTrans().y * ESConfig.ES_TO_METERS_SCALE;
		int xGridIdx = (int) Math.floor(recordX / BUCKET_RANGE);
		int yGridIdx = (int) Math.floor(recordY / BUCKET_RANGE);
		Point key = new Point(xGridIdx, yGridIdx);

		GridSpace gs = allGridSpaces.get(key);
		if (gs == null)
		{
			gs = new GridSpace(j3dCELL, key);
			allGridSpaces.put(key, gs);
		}

		gs.addRecord(record);
		recordsById.put(record.getFormID(), record);
		gridSpaceByRecordId.put(record.getFormID(), gs);
	}

	public void updateAll()
	{
		for (GridSpace gridSpace : allGridSpaces.values())
		{
			attachedGridSpaces.put(gridSpace.getKey(), gridSpace);
			gridSpace.loadChildren();
			gridSpace.compile();// better to be done not on the j3d thread?
			addChild(gridSpace);

		}
	}

	///TODO: for now the near system is used to load gridspaces to make it predictable (skyrim loads too many
	public void update(float charX, float charY, BethLodManager bethLodManager)
	{
		Rectangle bounds = bethLodManager.getGridBounds(charX, charY, BethRenderSettings.getNearLoadGridCount());

		List<GridSpace> gridsToRemove = getGridSpacesToRemove(bounds);
		for (GridSpace gridSpace : gridsToRemove)
		{
			removeChild(gridSpace);
			gridSpace.unloadChildren();
			attachedGridSpaces.remove(gridSpace.getKey());
		}

		List<GridSpace> gridsToAdd = getGridSpacesToAdd(bounds);
		for (GridSpace gridSpace : gridsToAdd)
		{
			attachedGridSpaces.put(gridSpace.getKey(), gridSpace);
			gridSpace.loadChildren();
			gridSpace.compile();// better to be done not on the j3d thread?
			addChild(gridSpace);
		}
	}

	public List<GridSpace> getGridSpacesToAdd(Rectangle bounds)
	{
		ArrayList<GridSpace> gridsToAdd = new ArrayList<GridSpace>();
		for (int x = bounds.x; x <= bounds.x + bounds.width; x++)
		{
			for (int y = bounds.y; y <= bounds.y + bounds.height; y++)
			{
				Point key = new Point(x, y);

				GridSpace gs = allGridSpaces.get(key);
				if (gs != null)
				{
					if (!attachedGridSpaces.containsValue(gs))
					{
						gridsToAdd.add(gs);
					}
				}
			}
		}

		return gridsToAdd;
	}

	public List<GridSpace> getGridSpacesToRemove(Rectangle bounds)
	{
		ArrayList<GridSpace> gridsToRemove = new ArrayList<GridSpace>();
		Iterator<Point> keys = attachedGridSpaces.keySet().iterator();
		while (keys.hasNext())
		{
			Point key = keys.next();
			if (key.x < bounds.x || key.x > bounds.x + bounds.width || key.y < bounds.y || key.y > bounds.y + bounds.height)
			{
				gridsToRemove.add(attachedGridSpaces.get(key));
			}
		}

		return gridsToRemove;
	}

	public void handleRecordCreate(Record record)
	{
		//TODO:find or create gridspace etc?
	}

	public void handleRecordDelete(Record record)
	{
		//TODO:find or create gridspace etc?
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		GridSpace gridSpaceForEvent = gridSpaceByRecordId.get(record.getFormID());
		gridSpaceForEvent.handleRecordUpdate(record, updatedSubrecord);

		//TODO: now check for movement of the record such that it needs to be in a different gridspace

	}

	public J3dRECOInst getJ3dInstRECO(int recoId)
	{

		GridSpace gs = gridSpaceByRecordId.get(recoId);
		if (gs != null)
		{
			return gs.getJ3dRECOsById().get(recoId);
		}
		else
		{
			return null;
		}
	}

}
