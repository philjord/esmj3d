package esmj3d.data.shared.subrecords;

import tools.io.ESMByteConvert;

public class XESP
{
	//XESP 	enable parent 	struct 	8-byte struct

	// formid - Parent reference (Object to take enable state from)
	// uint32 - Flags (records with no flags set have random values in high bits)

	public static int ENABLE_OPPOSITE_FLAG = 0x0001; //   0x0001 = Set Enable State Opposite Parent

	public static int POP_IN_FLAG = 0x0002; //   0x0002 = Pop In

	public int parentId;

	public int flags;

	public XESP(byte[] bytes)
	{
		parentId = ESMByteConvert.extractInt3(bytes, 0);
		flags = ESMByteConvert.extractInt(bytes, 4);
	}
}
