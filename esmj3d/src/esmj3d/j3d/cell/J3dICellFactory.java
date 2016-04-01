package esmj3d.j3d.cell;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.media.j3d.BranchGroup;

import utils.source.MediaSources;
import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginGroup;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.common.data.record.IRecordStore;
import esmmanager.common.data.record.Record;
import esmmanager.loader.CELLDIALPointer;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;
import esmmanager.loader.InteriorCELLTopGroup;
import esmmanager.loader.WRLDChildren;
import esmmanager.loader.WRLDTopGroup;

public abstract class J3dICellFactory implements IRecordStore
{

	protected IESMManager esmManager;

	protected MediaSources mediaSources;

	protected HashMap<Integer, PluginGroup> persistentChildrenGroupByFormId = new HashMap<Integer, PluginGroup>();

	protected HashMap<Integer, Record> persistentChildrenByFormId = new HashMap<Integer, Record>();

	protected HashMap<Integer, Integer> persistentCellIdByFormId = new HashMap<Integer, Integer>();

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

	public void setSources(IESMManager esmManager2, MediaSources mediaSources)
	{
		this.esmManager = esmManager2;
		this.mediaSources = mediaSources;

		//Carefully load on a separate thread, might cause trouble
		Thread t = new Thread() {
			public void run()
			{

				long start = System.currentTimeMillis();

				//let's load all WRLD, CELL persistent children now!
				//I need to pre-load ALL persistent children for all CELLS and keep them for XTEL look ups
				// and one day I would imagine for scripting of actors too 
				int wrldCount = 0;

				for (WRLDTopGroup WRLDTopGroup : ((ESMManager) esmManager).getWRLDTopGroups())
				{
					for (PluginRecord wrldPR : WRLDTopGroup.WRLDByFormId.values())
					{
						// it looks like no temps in wrld cell so no saving by making a special call
						WRLDChildren children = esmManager.getWRLDChildren(wrldPR.getFormID());
						PluginGroup cellChildGroups = children.getCellChildren();
						if (cellChildGroups != null && cellChildGroups.getRecordList() != null)
						{
							for (PluginRecord pgr : cellChildGroups.getRecordList())
							{
								PluginGroup pg = (PluginGroup) pgr;
								if (pg.getGroupType() == PluginGroup.CELL_PERSISTENT)
								{
									cachePersistentChildren(pg, wrldPR.getFormID());
								}
							}
						}
						wrldCount++;
					}
				}

				int cellCount = 0;

				List<InteriorCELLTopGroup> interiorCELLTopGroups = ((ESMManager) esmManager).getInteriorCELLTopGroups();
				for (InteriorCELLTopGroup interiorCELLTopGroup : interiorCELLTopGroups)
				{
					for (CELLDIALPointer cp : interiorCELLTopGroup.getAllInteriorCELLFormIds())
					{
						try
						{
							PluginGroup cellChildGroups = esmManager.getInteriorCELLPersistentChildren(cp.formId);
							cachePersistentChildren(cellChildGroups, cp.formId);
							cellCount++;
						}
						catch (DataFormatException e)
						{
							e.printStackTrace();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						catch (PluginException e)
						{
							e.printStackTrace();
						}
					}
				}

				System.out.println("Persistent Records loaded in " + (System.currentTimeMillis() - start) //
						+ " WRLD count = " + wrldCount//
						+ " CELL count = " + cellCount//
						+ " record count = " + persistentChildrenByFormId.size());
			}
		};
		t.setName("Persistent cells loader");
		t.start();

	}

	protected void cachePersistentChildren(PluginGroup cellChildGroups, int parentId)
	{
		if (cellChildGroups != null && cellChildGroups.getRecordList() != null)
		{
			persistentChildrenGroupByFormId.put(parentId, cellChildGroups);
			for (PluginRecord pr : cellChildGroups.getRecordList())
			{
				persistentChildrenByFormId.put(pr.getFormID(), new Record(pr));
				persistentCellIdByFormId.put(pr.getFormID(), parentId);
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
