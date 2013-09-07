package esmj3d.data.shared.subrecords;

public class XLOC
{
	/*
	  ubyte[12] or
	  ubtye[16] 	Lock information (only present if object is a DOOR or CONT, and if object is locked). Only partially understood:
	
	* First byte is base lock level for lock (0-100; 100 means key required)
	* Bytes 5-8 are the formid of the KEYM that opens this lock (00000000 if there is no key)
	* Last 4 bytes (i.e., bytes 9-12 if 12 bytes long, or bytes 13-16 if 16 bytes long) appear to be flags
	      o 0x00000004 = Is lock leveled 
	
	 */
	public byte[] unknown;

	public XLOC(byte[] bytes)
	{
		unknown = bytes;
	}
}
