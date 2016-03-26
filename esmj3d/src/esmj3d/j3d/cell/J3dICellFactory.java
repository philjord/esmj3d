package esmj3d.j3d.cell;

import java.util.HashMap;

import javax.media.j3d.BranchGroup;

import utils.source.MediaSources;
import esmmanager.common.data.plugin.PluginGroup;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.common.data.record.IRecordStore;
import esmmanager.common.data.record.Record;
import esmmanager.loader.IESMManager;

public abstract class J3dICellFactory implements IRecordStore
{

	protected IESMManager esmManager;

	protected MediaSources mediaSources;

	protected HashMap<Integer, PluginGroup> persistentChildrenGroupByFormId = new HashMap<Integer, PluginGroup>();

	protected HashMap<Integer, Record> persistentChildrenByFormId = new HashMap<Integer, Record>();

	protected HashMap<Integer, Integer> persistentCellIdByFormId = new HashMap<Integer, Integer>();

	public abstract void setSources(IESMManager iesmManager, MediaSources mediaSources);

	public abstract String getMainESMFileName();

	public abstract J3dICELLPersistent makeBGWRLDPersistent(int formId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGWRLDTemporary(int wrldFormId, int x, int y, boolean makePhys);

	public abstract J3dCELLGeneral makeBGWRLDDistant(int wrldFormId, int x, int y, boolean makePhys);

	public abstract J3dICELLPersistent makeBGInteriorCELLPersistent(int cellId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGInteriorCELLTemporary(int cellId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGInteriorCELLDistant(int cellId, boolean makePhys);

	public abstract Record getParentWRLDLAND(int wrldFormId, int x, int y);

	public abstract String getLODWorldName(int worldFormId);

	public abstract BranchGroup makeLODLandscape(int lodX, int lodY, int scale, String lodWorldName);

	public abstract boolean isWRLD(int worldFormId);

	protected void cachePersistentChildren(PluginGroup cellChildGroups, int parentId)
	{
		if (cellChildGroups != null && cellChildGroups.getRecordList() != null)
		{
			for (PluginRecord pgr : cellChildGroups.getRecordList())
			{
				PluginGroup pg = (PluginGroup) pgr;
				if (pg.getGroupType() == PluginGroup.CELL_PERSISTENT)
				{
					persistentChildrenGroupByFormId.put(parentId, pg);
					for (PluginRecord pr : pg.getRecordList())
					{
						persistentChildrenByFormId.put(pr.getFormID(), new Record(pr));
						persistentCellIdByFormId.put(pr.getFormID(), parentId);
					}
				}
			}
		}
	}

	public int getCellIdOfPersistentTarget(int formId)
	{
		return persistentCellIdByFormId.get(formId);
	}

	@Override
	public Record getRecord(int formID)
	{
		Record r = esmManager.getRecord(formID);

		if (r != null)
			return r;

		// other wise try the persistents
		return persistentChildrenByFormId.get(formID);

	}

	@Override
	public Record getRecord(String edidId)
	{
		// note persistent are not loaded this way
		return esmManager.getRecord(edidId);
	}

}
