package bsa.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import nif.niobject.bgsm.BSMaterial;
import nif.niobject.bgsm.BSMaterialDataBGEM;
import nif.niobject.bgsm.BSMaterialDataBGSM;
import nif.niobject.bgsm.bsmatcdb.BSMaterialsCDB;
import tools.WeakValueHashMap;
import utils.source.MaterialsSource;

public class BsaMaterialsSource extends MaterialsSource {

	private List<ArchiveFile>	bsas;
	
	private List<ArchiveFile>	bsasCDB;//starfield one db file in the ba2 file
	protected static WeakValueHashMap<String, BSMaterial>	materialFilesCDB	= new WeakValueHashMap<String, BSMaterial>();

	public BsaMaterialsSource(List<ArchiveFile> allBsas) {
		this.bsas = new ArrayList<ArchiveFile>();
		this.bsasCDB = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas) {
			if (archiveFile != null && archiveFile.hasMaterials()) {
				bsas.add(archiveFile);
			}
		}
		
		for (ArchiveFile archiveFile : allBsas) {
			if (archiveFile != null && archiveFile.hasMaterialCDB()) {
				bsasCDB.add(archiveFile);
			}
		}
		
		if (bsas.size() == 0 && bsasCDB.size() == 0) {
			System.out.print("No hasMaterials archive files found in:");
			for (ArchiveFile archiveFile : allBsas) {
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}		
	}

	@Override
	public BSMaterialDataBGEM getEffectMaterial(String fileName) {
		BSMaterial material = getMaterial(fileName);
		if (!(material instanceof BSMaterialDataBGEM)) {
			// it is possible for a desired EffectMaterial to have the header string BGSM and 
			// thus cause chaos about now 
			//example FO4: Materials\SetDressing\WaterCooler\WaterCooler_Dirty.BGEM
			return null;
		}

		return (BSMaterialDataBGEM)material;
	}

	@Override
	public BSMaterialDataBGSM getShaderMaterial(String fileName) {

		return (BSMaterialDataBGSM)getMaterial(fileName);
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
	
	
	
	public static BSMaterialsCDB materialsCDB;
	/**h
	 * https://forums.nexusmods.com/topic/13361451-starfields-cdb-material-database/
	 starfield in a cdb file in a ba2
	 */
	@Override
	public BSMaterial readMaterialFileCDB(String fileName) {

		// load on first read
		if (materialsCDB == null) {
			for (ArchiveFile archiveFile : bsasCDB) {
				ArchiveEntry archiveEntry = archiveFile.getEntry("materials\\materialsbeta.cdb");
				if (archiveEntry != null) {
					try {
						ByteBuffer in = archiveFile.getByteBuffer(archiveEntry);
						if (in != null) {
							in.order(ByteOrder.LITTLE_ENDIAN);
							materialsCDB = new BSMaterialsCDB(in);
							break;
						} else {
							System.err.println("materials\\materialsbeta.cdb Not Found in Material BSAs");
							return null;
						}

					} catch (IOException e) {
						System.out.println("BsaMaterialsSource  " + fileName + " " + e + " " + e.getStackTrace()[0]);
					}

				}
			}
			return null;
		}
		
		
		BSMaterial material = materialFilesCDB.get(fileName);

		if (material != null)
			return material;

		 
		try {
			material = materialsCDB.getMaterialFileCDB(fileName);
		} catch (IOException e) {							 
			e.printStackTrace();
		}
		if (material != null) {
			materialFilesCDB.put(fileName, material);	
			return material;
		} 
					 
		 
		return null;
	}
	

	 

}
