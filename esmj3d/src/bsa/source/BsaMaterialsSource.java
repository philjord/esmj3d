package bsa.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import nif.niobject.bgsm.BSMaterial;
import nif.niobject.bgsm.EffectMaterial;
import nif.niobject.bgsm.ShaderMaterial;
import utils.source.BgsmSource;
import utils.source.file.FileMeshSource;

public class BsaMaterialsSource extends BgsmSource {

	public static boolean		FALLBACK_TO_FILE_SOURCE	= false;
	private List<ArchiveFile>	bsas;
	private FileMeshSource		fileMeshSource			= null;

	public BsaMaterialsSource(List<ArchiveFile> allBsas) {
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas) {
			if (archiveFile != null && archiveFile.hasMaterials()) {
				bsas.add(archiveFile);
			}
		}

		if (bsas.size() == 0 && !FALLBACK_TO_FILE_SOURCE) {
			System.out.print("No hasMaterials archive files found in:");
			for (ArchiveFile archiveFile : allBsas) {
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}

		if (FALLBACK_TO_FILE_SOURCE) {
			fileMeshSource = new FileMeshSource();
		}
	}

	@Override
	public EffectMaterial getEffectMaterial(String fileName) {
		BSMaterial material = getMaterial(fileName);
		if (!(material instanceof EffectMaterial)) {
			// it is possible for a desired EffectMaterial to have the header string BGSM and 
			// thus cause chaos about now 
			//example FO4: Materials\SetDressing\WaterCooler\WaterCooler_Dirty.BGEM
			return null;
		}

		return (EffectMaterial)material;
	}

	@Override
	public ShaderMaterial getShaderMaterial(String fileName) {

		return (ShaderMaterial)getMaterial(fileName);
	}

	public BSMaterial getMaterial(String fileName) {

		// the chop up name thing for things like
		//C:\Projects\Fallout4\Build\PC\Data\materials\Interiors\Utility\MetalUtilityDoor01.BGSM

		if (fileName.length() > 0) {
			if (!fileName.toLowerCase().startsWith("materials")) {
				if (fileName.toLowerCase().indexOf("materials") == -1) {
					fileName = "materials\\" + fileName;
				} else {
					fileName = fileName.substring(fileName.toLowerCase().indexOf("materials"));
				}
			}

			//fallout 4 missing  materials\Architecture\Buildings\BrickWhite01R.BGSM
			// drop the R as that file exists
			//fileName = fileName.replace("BrickWhite01R.BGSM", "BrickWhite01.BGSM");

			BSMaterial material = materialFiles.get(fileName);

			if (material != null)
				return material;

			for (ArchiveFile archiveFile : bsas) {
				ArchiveEntry archiveEntry = archiveFile.getEntry(fileName);
				if (archiveEntry != null) {
					try {
						ByteBuffer inputStream = archiveFile.getByteBuffer(archiveEntry);

						material = readMaterialFile(fileName, inputStream);
						if (material != null) {
							materialFiles.put(fileName, material);
							return material;
						}
					} catch (IOException e) {
						System.out.println("BsaMaterialsSource  " + fileName + " " + e + " " + e.getStackTrace()[0]);
					}

				}
			}

			if (FALLBACK_TO_FILE_SOURCE) {
				try {
					material = readMaterialFile(fileName, fileMeshSource.getByteBuffer(fileName));

					if (material != null)
						return material;
				} catch (IOException e) {
					System.out.println(
							"BsaMaterialsSource:FileBgsmSource " + fileName + " " + e + " " + e.getStackTrace()[0]);
				}
			}

			//oops no material today
			if (!warningGiven.contains(fileName)) {
				System.out.print("getMaterial Material " + fileName + " not found in archive bsas");
				for (ArchiveFile archiveFile : bsas) {
					System.out.print(" checked: " + archiveFile.getName() + ", ");
				}
				System.out.println("");
				warningGiven.add(fileName);
			}
		}
		return null;
	}

	private static HashSet<String> warningGiven = new HashSet<String>();

}
