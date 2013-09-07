package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class TNAM
{
	public int markerData;

	public TNAM(byte[] bytes)
	{
		markerData = ESMByteConvert.extractShort(bytes, 0);

	}
}
