package esmj3d.j3d.cell;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.jogamp.java3d.BranchGroup;

import com.frostwire.util.SparseArray;

import esfilemanager.common.PluginException;
import esfilemanager.common.data.plugin.PluginGroup;
import esfilemanager.common.data.plugin.PluginRecord;
import esfilemanager.common.data.record.Record;
import esfilemanager.loader.ESMManager;
import esfilemanager.loader.FormToFilePointer;
import esfilemanager.loader.IESMManager;
import esfilemanager.loader.InteriorCELLTopGroup;
import esfilemanager.loader.WRLDChildren;
import esfilemanager.loader.WRLDTopGroup;
import esfilemanager.tes3.IRecordStoreTes3;
import utils.source.MediaSources;

//NOTE it will only be TEs3 style if the ESMMAnger given in is
public abstract class J3dICellFactory implements IRecordStoreTes3
{

	protected IESMManager esmManager;

	protected MediaSources mediaSources;

	protected SparseArray<PluginGroup> persistentChildrenGroupByFormId = new SparseArray<PluginGroup>();

	protected SparseArray<Record> persistentChildrenByFormId = new SparseArray<Record>();

	protected SparseArray<Integer> persistentCellIdByFormId = new SparseArray<Integer>();

	public abstract String getMainESMFileName();

	public abstract J3dICELLPersistent makeBGWRLDPersistent(int formId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGWRLDTemporary(int wrldFormId, int x, int y, boolean makePhys);

	public abstract J3dCELLGeneral makeBGWRLDDistant(int wrldFormId, int x, int y, boolean makePhys);

	public abstract J3dICELLPersistent makeBGInteriorCELLPersistent(int cellId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGInteriorCELLTemporary(int cellId, boolean makePhys);

	public abstract J3dCELLGeneral makeBGInteriorCELLDistant(int cellId, boolean makePhys);

	public abstract Record getParentWRLDLAND(int wrldFormId, int x, int y);

	public abstract String getLODWorldName(int worldFormId);

	public abstract BranchGroup makeLODLandscape(int wrldFormId, int lodX, int lodY, int scale, String lodWorldName);

	public abstract AICellGeneral makeAICell(int cellId, AIActorServices aiActorLocator);

	public abstract AICellGeneral makeAICell(int wrldFormId, int x, int y, AIActorServices aiActorLocator);

	public abstract boolean isWRLD(int worldFormId);

	public void setSources(IESMManager esmManager2, MediaSources mediaSources)
	{
		this.esmManager = esmManager2;
		this.mediaSources = mediaSources;

		//Carefully load on a separate thread, might cause trouble
		Thread t = new Thread() {
			@Override
			public void run()
			{

				long start = System.currentTimeMillis();

				//let's load all WRLD, CELL persistent children now!
				//I need to pre-load ALL persistent children for all CELLS and keep them for XTEL look ups
				// and one day I would imagine for scripting of actors too 
				int wrldCount = 0;

				for (WRLDTopGroup WRLDTopGroup : ((ESMManager) esmManager).getWRLDTopGroups())
				{
					for (int i = 0; i < WRLDTopGroup.WRLDByFormId.size(); i++)
					{
						PluginRecord wrldPR = WRLDTopGroup.WRLDByFormId.get(WRLDTopGroup.WRLDByFormId.keyAt(i));

						// it looks like no temps in wrld cell so no saving by making a special call
						WRLDChildren children = esmManager.getWRLDChildren(wrldPR.getFormID());
						if(children != null) {
							PluginGroup cellChildGroups = children.getCellChildren();
							if (cellChildGroups != null && cellChildGroups.getRecordList() != null)
							{
								for (Record pgr : cellChildGroups.getRecordList())
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
				}

				int cellCount = 0;

				List<InteriorCELLTopGroup> interiorCELLTopGroups = ((ESMManager) esmManager).getInteriorCELLTopGroups();
				for (InteriorCELLTopGroup interiorCELLTopGroup : interiorCELLTopGroups)
				{
					for (FormToFilePointer cp : interiorCELLTopGroup.getAllInteriorCELLFormIds())
					{
						try
						{
							PluginGroup cellChildGroups = esmManager.getInteriorCELLPersistentChildren(cp.formId);
							cachePersistentChildren(cellChildGroups, cp.formId);
							cellCount++;
						}
						catch (Exception e)
						{
							System.out.println("Exception loading interior CELL " + cp.formId);
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
			for (Record pr : cellChildGroups.getRecordList())
			{
				persistentChildrenByFormId.put(pr.getFormID(), pr);
				persistentCellIdByFormId.put(pr.getFormID(), new Integer(parentId));
			}
		}
	}

	
	public PluginGroup getPersistentChildrenOfCell(int formId)
	{
		return persistentChildrenGroupByFormId.get(formId);
	}
	
	public int getCellIdOfPersistentTarget(int formId)
	{
		Integer ret = persistentCellIdByFormId.get(formId);
		return ret == null ? -1 : ret.intValue();
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
		return ((IRecordStoreTes3) esmManager).getRecord(edidId);
	}

}
