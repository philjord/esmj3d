package bsa.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import nif.BgsmSource;
import nif.niobject.bgsm.BSMaterial;
import nif.niobject.bgsm.BgsmFile;
import utils.source.file.FileMeshSource;
 

public class BsaMaterialsSource extends BgsmSource {
	

	public static boolean FALLBACK_TO_FILE_SOURCE = false;
	private List<ArchiveFile> bsas;
	private FileMeshSource fileMeshSource = null;
	
	public BsaMaterialsSource(List<ArchiveFile> allBsas)
	{
		this.bsas = new ArrayList<ArchiveFile>();
		for (ArchiveFile archiveFile : allBsas)
		{
			if (archiveFile != null && archiveFile.hasMaterials())
			{
				bsas.add(archiveFile);
			}
		}

		if (bsas.size() == 0 && !FALLBACK_TO_FILE_SOURCE)
		{
			System.out.print("No hasMaterials archive files found in:");
			for (ArchiveFile archiveFile : allBsas)
			{
				System.out.print(" Looked in Archive:" + archiveFile.getName());
			}
			System.out.println("");
		}

		if (FALLBACK_TO_FILE_SOURCE)
		{
			fileMeshSource = new FileMeshSource();
		}
	}
	
	@Override
	public BSMaterial getMaterial(String fileName) throws IOException
	{	
		
		// the chop up name thing for things like
		//C:\Projects\Fallout4\Build\PC\Data\materials\Interiors\Utility\MetalUtilityDoor01.BGSM
		

		
		if (fileName.length() > 0)
		{
			if (!fileName.toLowerCase().startsWith("materials"))
			{
				if (fileName.toLowerCase().indexOf("materials") == -1)
				{
					fileName = "materials\\" + fileName;
				} else {
 					fileName = fileName.substring(fileName.toLowerCase().indexOf("materials"));
				}				
			}

			BSMaterial material = materialFiles.get(fileName);

			if (material == null)
			{
				for (ArchiveFile archiveFile : bsas)
				{
					ArchiveEntry archiveEntry = archiveFile.getEntry(fileName);
					if (archiveEntry != null)
					{
						try
						{					 
							ByteBuffer inputStream = archiveFile.getByteBuffer(archiveEntry);
							try
							{
								material = BgsmFile.readMaterialFile(fileName, inputStream);
								materialFiles.put(fileName, material);
							}
							catch (IOException e)
							{
								System.out.println("BsaMaterialsSource:  " + fileName + " " + e + " " + e.getStackTrace()[0]);
							}
	
							if (material != null)
							{
								return material;
							}
						}
						catch (IOException e)
						{
							System.out.println("BsaMaterialsSource  " + fileName + " " + e + " " + e.getStackTrace()[0]);
						}
	
					}
				}
			} else {
				return material;
			}
			

			if (FALLBACK_TO_FILE_SOURCE)
			{
				material  = BgsmFile.readMaterialFile(fileName, fileMeshSource.getByteBuffer(fileName));
				if (material != null)
					return material;
			}

			System.out.print("getMaterial Material " + fileName + " not found in archive bsas");
			for (ArchiveFile archiveFile : bsas)
			{
				System.out.print(" checked: " + archiveFile.getName() + ", ");
			}
			System.out.println("");
		}
		return null;
	}

}
