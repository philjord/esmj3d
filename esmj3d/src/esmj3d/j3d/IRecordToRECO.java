package esmj3d.j3d;

import java.util.List;

import esmLoader.common.data.record.IRecordStore;
import esmLoader.common.data.record.Record;

public interface IRecordToRECO
{
	public void makeRECOsForCELL(IRecordStore master, Record cellRecord, List<Record> children);
}
