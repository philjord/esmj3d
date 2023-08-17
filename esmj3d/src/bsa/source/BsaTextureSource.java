package bsa.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.jogamp.java3d.CompressedImageComponent2D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.NioImageBuffer;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture2D;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.java3d.compressedtexture.dktxtools.dds.DDSDecompressor;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.Folder;
import compressedtexture.CompressedBufferedImage;
import compressedtexture.DDSImage;
import compressedtexture.KTXImage;
import compressedtexture.dktxtools.ktx.KTXFormatException;
import etcpack.ETCPack;
import etcpack.ETCPack.FORMAT;
import javaawt.image.BufferedImage;
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
			if (archiveFile != null && (archiveFile.hasDDS() || archiveFile.hasKTX() || archiveFile.hasASTC())) {
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
			if (archiveFile.hasDDS())
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
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\")) {
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures")) {
				texName = "textures\\" + texName;
			}

			Texture tex = null;
			//check cache hit
			tex = CompressedTextureLoader.checkCachedTexture(texName);
			if (tex != null) {
				return true;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && allowedTextureFormats == AllowedTextureFormats.DDS)
					|| (archiveFile.hasKTX() && allowedTextureFormats == AllowedTextureFormats.KTX)
					|| (archiveFile.hasASTC() && allowedTextureFormats == AllowedTextureFormats.ASTC)) {
					if (archiveFile.hasKTX()) {
						texName = texName.replace(".dds", ".ktx");
					} else if (archiveFile.hasASTC()) {
						texName = texName.replace(".dds", ".tga.astc");
					}

					ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
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
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\")) {
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures")) {
				texName = "textures\\" + texName;
			}

			Texture tex = null;

			//check cache hit
			tex = CompressedTextureLoader.checkCachedTexture(texName);
			if (tex != null) {
				return tex;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && allowedTextureFormats == AllowedTextureFormats.DDS)
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

							if (texNameForArchive.endsWith(".dds")) {
								if(CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2)
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
						} catch (IOException e) {
							System.out.println(
									"BsaTextureSource  " + texNameForArchive + " " + e + " " + e.getStackTrace() [0]);
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
		if (texName != null && texName.length() > 0) {
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\")) {
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures")) {
				texName = "textures\\" + texName;
			}

			TextureUnitState tex = null;

			//check cache hit
			tex = CompressedTextureLoader.checkCachedTextureUnitState(texName);
			if (tex != null) {
				return tex;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && allowedTextureFormats == AllowedTextureFormats.DDS)
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

							if (texNameForArchive.endsWith(".dds")) {
								if(CompressedTextureLoaderETCPackDDS.CONVERT_DDS_TO_ETC2)
									tex = CompressedTextureLoaderETCPackDDS.getTextureUnitState(texNameForArchive, in);
								else
									tex = CompressedTextureLoader.DDS.getTextureUnitState(texNameForArchive, in);
							} else if (texNameForArchive.endsWith(".astc") || texNameForArchive.endsWith(".atc")) {
								tex = CompressedTextureLoader.ASTC.getTextureUnitState(texNameForArchive, in);
							} else if (texNameForArchive.endsWith(".ktx")) {
								tex = CompressedTextureLoader.KTX.getTextureUnitState(texNameForArchive, in);
							} else {
								//FIXME: generic texture loading system
								/*TextureLoader tl = new TextureLoader(ImageIO.read(in));
								tex = tl.getTexture();*/
							}

							if (tex != null) {
								return tex;
							}
						} catch (IOException e) {
							System.out.println(
									"BsaTextureSource  " + texNameForArchive + " " + e + " " + e.getStackTrace() [0]);
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

		//No many times this will fall through here, if texture doesn't exist for example
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
			texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\")) {
				texName = texName.substring(5);
			}

			// add the textures path part (unless tes3 bookart folder)
			if (!texName.startsWith("textures") && !texName.startsWith("bookart")) {
				texName = "textures\\" + texName;
			}

			for (ArchiveFile archiveFile : bsas) {
				// shall we inspect this archive?
				if (allowedTextureFormats == AllowedTextureFormats.ALL
					|| (archiveFile.hasDDS() && allowedTextureFormats == AllowedTextureFormats.DDS)
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
									+ e.getStackTrace() [0]);
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
		 * @param filename just a useful name for teh inputstreams source
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
					System.out.println(""	+ DDS.class + " had a  IO problem with " + filename + " : " + e + " "
										+ e.getStackTrace() [0]);
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
					System.out.println(""	+ DDS.class + " had a  IO problem with " + filename + " : " + e + " "
										+ e.getStackTrace() [0]);
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

			NioImageBuffer decompressedImage = new DDSDecompressor(ddsImage, 0, filename).convertImageNio();
			Buffer b = decompressedImage.getDataBuffer();
			if (b instanceof ByteBuffer) {
				//ok so now find the RGB or RGBA byte buffers
				ByteBuffer bb = (ByteBuffer)decompressedImage.getDataBuffer();
				byte[] img = null;
				byte[] imgalpha = null;
				if (decompressedImage.getImageType() == NioImageBuffer.ImageType.TYPE_3BYTE_RGB) {
					// just put the RGB data straight into the img byte array 
					img = new byte[bb.capacity()];
					bb.get(img, 0, bb.capacity());
				} else if (decompressedImage.getImageType() == NioImageBuffer.ImageType.TYPE_4BYTE_RGBA) {
					byte[] ddsimg = new byte[bb.capacity()];
					bb.get(ddsimg, 0, bb.capacity());
					// copy RGB 3 sets out then 1 sets of alpha 
					img = new byte[(bb.capacity() / 4) * 3];
					imgalpha = new byte[(bb.capacity() / 4)];
					for (int i = 0; i < img.length / 3; i++) {
						img [i * 3 + 0] = ddsimg [i * 4 + 0];
						img [i * 3 + 1] = ddsimg [i * 4 + 1];
						img [i * 3 + 2] = ddsimg [i * 4 + 2];
						imgalpha [i] = ddsimg [i * 4 + 3];
					}
				} else {
					System.err.println("Bad Image Type " + decompressedImage.getImageType());
					return null;
				}

				//System.out.println("Debug of dds image " + filename);
				//ddsImage.debugPrint();
				int fmt = ddsImage.getPixelFormat();
				FORMAT format = FORMAT.ETC2PACKAGE_RGBA;

				if (fmt == DDSImage.D3DFMT_R8G8B8) {
					format = FORMAT.ETC2PACKAGE_RGB;
				} else if (fmt == DDSImage.D3DFMT_A8R8G8B8 || fmt == DDSImage.D3DFMT_X8R8G8B8) {
					format = FORMAT.ETC2PACKAGE_RGBA;
				} else if (fmt == DDSImage.D3DFMT_DXT1) {
					// DXT1 might have the odd punch through alpha in it, but there is no way to say if it's just RGB or RGB and some A1
					format = FORMAT.ETC2PACKAGE_RGBA1;
				} else if (fmt == DDSImage.D3DFMT_DXT2	|| fmt == DDSImage.D3DFMT_DXT3 || fmt == DDSImage.D3DFMT_DXT4
							|| fmt == DDSImage.D3DFMT_DXT5) {
					format = FORMAT.ETC2PACKAGE_RGBA;
				}

				KTXImage ktxImage = null;
				ByteBuffer ktxBB = null;
				try {
					ETCPack ep = new ETCPack();
					if(filename.contains("neoclassicalmaintile03")) {
						System.out.println("hidy ho");
					}
					ktxBB = ep.compressImageToByteBuffer(img, imgalpha, ddsImage.getWidth(), ddsImage.getHeight(), format,
							true);
					
					ktxImage = new KTXImage(ktxBB);
				} catch (KTXFormatException e) {
					System.out.println("DDS to KTX image: " + filename);
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("DDS to KTX image: " + filename);
					e.printStackTrace();
				} catch (BufferOverflowException e) {
					System.out.println("DDS to KTX image: " + filename);
					e.printStackTrace();
				}
				ddsImage.close();
				
				if(filename.contains("neoclassicalmaintile03")) {
					try {
						filename = "D:\\temp\\"+filename.substring(filename.indexOf("textures"))+".ktx";
						File file = new File(filename);
						file.getParentFile().mkdirs();
					//yeeha! lets write this bad boy out to a file!!!
					RandomAccessFile raf = new RandomAccessFile(filename, "rw");
					
					FileChannel fc = raf.getChannel();
					ktxBB.rewind();
						fc.write(ktxBB);
						fc.close();
						ktxBB.rewind();
					} catch (IOException e1) {
				 
						e1.printStackTrace();
					}
				}

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

			} else {
				System.err.println("Not a ByteBuffer " + b);
			}

			cacheTexture(filename, tex);

			

			return tex;

		}
	}

}
