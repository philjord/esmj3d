package bsa.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.CompressedImageComponent2D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture2D;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.Folder;
import compressedtexture.CompressedBufferedImage;
import compressedtexture.DDSImage;
import compressedtexture.KTXImage;
import compressedtexture.dktxtools.ktx.KTXFormatException;
import javaawt.image.BufferedImage;
import texture.DDSToKTXConverter;
import utils.source.TextureSource;
import utils.source.file.FileTextureSource;

public class BsaTextureSource implements TextureSource {


	//DDS will be in .dds and are S3TC compress, KTX are .ktx and ETC2 compressed, ASTC will be .astc
	public enum AllowedTextureFormats {
		ALL, DDS, KTX, ASTC
	};

	public static AllowedTextureFormats	allowedTextureFormats	= AllowedTextureFormats.ALL;

	private List<ArchiveFile>			bsas;

	private FileTextureSource			fileTextureSource		= null;

	public BsaTextureSource(List<ArchiveFile> allBsas) {
		this.bsas = new ArrayList<ArchiveFile>();

		for (ArchiveFile archiveFile : allBsas) {
			if (archiveFile != null
				&& (archiveFile.hasTextureFiles() || archiveFile.hasKTX() || archiveFile.hasASTC())) {
				bsas.add(archiveFile);
			}
		}
		if (bsas.size() == 0) {
			System.out.print("No hasDDS or hasKTX  or hasASTC archive files found in:");
			for (ArchiveFile archiveFile : allBsas) {
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}

		if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE) {
			fileTextureSource = new FileTextureSource();
		}
	}

	public boolean hasDDS() {
		for (ArchiveFile archiveFile : bsas) {
			if (archiveFile.hasTextureFiles())
				return true;
		}
		return false;
	}

	public boolean hasKTX() {
		for (ArchiveFile archiveFile : bsas) {
			if (archiveFile.hasKTX())
				return true;
		}
		return false;
	}

	public boolean hasASTC() {
		for (ArchiveFile archiveFile : bsas) {
			if (archiveFile.hasASTC())
				return true;
		}
		return false;
	}

	@Override
	public boolean textureFileExists(String texName) {
		if (texName != null && texName.length() > 0) {
			texName = cleanTexName(texName);

			Texture tex = null;
			//check cache hit
			tex = CompressedTextureLoader.checkCachedTexture(texName);
			if (tex != null) {
				return true;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasTextureFiles() && allowedTextureFormats == AllowedTextureFormats.DDS)
					|| (archiveFile.hasKTX() && allowedTextureFormats == AllowedTextureFormats.KTX)
					|| (archiveFile.hasASTC() && allowedTextureFormats == AllowedTextureFormats.ASTC)) {
					String texNameForArchive = texName;
					if (archiveFile.hasKTX()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".ktx");
					} else if (archiveFile.hasASTC()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".tga.astc");
					}

					ArchiveEntry archiveEntry = archiveFile.getEntry(texNameForArchive);
					if (archiveEntry != null) {
						return true;
					}
				}
			}
		}

		if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE) {
			return fileTextureSource.textureFileExists(texName);
		}

		return false;
	}

	@Override
	public Texture getTexture(String texName) {
		if (texName != null && texName.length() > 0) {
			texName = cleanTexName(texName);

			Texture tex = null;

			//check cache hit
			tex = CompressedTextureLoader.checkCachedTexture(texName);
			if (tex != null) {
				return tex;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && (allowedTextureFormats == AllowedTextureFormats.DDS
													|| CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2))
					|| (archiveFile.hasKTX() && allowedTextureFormats == AllowedTextureFormats.KTX)
					|| (archiveFile.hasASTC() && allowedTextureFormats == AllowedTextureFormats.ASTC)) {
					String texNameForArchive = texName;
					if (archiveFile.hasKTX()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".ktx");
					} else if (archiveFile.hasASTC()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".tga.astc");
					}

					ArchiveEntry archiveEntry = archiveFile.getEntry(texNameForArchive);
					if (archiveEntry != null) {
						try {
							//InputStream in = archiveFile.getInputStream(archiveEntry);
							ByteBuffer in = archiveFile.getByteBuffer(archiveEntry, true);
							if(in != null) {
								if (texNameForArchive.endsWith(".dds")) {
									if (CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2)
										tex = CompressedTextureLoaderETCPackDDS.getTexture(texNameForArchive, in);
									else
										tex = CompressedTextureLoader.DDS.getTexture(texNameForArchive, in);
								} else if (texNameForArchive.endsWith(".astc") || texNameForArchive.endsWith(".atc")) {
									tex = CompressedTextureLoader.ASTC.getTexture(texNameForArchive, in);
								} else if (texNameForArchive.endsWith(".ktx")) {
									tex = CompressedTextureLoader.KTX.getTexture(texNameForArchive, in);
								} else {
									//FIXME: generic texture loading system
									/*TextureLoader tl = new TextureLoader(ImageIO.read(in));
									tex = tl.getTexture();*/
								}
	
								if (tex != null) {
									return tex;
								}
							}
						} catch (IOException e) {
							System.out.println(
									"BsaTextureSource  " + texNameForArchive + " " + e + " " + e.getStackTrace()[0]);
						}
					}
				}
			}

			if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE) {
				Texture mc = fileTextureSource.getTexture(texName);
				if (mc != null)
					return mc;
			}
		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + texName);
		//new Throwable().printStackTrace();
		return null;
	}

	@Override
	public TextureUnitState getTextureUnitState(String texName) {
		return getTextureUnitState(texName, false); 
	}
	@Override
	public TextureUnitState getTextureUnitState(String texName, boolean dropMip0) {
		if (texName != null && texName.length() > 0) {
			texName = cleanTexName(texName);

			TextureUnitState tex = null;

			//check cache hit
			tex = CompressedTextureLoader.checkCachedTextureUnitState(texName, dropMip0);
			if (tex != null) {
				return tex;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && (allowedTextureFormats == AllowedTextureFormats.DDS
													|| CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2))
					|| (archiveFile.hasKTX() && allowedTextureFormats == AllowedTextureFormats.KTX)
					|| (archiveFile.hasASTC() && allowedTextureFormats == AllowedTextureFormats.ASTC)) {
					String texNameForArchive = texName;
					if (archiveFile.hasKTX()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".ktx");
					} else if (archiveFile.hasASTC()) {
						texNameForArchive = texNameForArchive.replace(".dds", ".tga.astc");
					}

					ArchiveEntry archiveEntry = archiveFile.getEntry(texNameForArchive);
					if (archiveEntry != null) {
						try {
							//InputStream in = archiveFile.getInputStream(archiveEntry);
							ByteBuffer in = archiveFile.getByteBuffer(archiveEntry, true);
							if(in != null) {
									if (texNameForArchive.endsWith(".dds")) {
									if (CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2)
										tex = CompressedTextureLoaderETCPackDDS.getTextureUnitState(texNameForArchive, in);
									else
										tex = CompressedTextureLoader.DDS.getTextureUnitState(texNameForArchive, in, dropMip0);
								} else if (texNameForArchive.endsWith(".astc") || texNameForArchive.endsWith(".atc")) {
									tex = CompressedTextureLoader.ASTC.getTextureUnitState(texNameForArchive, in, dropMip0);
								} else if (texNameForArchive.endsWith(".ktx")) {
									tex = CompressedTextureLoader.KTX.getTextureUnitState(texNameForArchive, in, dropMip0);
								} else {
									//FIXME: generic texture loading system good for png images
									/*TextureLoader tl = new TextureLoader(ImageIO.read(in));
									tex = tl.getTexture();*/
								}
	
								if (tex != null) {
									return tex;
								}
							}
						} catch (IOException e) {
							System.out.println(
									"BsaTextureSource  " + texNameForArchive + " " + e + " " + e.getStackTrace()[0]);
						}
					}
				}
			}

			if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE) {
				TextureUnitState mc = fileTextureSource.getTextureUnitState(texName);
				if (mc != null)
					return mc;
			}
		}

		//Many times this will fall through here, if texture doesn't exist for example
		//System.out.println("BsaTextureSource TextureUnitState not found in archive bsas: " + texName);
		//new Throwable().printStackTrace();
		return null;
	}

	@Override
	public List<String> getFilesInFolder(String folderName) {
		ArrayList<String> ret = new ArrayList<String>();

		for (ArchiveFile archiveFile : bsas) {
			Folder folder = archiveFile.getFolder(folderName, true);
			if (folder != null) {
				for (int i = 0; i < folder.fileToHashMap.size(); i++) {
					ArchiveEntry e = folder.fileToHashMap.get(folder.fileToHashMap.keyAt(i));
					ret.add(folderName + "\\" + e.getFileName());
				}
			}
		}

		return ret;
	}

	public InputStream getInputStream(String texName) {
		if (texName != null && texName.length() > 0) {
			texName = cleanTexName(texName);

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasTextureFiles() && allowedTextureFormats == AllowedTextureFormats.DDS)
					|| (archiveFile.hasKTX() && allowedTextureFormats == AllowedTextureFormats.KTX)
					|| (archiveFile.hasASTC() && allowedTextureFormats == AllowedTextureFormats.ASTC)) {
					ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
					if (archiveEntry != null) {
						try {
							InputStream in = archiveFile.getInputStream(archiveEntry);
							return in;
						} catch (IOException e) {

							e.printStackTrace();
						}

					}
				}
			}

		}
		System.out.println("BsaTextureSource texture not found in archive bsas: " + texName);
		new Throwable().printStackTrace();
		return null;
	}

	/**
	 * add the textures path part (unless one of the other types)
	 * @param texName
	 * @return
	 */
	private static String cleanTexName(String texName) {
		texName = texName.toLowerCase();

		// remove incorrect file path prefix, if it exists
		if (texName.startsWith("data\\")) {
			texName = texName.substring(5);
		}

		if (!texName.startsWith("textures") && !texName.startsWith("bookart") && !texName.startsWith("interface")) {
			texName = "textures\\" + texName;
		}
		return texName;
	}

	/**
	 * Crazy test to see if swapping dds to etc is worth doing on the fly
	 * @author pjnz
	 *
	 */
	public static class CompressedTextureLoaderETCPackDDS extends CompressedTextureLoader {

		public static boolean CONVERT_DDS_TO_ETC2 = false;

		public static TextureUnitState getTextureUnitState(File file) {
			String filename = file.getAbsolutePath();
			try {
				return getTextureUnitState(filename, new FileInputStream(file));
			} catch (IOException e) {
				System.out.println(""	+ DDS.class + " had a  IO problem with " + filename + " : " + e + " "
									+ e.getStackTrace()[0]);
				return null;
			}
		}

		public static TextureUnitState getTextureUnitState(String filename, InputStream inputStream) {
			TextureUnitState ret_val = checkCachedTextureUnitState(filename);

			if (ret_val == null) {
				Texture tex = getTexture(filename, inputStream);
				//notice nulls are fine

				TextureUnitState tus = new TextureUnitState();
				tus.setTexture(tex);
				tus.setName(filename);
				cacheTextureUnitState(filename, tus);
				ret_val = tus;
			}
			return ret_val;
		}

		public static TextureUnitState getTextureUnitState(String filename, ByteBuffer inputBuffer) {
			TextureUnitState ret_val = checkCachedTextureUnitState(filename);

			if (ret_val == null) {
				Texture tex = getTexture(filename, inputBuffer);
				//notice nulls are fine

				TextureUnitState tus = new TextureUnitState();
				tus.setTexture(tex);
				tus.setName(filename);
				cacheTextureUnitState(filename, tus);
				ret_val = tus;
			}
			return ret_val;
		}

		/**
		 * Returns the associated Texture object or null if the image failed to load Note it may return a Texture loaded
		 * earlier
		 * @param filename just a useful name for the inputstream's source
		 * @param inputStream which is fully read into a {@code ByteBuffer} and must contain a dds texture
		 * @return A {@code Texture} with the associated DDS image
		 */
		public static Texture getTexture(String filename, InputStream inputStream) {
			// Check the cache for an instance first
			Texture ret_val = checkCachedTexture(filename);

			if (ret_val == null) {
				try {
					DDSImage ddsImage = DDSImage.read(toByteBuffer(inputStream));
					return createTextureETCPack(filename, ddsImage);
				} catch (IOException e) {
					System.out.println(""	+ DDS.class + " had an IO problem with " + filename + " : " + e + " "
										+ e.getStackTrace()[0]);
					return null;
				}
			}
			return ret_val;
		}

		/**
		 * Note avoid mappedbytebufffers as that will push texture loading (disk activity) onto the j3d thread which is
		 * bad, pull everything into byte arrays on the current thread
		 * @param filename
		 * @param inputBuffer
		 * @return
		 */

		public static Texture getTexture(String filename, ByteBuffer inputBuffer) {
			// Check the cache for an instance first
			Texture ret_val = checkCachedTexture(filename);

			if (ret_val == null) {
				try {
					DDSImage ddsImage = DDSImage.read(inputBuffer);
					return createTextureETCPack(filename, ddsImage);
				} catch (IOException e) {
					System.out.println(""	+ DDS.class + " had an IO problem with " + filename + " : " + e + " "
										+ e.getStackTrace()[0]);
					return null;
				}
			}
			return ret_val;
		}

		private static Texture2D createTextureETCPack(String filename, DDSImage ddsImage) {

			// return null for unsupported types
			if (ddsImage.getPixelFormat() == DDSImage.D3DFMT_DXT2 //
				|| ddsImage.getPixelFormat() == DDSImage.D3DFMT_DXT4 //
				|| ddsImage.getPixelFormat() == DDSImage.D3DFMT_UNKNOWN) {
				System.out.println("Unsupported DDS format " + ddsImage.getPixelFormat() + " for file " + filename);
				return null;
			}

			Texture2D tex = null;

			ByteBuffer ktxBB = DDSToKTXConverter.convertDDSToKTX(ddsImage, filename);

			if (ktxBB != null) {
				try {
					KTXImage ktxImage = new KTXImage(ktxBB);

					int levels = ktxImage.getNumMipMaps();
					// now check how big it should be! sometime these things run out with 0 width or 0 height size images
					int levels2 = Math.min(computeLog(ktxImage.getWidth()), computeLog(ktxImage.getHeight())) + 1;
					// use the lower of the two, to avoid 0 sizes going to the driver
					levels = levels > levels2 ? levels2 : levels;

					// always 1 level
					levels = levels == 0 ? 1 : levels;

					int mipMapMode = ktxImage.getNumMipMaps() <= 1 ? Texture.BASE_LEVEL : Texture.MULTI_LEVEL_MIPMAP;

					//note Texture.RGBA is not used on the pipeline for compressed image, the buffered image holds that info
					tex = new Texture2D(mipMapMode, Texture.RGBA, ktxImage.getWidth(), ktxImage.getHeight());

					tex.setName(filename);

					tex.setBaseLevel(0);
					tex.setMaximumLevel(levels - 1);

					tex.setBoundaryModeS(Texture.WRAP);
					tex.setBoundaryModeT(Texture.WRAP);

					// better to let machine decide
					tex.setMinFilter(Texture.NICEST);
					tex.setMagFilter(Texture.NICEST);

					//defaults to Texture.ANISOTROPIC_NONE
					if (anisotropicFilterDegree > 0) {
						tex.setAnisotropicFilterMode(Texture.ANISOTROPIC_SINGLE_VALUE);
						tex.setAnisotropicFilterDegree(anisotropicFilterDegree);
					}

					for (int i = 0; i < levels; i++) {
						BufferedImage image = new CompressedBufferedImage.KTX(ktxImage, i, filename);
						tex.setImage(i, new CompressedImageComponent2D(ImageComponent.FORMAT_RGBA, image));
					}
				} catch (KTXFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.err.println("Not a DDSToKTXConverter.convertDDSToKTX returned null for " + filename);
			}

			cacheTexture(filename, tex);

			return tex;

		}
	}

}
