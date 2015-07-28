package esmj3d.j3d.cell;

import java.awt.Point;
import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;

import tools.io.ESMByteConvert;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.j3drecords.inst.J3dRECODynInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

/**
 * Note whilst this extends branchgroup these GridSpace objects are not discarded, so to keep memory down GridSpace
 * can be told to discard it's loaded j3d objects under it when not attached to the scene graph
 * @author Administrator
 *
 */
public class GridSpace extends BranchGroup
{

	private Point key;

	private boolean makePhys = false;

	private HashMap<Integer, Record> recordsById = new HashMap<Integer, Record>();

	private HashMap<Integer, J3dRECOInst> j3dRECOsById = new HashMap<Integer, J3dRECOInst>()
	{
		public String name = "GridSpace";
	};

	private J3dCELLGeneral j3dCELL;

	private BranchGroup children;

	/**
	 * j3dLoaded means the grid is now visible and needs it's j3d records addeed
	 * @param j3dCELLPersistent 
	 * @param key 
	 * @param parent
	 */
	public GridSpace(J3dCELLGeneral j3dCELL, Point key)
	{
		this.j3dCELL = j3dCELL;
		this.key = key;
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
	}

	public HashMap<Integer, J3dRECOInst> getJ3dRECOsById()
	{

		return j3dRECOsById;

	}

	/**
	 * j3dRECO may well be null
	 * @param record
	 * @param j3dRECO
	 */

	public void addRecord(Record record)
	{
		if (!recordsById.containsKey(new Integer(record.getFormID())))
		{
			recordsById.put(new Integer(record.getFormID()), record);
		}
	}

	/**
	 * Removes the record from this grid space and detaches and returns the j3dreco associated with it if one exists
	 * @param id
	 * @return
	 */
	public synchronized J3dRECOInst removeRecord(int id)
	{
		if (recordsById.containsKey(new Integer(id)))
		{
			recordsById.remove(new Integer(id));

			J3dRECOInst j3dRECO = j3dRECOsById.remove(new Integer(id));
			if (j3dRECO != null)
			{
				((BranchGroup) j3dRECO).detach();
			}
			return j3dRECO;

		}
		return null;
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		J3dRECOInst j3dRECO = j3dRECOsById.get(new Integer(record.getFormID()));
		if (j3dRECO instanceof J3dRECODynInst)
		{
			if (record.getRecordType().equals("REFR") || (!makePhys && record.getRecordType().equals("ACRE"))
					|| (!makePhys && record.getRecordType().equals("ACHR")))
			{
				if (updatedSubrecord.getType().equals("DATA"))
				{
					byte[] bs = updatedSubrecord.getData();
					float x = ESMByteConvert.extractFloat(bs, 0);
					float y = ESMByteConvert.extractFloat(bs, 4);
					float z = ESMByteConvert.extractFloat(bs, 8);
					float rx = ESMByteConvert.extractFloat(bs, 12);
					float ry = ESMByteConvert.extractFloat(bs, 16);
					float rz = ESMByteConvert.extractFloat(bs, 20);

					((J3dRECODynInst) j3dRECO).setLocation(x, y, z, rx, ry, rz, 1);

				}
			}
		}

	}

	public void loadChildren()
	{
		//skip if loaded already
		if (children == null)
		{
			children = new BranchGroup();
			children.setCapability(BranchGroup.ALLOW_DETACH);
			children.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			children.setCapability(Group.ALLOW_CHILDREN_WRITE);

			for (Record record : recordsById.values())
			{
				J3dRECOInst j3dRECOInst = j3dCELL.makeJ3dRECO(record);
				if (j3dRECOInst != null)
				{
					// now attach and record the j3dRECO
					j3dRECOsById.put(new Integer(record.getFormID()), j3dRECOInst);
					children.addChild((Node) j3dRECOInst);
				}
			}

			addChild(children);

		}
	}

	public void unloadChildren()
	{
		if (children != null)
		{
			children.detach();
			children = null;
		}
	}

	public Point getKey()
	{
		return key;
	}

}
