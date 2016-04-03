package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Texture;

import archive.ArchiveEntry;
import archive.ArchiveFile;
import archive.ArchiveFile.Folder;
import tools.compressedtexture.astc.ASTCTextureLoader;
import tools.compressedtexture.dds.DDSTextureLoader;
import tools.compressedtexture.ktx.KTXTextureLoader;
import utils.source.TextureSource;

public class BsaTextureSource implements TextureSource
{
	private List<ArchiveFile> bsas;

	public BsaTextureSource(List<ArchiveFile> allBsas)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile.hasDDS() || archiveFile.hasKTX())
			{
				bsas.add(archiveFile);
			}
		}
		if (bsas.size() == 0)
		{
			System.out.print("No hasDDS nor hasKTX archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}
	}

	@Override
	public boolean textureFileExists(String texName)
	{
		if (texName != null && texName.length() > 0)
		{
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\"))
			{
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures"))
			{
				texName = "textures\\" + texName;
			}

			Texture tex = null;
			//check cache hit
			tex = DDSTextureLoader.checkCachedTexture(texName);
			if (tex != null)
			{
				return true;
			}

			for (ArchiveFile archiveFile : bsas)
			{
				if (archiveFile.hasKTX())
				{
					texName = texName.replace(".dds", ".ktx");
				}
				else if (archiveFile.hasASTC())
				{
					texName = texName.replace(".dds", ".tga.astc");
				}

				ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
				if (archiveEntry != null)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Texture getTexture(String texName)
	{
		if (texName != null && texName.length() > 0)
		{
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\"))
			{
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures"))
			{
				texName = "textures\\" + texName;
			}

			Texture tex = null;

			//check cache hit
			tex = DDSTextureLoader.checkCachedTexture(texName);
			if (tex != null)
			{
				return tex;
			}

			for (ArchiveFile archiveFile : bsas)
			{
				if (archiveFile.hasKTX())
				{
					texName = texName.replace(".dds", ".ktx");
				}
				else if (archiveFile.hasASTC())
				{
					texName = texName.replace(".dds", ".tga.astc");
				}

				ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
				if (archiveEntry != null)
				{
					try
					{
						//note that we want all disk activity now, (mappedbytebuffers can delay it until the j3d thread)
						InputStream in = archiveFile.getInputStream(archiveEntry);

						if (texName.endsWith(".dds"))
						{
							tex = DDSTextureLoader.getTexture(texName, in);
						}
						else if (texName.endsWith(".astc") || texName.endsWith(".atc"))
						{
							tex = ASTCTextureLoader.getTexture(texName, in);
						}
						else if (texName.endsWith(".ktx"))
						{
							tex = KTXTextureLoader.getTexture(texName, in);
						}
						else
						{
							//FIXME: generic texture loading system
							/*TextureLoader tl = new TextureLoader(ImageIO.read(in));
							tex = tl.getTexture();*/
						}

						if (tex != null)
						{
							return tex;
						}
					}
					catch (IOException e)
					{
						System.out.println("BsaTextureSource  " + texName + " " + e + " " + e.getStackTrace()[0]);
					}
				}

			}
		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + texName);
		//new Throwable().printStackTrace();
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

	public InputStream getInputStream(String texName)
	{
		if (texName != null && texName.length() > 0)
		{
			texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\"))
			{
				texName = texName.substring(5);
			}

			// add the textures path part (unless tes3 bookart folder)
			if (!texName.startsWith("textures") && !texName.startsWith("bookart"))
			{
				texName = "textures\\" + texName;
			}

			for (ArchiveFile archiveFile : bsas)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
				if (archiveEntry != null)
				{
					try
					{
						InputStream in = archiveFile.getInputStream(archiveEntry);
						return in;
					}
					catch (IOException e)
					{

						e.printStackTrace();
					}

				}
			}
		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + texName);
		new Throwable().printStackTrace();
		return null;
	}

}
