package bsa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import FO3Archive.ArchiveEntry;
import FO3Archive.ArchiveFile;
import FO3Archive.ArchiveNode;
import FO3Archive.LoadTask;
import FO3Archive.StatusDialog;

public class BSAFileSet extends ArrayList<ArchiveFile>
{
	public ArrayList<ArchiveNode> nodes = new ArrayList<ArchiveNode>();

	private String name = "";

	/**
	 * If the root file is not a folder, it is assumed to be teh esm file and so it's parent folder is used
	 * 
	 * @param rootFilename
	 * @param loadSiblingBsaFiles
	 * @param loadNodes set true if you want to add this bsa file set to a tree
	 * @param sopErrOnly 
	 */
	public BSAFileSet(String rootFilename, boolean loadSiblingBsaFiles, boolean loadNodes)
	{
		File rootFile = new File(rootFilename);
		if (loadSiblingBsaFiles)
		{
			if (!rootFile.isDirectory())
			{
				rootFile = rootFile.getParentFile();
			}
			name = rootFile.getAbsolutePath();
			for (File file : rootFile.listFiles())
			{
				if (file.getName().toLowerCase().endsWith(".bsa"))
				{
					loadFile(file, loadNodes);
				}
			}
		}
		else
		{
			if (!rootFile.isDirectory() && rootFile.getName().toLowerCase().endsWith(".bsa"))
			{
				name = rootFile.getAbsolutePath();
				loadFile(rootFile, loadNodes);
			}
			else
			{
				System.out.println("BSAFileSet bad non sibling load of " + rootFilename);
			}
		}

		if (this.size() == 0)
		{
			System.out.println("BSAFileSet loaded no files using root: " + rootFilename);
		}
	}

	private void loadFile(final File file, boolean loadNodes)
	{
		System.out.println("BSA File Set loading " + file);
		ArchiveFile archiveFile = new ArchiveFile(file);
		StatusDialog statusDialog = new StatusDialog(null, "Loading " + archiveFile.getName());

		try
		{
			if (loadNodes)
			{
				ArchiveNode archiveNode = new ArchiveNode(archiveFile);

				LoadTask loadTask = new LoadTask(archiveFile, archiveNode, statusDialog);
				loadTask.start();

				int status = statusDialog.showDialog();
				loadTask.join();
				if (status == 1)
				{
					add(archiveFile);
					nodes.add(archiveNode);
				}
			}
			else
			{
				LoadTask loadTask = new LoadTask(archiveFile, statusDialog);
				loadTask.start();

				int status = statusDialog.showDialog();
				loadTask.join();
				if (status == 1)
				{
					add(archiveFile);
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			try
			{
				archiveFile.close();
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}
		}

	}

	public void close() throws IOException
	{
		for (ArchiveFile af : this)
		{
			af.close();
		}
		nodes.clear();

	}

	public List<ArchiveEntry> getEntries(StatusDialog statusDialog)
	{
		List<ArchiveEntry> ret = new ArrayList<ArchiveEntry>();
		for (ArchiveFile af : this)
		{
			ret.addAll(af.getEntries(statusDialog));
		}
		return ret;
	}

	public String getName()
	{
		return name;
	}

	public List<ArchiveFile> getMeshArchives()
	{
		List<ArchiveFile> ret = new ArrayList<ArchiveFile>();
		for (ArchiveFile af : this)
		{
			if (af.hasNifOrKf())
				ret.add(af);
		}
		return ret;
	}

	public List<ArchiveFile> getSoundArchives()
	{
		List<ArchiveFile> ret = new ArrayList<ArchiveFile>();
		for (ArchiveFile af : this)
		{
			if (af.hasSounds())
				ret.add(af);
		}
		return ret;
	}

	public List<ArchiveFile> getTextureArchives()
	{
		List<ArchiveFile> ret = new ArrayList<ArchiveFile>();
		for (ArchiveFile af : this)
		{
			if (af.hasDDS())
				ret.add(af);
		}
		return ret;
	}
}
