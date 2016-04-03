package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.MediaContainer;
import javax.media.j3d.SoundException;

import archive.ArchiveEntry;
import archive.ArchiveFile;
import archive.ArchiveFile.Folder;
import tools.SoundKeyToName;
import utils.source.SoundSource;

public class BsaSoundSource implements SoundSource
{
	private List<ArchiveFile> bsas;

	private SoundKeyToName soundKeyToName;

	public BsaSoundSource(List<ArchiveFile> allBsas, SoundKeyToName soundKeyToName)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile.hasSounds())
			{
				bsas.add(archiveFile);
			}
		}

		this.soundKeyToName = soundKeyToName;

		if (bsas.size() == 0)
		{
			System.out.print("No hasSounds archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}
	}

	@Override
	public MediaContainer getMediaContainer(String mediaName)
	{
		String soundFile = soundKeyToName.getFileName(mediaName);

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
