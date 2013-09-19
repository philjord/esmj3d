// Decompiled by DJ v3.6.6.79 Copyright 2004 Atanas Neshkov  Date: 5/27/2009 3:52:54 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ExtractTask.java

package bsa;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Texture;
import javax.swing.SwingUtilities;

import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.character.KfJ3dRoot;
import nif.gui.NifDisplayTester;
import tools.image.SimpleImageLoader;
import tools.texture.DDSToTexture;
import utils.source.DummyTextureSource;
import FO3Archive.ArchiveEntry;
import FO3Archive.Main;
import FO3Archive.StatusDialog;
import bsa.source.BsaMeshSource;
import bsa.source.BsaTextureSource;

// Referenced classes of package FO3Archive:
//            ArchiveEntry, ArchiveFile, StatusDialog, Main

public class DisplayTask extends Thread
{
	private BSAFileSet bsaFileSet;

	private List<ArchiveEntry> entries;

	private StatusDialog statusDialog;

	private boolean verifyOnly;

	private boolean completed;

	private boolean sopErrOnly;

	public DisplayTask(BSAFileSet bsaFileSet, List<ArchiveEntry> entries, StatusDialog statusDialog, boolean verifyOnly, boolean sopErrOnly)
	{
		completed = false;
		this.bsaFileSet = bsaFileSet;
		this.entries = entries;
		this.statusDialog = statusDialog;
		this.verifyOnly = verifyOnly;
		this.sopErrOnly = sopErrOnly;
	}

	public void run()
	{

		statusDialog.updateMessage("" + (verifyOnly ? "Verifying files" : "Displaying Files"));
		statusDialog.updateProgress(0);

		int fileCount = entries.size();
		int filesProcessCount = 0;
		float currentProgress = 0;
		for (ArchiveEntry entry : entries)
		{
			try
			{
				InputStream in = entry.getArchiveFile().getInputStream(entry);

				String fileName = entry.getName();

				int sep = fileName.lastIndexOf('.');
				if (sep >= 0)
				{
					String ext = fileName.substring(sep);
					if (ext.equals(".nif"))
					{
						if (verifyOnly)
						{
							NifJ3dVisRoot nr = NifToJ3d.loadShapes(fileName, new BsaMeshSource(bsaFileSet), new DummyTextureSource());
							if (nr != null)
							{
								if (!sopErrOnly)
								{
									System.out.println("verified: " + fileName);
								}
							}
							else
							{
								System.out.println("issue: " + fileName);
							}
							NifToJ3d.clearCache();
						}
						else
						{
							getNifDisplayer().showNif(fileName, new BsaMeshSource(bsaFileSet), new BsaTextureSource(bsaFileSet));
						}
					}
					else if (ext.equals(".dds"))
					{
						if (verifyOnly)
						{
							Texture tex = new BsaTextureSource(bsaFileSet).getTexture(fileName);
							if (tex != null)
							{
								if (!sopErrOnly)
								{
									System.out.println("verified: " + fileName);
								}
							}
							else
							{
								System.out.println("issue: " + fileName);
							}

							DDSToTexture.clearCache();
						}
						else
						{
							DDSToTexture.showImage(fileName, new BsaTextureSource(bsaFileSet).getInputStream(fileName),
									entries.size() < 10 ? 5000 : 500);
						}
					}
					else if (ext.equals(".kf"))
					{
						//only verify with no skeleton
						KfJ3dRoot kr = NifToJ3d.loadKf(fileName, new BsaMeshSource(bsaFileSet));
						if (kr != null)
						{
							if (!sopErrOnly)
							{
								System.out.println("verified: " + fileName);
							}
						}
						else
						{
							System.out.println("issue: " + fileName);
						}
						NifToJ3d.clearCache();
					}
					else if (ext.equals(".png"))
					{
						if (verifyOnly)
						{
							BufferedImage bi = SimpleImageLoader.getImage(fileName);
							if (bi != null)
							{
								if (!sopErrOnly)
								{
									System.out.println("verified: " + fileName);
								}
							}
							else
							{
								System.out.println("issue: " + fileName);
							}
						}
						else
						{
							if (!sopErrOnly)
							{
								System.out.println("I would have displayed you a png just now! " + fileName);
							}
						}

					}
					else if (ext.equals(".wav"))
					{

						if (!sopErrOnly)
						{
							System.out.println("I would have played you a wav just now! " + fileName);
						}

					}
					else if (ext.equals(".lip"))
					{

						if (!sopErrOnly)
						{
							System.out.println("display lip file? " + fileName);
						}

					}
					else if (ext.equals(".mp3"))
					{

						if (!sopErrOnly)
						{
							System.out.println("I would have played you a mp3 just now! " + fileName);
						}

					}
					else if (ext.equals(".ogg"))
					{

						if (!sopErrOnly)
						{
							System.out.println("I would have played you a ogg just now! " + fileName);
						}

					}
					else if (ext.equals(".xml"))
					{

						if (!sopErrOnly)
						{
							System.out.println("display xml file? " + fileName);
						}

					}
					else
					{
						if (!sopErrOnly)
						{
							System.out.println("unknown file : " + fileName);
						}
					}
				}

				in.close();

				filesProcessCount++;
				float newProgress = filesProcessCount / (float) fileCount;

				if ((newProgress - currentProgress) > 0.01)
				{
					statusDialog.updateMessage("" + (verifyOnly ? "Verifying " : "Displaying ") + fileName);
					statusDialog.updateProgress((filesProcessCount * 100) / fileCount);
					currentProgress = newProgress;
				}
			}
			catch (IOException exc)
			{
				Main.logException("I/O error while extracting files", exc);
			}
			catch (Throwable exc)
			{
				if (verifyOnly)
				{
					exc.printStackTrace();
				}
				else
				{
					Main.logException("Exception while extracting files", exc);
				}
			}
		}

		completed = true;

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				statusDialog.closeDialog(completed);
			}
		});
	}

	private NifDisplayTester nifDisplay;

	private NifDisplayTester getNifDisplayer()
	{
		if (nifDisplay == null)
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration[] gc = gd.getConfigurations();
			GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
			template.setStencilSize(8);
			GraphicsConfiguration config = template.getBestConfiguration(gc);

			nifDisplay = new NifDisplayTester(config);
		}

		return nifDisplay;
	}
}