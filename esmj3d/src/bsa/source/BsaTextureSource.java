package bsa.source;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.j3d.Texture;

import tools.image.ImageFlip;
import tools.image.SimpleImageLoader;
import tools.texture.DDSToTexture;
import utils.source.TextureSource;
import FO3Archive.ArchiveEntry;
import FO3Archive.ArchiveFile;
import FO3Archive.ArchiveFile.Folder;

import com.sun.j3d.utils.image.TextureLoader;

public class BsaTextureSource implements TextureSource
{
	private List<ArchiveFile> bsas;

	public BsaTextureSource(List<ArchiveFile> bsas)
	{
		this.bsas = bsas;
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
			tex = DDSToTexture.checkCachedTexture(texName);
			if (tex != null)
			{
				return true;
			}

			for (ArchiveFile archiveFile : bsas)
			{
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
			tex = DDSToTexture.checkCachedTexture(texName);
			if (tex != null)
			{
				return tex;
			}

			for (ArchiveFile archiveFile : bsas)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
				if (archiveEntry != null)
				{
					try
					{

						InputStream in = archiveFile.getInputStream(archiveEntry);
						//String fileName = archiveEntry.getName();

						if (texName.endsWith(".dds"))
						{
							tex = DDSToTexture.getTexture(texName, in);
						}
						else
						{
							TextureLoader tl = new TextureLoader(ImageIO.read(in));
							tex = tl.getTexture();
						}

						if (tex != null)
						{
							return tex;
						}
					}
					catch (IOException e)
					{
						System.out.println("BsaTextureSource  " + texName + " " + e.getMessage());
					}
				}
			}
		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + texName);
		//new Throwable().printStackTrace();
		return null;
	}

	@Override
	public Image getImage(String imageName)
	{
		if (imageName != null && imageName.length() > 0)
		{

			// remove incorrect file path prefix, if it exists
			if (imageName.startsWith("data\\"))
			{
				imageName = imageName.substring(5);
			}

			// add the textures path part
			if (!imageName.startsWith("textures"))
			{
				imageName = "textures\\" + imageName;
			}

			for (ArchiveFile archiveFile : bsas)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(imageName);
				if (archiveEntry != null)
				{
					try
					{
						InputStream in = archiveFile.getInputStream(archiveEntry);

						BufferedImage image = SimpleImageLoader.getImage(imageName, in);

						if (image != null)
						{
							return ImageFlip.verticalflip(image);
						}
					}
					catch (IOException e)
					{
						System.out.println("BsaTextureSource  " + imageName + " " + e.getMessage());
					}
				}
			}
		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + imageName);
		new Throwable().printStackTrace();
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

			// add the textures path part
			if (!texName.startsWith("textures"))
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
