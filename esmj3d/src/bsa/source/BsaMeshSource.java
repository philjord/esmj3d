package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.Folder;
import nif.NifFile;
import nif.NifFileReader;
import utils.source.MeshSource;
import utils.source.file.FileMeshSource;

public class BsaMeshSource implements MeshSource
{

	public static boolean FALLBACK_TO_FILE_SOURCE = false;
	private List<ArchiveFile> bsas;
	private FileMeshSource fileMeshSource = null;

	public BsaMeshSource(List<ArchiveFile> allBsas)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile != null && archiveFile.hasNifOrKf())
			{
				bsas.add(archiveFile);
			}
		}

		if (bsas.size() == 0 && !FALLBACK_TO_FILE_SOURCE)
		{
			System.out.print("No hasNifOrKf archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}

		if (FALLBACK_TO_FILE_SOURCE)
		{
			fileMeshSource = new FileMeshSource();
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

		if (FALLBACK_TO_FILE_SOURCE)
		{
			return fileMeshSource.nifFileExists(nifName);
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

			if (FALLBACK_TO_FILE_SOURCE)
			{
				NifFile nf = fileMeshSource.getNifFile(nifName);
				if (nf != null)
					return nf;
			}

			System.out.print("getNifFile nif " + nifName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}
		
	public List<ArchiveEntry> getEntriesInFolder(String folderName)
	{
		ArrayList<ArchiveEntry> ret = new ArrayList<ArchiveEntry>();

		for (ArchiveFile archiveFile : bsas)
		{
			Folder folder = archiveFile.getFolder(folderName, true);
			if (folder != null)
			{
				for (int i = 0; i < folder.fileToHashMap.size(); i++)
				{
					ArchiveEntry e = folder.fileToHashMap.get(folder.fileToHashMap.keyAt(i));
					ret.add(e);
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

			if (FALLBACK_TO_FILE_SOURCE)
			{
				InputStream is = fileMeshSource.getInputStreamForFile(fileName);
				if (is != null)
					return is;
			}
			
			System.out.print("getInputStreamForFile nif " + fileName + " not found in archive bsas");
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

			if (FALLBACK_TO_FILE_SOURCE)
			{
				ByteBuffer bb = fileMeshSource.getByteBuffer(fileName);
				if (bb != null)
					return bb;
			}
			
			System.out.print("getByteBuffer nif " + fileName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}
}
