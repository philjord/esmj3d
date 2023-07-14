package bsa.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.Folder;
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
		//Always look for ktx before the others, it was the widest support
		Collections.sort(bsas, new Comparator<ArchiveFile>() {
		    @Override
		    public int compare(ArchiveFile lhs, ArchiveFile rhs) {
		        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
		        return lhs.hasKTX() ? -1 : lhs.hasASTC() ? 0 : 1;
		    }
		});
		

		if (BsaMeshSource.FALLBACK_TO_FILE_SOURCE) {
			fileTextureSource = new FileTextureSource();
		}
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

}
