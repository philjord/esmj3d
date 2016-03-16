package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import archive.ArchiveEntry;
import archive.ArchiveFile;
import archive.ArchiveFile.Folder;
import nif.NifFile;
import nif.NifFileReader;
import utils.source.MeshSource;

public class BsaMeshSource implements MeshSource
{
	private List<ArchiveFile> bsas;

	public BsaMeshSource(List<ArchiveFile> allBsas)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile.hasNifOrKf())
			{
				bsas.add(archiveFile);
			}
		}

		if (bsas.size() == 0)
		{
			System.out.println("No hasNifOrKf archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.println("ArchiveFiFSle:" + archiveFile.getName());
			}
		}
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
		if (nifName.length() > 0)
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
					try
					{
						NifFile nifFile = null;
						ByteBuffer inputStream = archiveFile.getByteBuffer(archiveEntry);
						// String fileName = archiveEntry.getName();

						try
						{
							nifFile = NifFileReader.readNif(nifName, inputStream);
						}
						catch (IOException e)
						{
							System.out.println("BsaMeshSource:  " + nifName + " " + e + " " + e.getStackTrace()[0]);
						}
					/*	finally
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
						}*/

						if (nifFile != null)
						{
							return nifFile;
						}
					}
					catch (IOException e)
					{
						System.out.println("BsaMeshSource  " + nifName + " " + e + " " + e.getStackTrace()[0]);
					}

				}
			}

			System.out.print("nif " + nifName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}

	@Override
	public List<String> getFilesInFolder(String folderName)
	{
		ArrayList<String> ret = new ArrayList<String>();

		for (ArchiveFile archiveFile : bsas)
		{
			Folder folder = archiveFile.getFolder(folderName);
			if (folder != null)
			{
				for (ArchiveEntry e : folder.fileToHashMap.values())
				{
					ret.add(folderName + "\\" + e.getFileName());
				}
			}
		}

		return ret;
	}

	@Override
	public InputStream getInputStreamForFile(String fileName)
	{
		if (fileName != null && fileName.length() > 0)
		{
			for (ArchiveFile archiveFile : bsas)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(fileName);
				if (archiveEntry != null)
				{
					try
					{
						return archiveFile.getInputStream(archiveEntry);
					}
					catch (IOException e)
					{
						System.out.println("BsaMeshSource  " + fileName + " " + e + " " + e.getStackTrace()[0]);
					}

				}
			}

			System.out.print("nif " + fileName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}

	@Override
	public ByteBuffer getByteBuffer(String fileName)
	{
		if (fileName != null && fileName.length() > 0)
		{
			for (ArchiveFile archiveFile : bsas)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(fileName);
				if (archiveEntry != null)
				{
					try
					{
						return archiveFile.getByteBuffer(archiveEntry);
					}
					catch (IOException e)
					{
						System.out.println("BsaMeshSource  " + fileName + " " + e + " " + e.getStackTrace()[0]);
					}

				}
			}

			System.out.print("nif " + fileName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}
}
