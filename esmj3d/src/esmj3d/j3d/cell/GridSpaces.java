package esmj3d.j3d.cell;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

import utils.ESConfig;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.data.shared.records.InstRECO;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public class GridSpaces extends BranchGroup
{
	public static int BUCKET_RANGE = 82;

	private HashMap<Point, GridSpace> allGridSpaces = new HashMap<Point, GridSpace>();

	private HashMap<Point, GridSpace> attachedGridSpaces = new HashMap<Point, GridSpace>();

	private HashMap<Integer, Record> recordsById = new HashMap<Integer, Record>();

	private HashMap<Integer, GridSpace> gridSpaceByRecordId = new HashMap<Integer, GridSpace>();

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
		recordsById.put(new Integer(record.getFormID()), record);
		gridSpaceByRecordId.put(new Integer(record.getFormID()), gs);
	}

	public void update(float charX, float charY, float loadDist)
	{
		List<GridSpace> gridsToRemove = getGridSpacesToRemove(charX, charY, loadDist);
		for (GridSpace gridSpace : gridsToRemove)
		{
			removeChild(gridSpace);
			gridSpace.unloadChildren();
			attachedGridSpaces.remove(gridSpace.getKey());
		}

		List<GridSpace> gridsToAdd = getGridSpacesToAdd(charX, charY, loadDist);
		for (GridSpace gridSpace : gridsToAdd)
		{

			attachedGridSpaces.put(gridSpace.getKey(), gridSpace);
			gridSpace.loadChildren();
			gridSpace.compile();// better to be done not on the j3d thread?
			addChild(gridSpace);

		}
	}

	public List<GridSpace> getGridSpacesToAdd(float charX, float charY, float loadDist)
	{
		int newLowX = (int) Math.floor((charX - loadDist) / BUCKET_RANGE);
		int newLowY = (int) Math.floor((charY - loadDist) / BUCKET_RANGE);
		int newHighX = (int) Math.ceil((charX + loadDist) / BUCKET_RANGE);
		int newHighY = (int) Math.ceil((charY + loadDist) / BUCKET_RANGE);

		ArrayList<GridSpace> gridsToAdd = new ArrayList<GridSpace>();
		for (int x = newLowX; x <= newHighX; x++)
		{
			for (int y = newLowY; y <= newHighY; y++)
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

	public List<GridSpace> getGridSpacesToRemove(float charX, float charY, float loadDist)
	{
		int newLowX = (int) Math.floor((charX - loadDist) / BUCKET_RANGE);
		int newLowY = (int) Math.floor((charY - loadDist) / BUCKET_RANGE);
		int newHighX = (int) Math.ceil((charX + loadDist) / BUCKET_RANGE);
		int newHighY = (int) Math.ceil((charY + loadDist) / BUCKET_RANGE);

		Iterator<Point> keys = attachedGridSpaces.keySet().iterator();
		ArrayList<GridSpace> gridsToRemove = new ArrayList<GridSpace>();
		while (keys.hasNext())
		{
			Point key = keys.next();
			if (key.x < newLowX || key.x > newHighX || key.y < newLowY || key.y > newHighY)
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
		GridSpace gridSpaceForEvent = gridSpaceByRecordId.get(new Integer(record.getFormID()));
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
