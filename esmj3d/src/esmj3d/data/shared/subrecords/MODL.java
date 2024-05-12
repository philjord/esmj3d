package esmj3d.data.shared.subrecords;

public class MODL
{
	public String model;

	public MODT MODT;

	public MODS MODS;

	public MODB MODB;

	public MODL(byte[] bytes)
	{
		model = ZString.toString(bytes);
	}

	public void addMODTSub(byte[] bytes)
	{
		MODT = new MODT(bytes);
	}

	public void addMODSSub(byte[] bytes)
	{
		MODS = new MODS(bytes);
	}

	public void addMODBSub(byte[] bytes)
	{
		MODB = new MODB(bytes);
	}

}
