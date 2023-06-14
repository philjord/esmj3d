package esmj3d.j3d.cell;

import com.frostwire.util.SparseArray;

import esmj3d.data.shared.records.CommonWRLD;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public interface J3dICELLPersistent 
{
	public GridSpaces getGridSpaces();

	public CommonWRLD getWRLD();
	
	public SparseArray<J3dRECOInst> getJ3dRECOs();
}
