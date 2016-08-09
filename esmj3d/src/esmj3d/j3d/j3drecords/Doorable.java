package esmj3d.j3d.j3drecords;

public interface Doorable
{

	public boolean isOpen();

	public String getDoorName();

	public void toggleOpen();

	public void playBothSounds();

}
