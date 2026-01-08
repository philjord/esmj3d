package forms.baseforms;

import java.util.Iterator;
import java.util.List;

import esfilemanager.common.data.record.Record;
import esfilemanager.common.data.record.Subrecord;

public class SubRecoHandler implements Iterator<Subrecord> {
	public Record			recordData;
	private List<Subrecord>	subrecords;

	//keeps track of where we are in the list
	private int				subIndex	= 0;

	public SubRecoHandler(Record recordData) {
		this.recordData = recordData;
		this.subrecords = recordData.getSubrecords();
	}
	
	@Override
	public boolean hasNext() {
		return subIndex < subrecords.size();
	}


	//hands out the next sub regardless
	@Override
	public Subrecord next() {
		if (subIndex < subrecords.size())
			return subrecords.get(subIndex++);
		else
			return null;
	}

	// true if the next sub is of that type
	public boolean isNext(String subType) {
		if (subIndex < subrecords.size())
			return subrecords.get(subIndex).getSubrecordType().equals(subType);
		else
			return false;
	}

	//hands back the next if of that type, otherwise null
	public Subrecord ifNext(String subType) {
		if (isNext(subType))
			return next();
		else
			return null;
	}

	
}
