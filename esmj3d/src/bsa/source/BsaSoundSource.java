package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.MediaContainer;
import org.jogamp.java3d.SoundException;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.Folder;
import tools.SoundKeyToName;
import utils.source.SoundSource;
import utils.source.file.FileSoundSource;

public class BsaSoundSource implements SoundSource
{
	private List<ArchiveFile> bsas;

	private SoundKeyToName soundKeyToName;

	private FileSoundSource fileSoundSource = null;

	public BsaSoundSource(List<ArchiveFile> allBsas, SoundKeyToName soundKeyToName)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile != null && archiveFile.hasSounds())
			{
				bsas.add(archiveFile);
			}
		}

		this.soundKeyToName = soundKeyToName;

		if (bsas.size() == 0 && !BsaMeshSource.FALLBACK_TO_FILE_SOURCE)
		{
			System.out.print("No hasSounds archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}

		if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE)
		{
			fileSoundSource = new FileSoundSource();
		}
	}

	@Override
	public MediaContainer getMediaContainer(String mediaName)
	{
		String soundFile = mediaName;

		// do we have the key system?
		if (soundKeyToName != null)
			soundFile = soundKeyToName.getFileName(mediaName);
		if (soundFile != null)
		{
			for (ArchiveFile archiveFile : bsas)
			{

				ArchiveEntry archiveEntry = archiveFile.getEntry(soundFile);
				if (archiveEntry != null)
				{
					MediaContainer mediaContainer = null;
					InputStream inputStream = null;

					try
					{
						inputStream = archiveFile.getInputStream(archiveEntry);
						//String fileName = archiveEntry.getName();

						mediaContainer = new MediaContainer(inputStream);
					}
					catch (SoundException e)
					{
						System.out.println("BsaSoundSource Error get sound key: " + mediaName + " file: " + soundFile + " " + e + " "
								+ e.getStackTrace()[0]);
					}
					catch (IOException e)
					{
						System.out.println("BsaSoundSource Error get sound key: " + mediaName + " file: " + soundFile + " " + e + " "
								+ e.getStackTrace()[0]);
					}

					if (mediaContainer != null)
					{
						return mediaContainer;
					}
				}

			}

			if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE)
			{
				MediaContainer mc = fileSoundSource.getMediaContainer(mediaName);
				if (mc != null)
					return mc;
			}
		}

		System.out.println("BsaSoundSource Error getting sound from bsas key: " + mediaName + " file: " + soundFile);
		return null;
	}

	@Override
	public InputStream getInputStream(String mediaName)
	{
		String soundFile = mediaName;

		// do we have the key system?
		if (soundKeyToName != null)
			soundFile = soundKeyToName.getFileName(mediaName);

		for (ArchiveFile archiveFile : bsas)
		{

			ArchiveEntry archiveEntry = archiveFile.getEntry(soundFile);
			if (archiveEntry != null)
			{

				InputStream inputStream = null;

				try
				{
					inputStream = archiveFile.getInputStream(archiveEntry);
					//String fileName = archiveEntry.getName();

				}
				catch (SoundException e)
				{
					System.out.println("BsaSoundSource Error get sound key: " + mediaName + " file: " + soundFile + " " + e + " "
							+ e.getStackTrace()[0]);
				}
				catch (IOException e)
				{
					System.out.println("BsaSoundSource Error get sound key: " + mediaName + " file: " + soundFile + " " + e + " "
							+ e.getStackTrace()[0]);
				}

				if (inputStream != null)
				{
					return inputStream;
				}
			}

		}
		if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE)
		{
			InputStream mc = fileSoundSource.getInputStream(mediaName);
			if (mc != null)
				return mc;
		}
		System.out.println("BsaSoundSource Error getting sound from bsas key: " + mediaName + " file: " + soundFile);
		return null;
	}

	@Override
	public List<String> getFilesInFolder(String folderName)
	{
		ArrayList<String> ret = new ArrayList<String>();

		for (ArchiveFile archiveFile : bsas)
		{
			Folder folder = archiveFile.getFolder(folderName, true);
			if (folder != null)
			{
				for (int i = 0; i < folder.fileToHashMap.size(); i++)
				{
					ArchiveEntry e = folder.fileToHashMap.get(folder.fileToHashMap.keyAt(i));
					ret.add(folderName + "\\" + e.getFileName());
				}
			}
		}

		return ret;
	}
}
