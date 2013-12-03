package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nif.NifFile;
import nif.NifFileReader;
import utils.ESConfig;
import utils.source.MeshSource;
import FO3Archive.ArchiveEntry;
import FO3Archive.ArchiveFile;
import FO3Archive.ArchiveFile.Folder;

public class BsaMeshSource implements MeshSource
{

	private List<ArchiveFile> bsas;

	public BsaMeshSource(List<ArchiveFile> bsas)
	{
		this.bsas = bsas;
	}

	@Override
	public boolean nifFileExists(String nifName)
	{
		if (!nifName.toLowerCase().startsWith("meshes"))
		{
			nifName = "Meshes\\" + nifName;
		}

		for (ArchiveFile archiveFile : bsas)
		{
			ArchiveEntry archiveEntry = archiveFile.getEntry(nifName);
			if (archiveEntry != null)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public NifFile getNifFile(String nifName)
	{

		if (!nifName.toLowerCase().startsWith("meshes"))
		{
			nifName = "Meshes\\" + nifName;
		}

		for (ArchiveFile archiveFile : bsas)
		{
			//dds and kf flags
			if ((archiveFile.getFileFlags() & 1) != 0 || (archiveFile.getFileFlags() & 0x40) != 0)
			{

				ArchiveEntry archiveEntry = archiveFile.getEntry(nifName);
				if (archiveEntry != null)
				{
					try
					{
						NifFile nifFile = null;
						InputStream inputStream = archiveFile.getInputStream(archiveEntry);
						//String fileName = archiveEntry.getName();

						if (archiveFile.getName().toLowerCase().contains("skyrim"))
						{
							ESConfig.HAVOK_TO_METERS_SCALE = ESConfig.SKYRIM_HAVOK_TO_METERS_SCALE;
						}
						else
						{
							ESConfig.HAVOK_TO_METERS_SCALE = ESConfig.PRE_SKYRIM_HAVOK_TO_METERS_SCALE;
						}

						try
						{
							nifFile = NifFileReader.readNif(nifName, inputStream);
						}
						catch (IOException e)
						{
							System.out.println("BsaMeshSource:  " + nifName + " " + e.getMessage());
						}
						finally
						{
							try
							{
								if (inputStream != null)
									inputStream.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}

						if (nifFile != null)
						{
							return nifFile;
						}
					}
					catch (IOException e)
					{
						System.out.println("BsaMeshSource  " + nifName + " " + e.getMessage());
					}
				}
			}
		}

		System.out.println("nif not found in archive bsas " + nifName);
		return null;
	}

	@Override
	public List<String> getFilesInFolder(String folderName)
	{
		ArrayList<String> ret = new ArrayList<String>();

		for (ArchiveFile archiveFile : bsas)
		{
			Folder folder = archiveFile.getFolder(folderName);
			for (ArchiveEntry e : folder.fileToHashMap.values())
			{
				ret.add(folderName + "\\" + e.getFileName());
			}
		}

		return ret;
	}
}
