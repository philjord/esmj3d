package bsa.source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;

import org.jogamp.java3d.NioImageBuffer;
import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;
import org.jogamp.java3d.compressedtexture.dktxtools.dds.DDSDecompressor;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.SIG;
import bsaio.DBException;
import bsaio.HashCode;
import bsaio.displayables.DisplayableArchiveEntry;
import compressedtexture.DDSImage;
import etcpack.ETCPack.FORMAT;
import etcpack.QuickETC;
import tools.io.FileChannelRAF;

public class DDSToKTXBsaConverter extends Thread {

	private static final boolean CONVERT_DDS_to_KTX = true;

	private FileChannel			outputArchiveFile;

	private ArchiveFile				inputArchive;

	private boolean					completed;

	private int						archiveFlags;

	private int						fileFlags;

	private int						folderCount;

	private int						fileCount;

	private int						folderNamesLength;

	private int						fileNamesLength;

	private ArrayList<ArchiveEntry>	entries;

	private ArrayList<Folder>		folders;

	//TODO: in fact accept a file channel here please
	public DDSToKTXBsaConverter(FileChannel outputArchiveFile, ArchiveFile inputArchive) {
		completed = false;
		this.outputArchiveFile = outputArchiveFile;
		this.inputArchive = inputArchive;
	}

	@Override
	public void run() {
		FileChannelRAF out = null;
		try {
			entries = new ArrayList<ArchiveEntry>(256);
			folders = new ArrayList<Folder>(256);
			archiveFlags = 3; // this is  2  and 1 being folders and filenames :  4 is compressed, (0x100==256) is required for names to be written
			
			if(inputArchive.getSig() != SIG.TES3)//tes3 no compression
				archiveFlags |= 4;
			fileFlags = 0;
			
			List<ArchiveEntry> inEntries = inputArchive.getEntries(); 
			// notice we require the loader to have keep the displayable version which holds the folder name per entry
			for (ArchiveEntry entry : inEntries) {				
				insertEntry(entry);				
			}

			if (fileCount != 0) {				
				//file channel had better be empty file by now!
				out = new FileChannelRAF(outputArchiveFile);
				writeArchive(out);
				out.close();
				out = null;
			}
			completed = true;
		} catch (DBException exc) {
			System.out.println("Format error while creating archive");
			exc.printStackTrace();
		} catch (IOException exc) {
			System.out.println("I/O error while creating archive");
			exc.printStackTrace();
		} catch (Throwable exc) {
			System.out.println("Exception while creating archive");
			exc.printStackTrace();
		}

		if (!completed && out != null) {
			try {
				out.close();
				out = null;
			} catch (IOException exc) {
				System.out.println("I/O error while cleaning up");
				exc.printStackTrace();
			}
		}
	
	}
  

	private void insertEntry(ArchiveEntry entry) throws DBException {
		String folderName = ((DisplayableArchiveEntry) entry).getFolderName();
		//String baseName = "";// no base as we are coming from a BSA file

		//folderName = folderName.substring(baseName.length() + 1);
		if (folderName.length() > 254) {
			throw new DBException("Maximum folder path length is 254 characters");
		}

		
		if(CONVERT_DDS_to_KTX && entry.getFileName().endsWith(".dds")) {
			entry.setFileName(entry.getFileName().replace(".dds",".ktx"));
		}
		
		String fileName = ((DisplayableArchiveEntry) entry).getName();
		if (fileName.length() > 254) {
			throw new DBException("Maximum file name length is 254 characters");
		}

		boolean insert = true;

		int count = entries.size();
		int i = 0;
		while (i < count) {
			ArchiveEntry listEntry = entries.get(i);
			int diff = entry.compareTo(listEntry);
			if (diff == 0) {
				throw new DBException("Hash collision: '"	+ ((DisplayableArchiveEntry)entry).getName() + "' and '"
						+ ((DisplayableArchiveEntry)listEntry).getName() + "'");
			}
			if (diff < 0) {
				insert = false;
				entries.add(i, entry);
				break;
			}
			i++;
		}

		if (insert) {
			entries.add(entry);
		}

		int sep = fileName.lastIndexOf('.');
		if (sep >= 0) {
			String ext = fileName.substring(sep);
			if (ext.equals(".nif")) {
				fileFlags |= 1;
				archiveFlags |= 0x80;
			} else if (ext.equals(".dds") || ext.equals(".ktx")) {
				fileFlags |= 2;
				archiveFlags |= 0x100;
			} else if (ext.equals(".kf")) {
				fileFlags |= 0x40;
			} else if (ext.equals(".wav")) {
				fileFlags |= 8;
				archiveFlags |= 0x10;
			} else if (ext.equals(".lip")) {
				fileFlags |= 8;
			} else if (ext.equals(".mp3")) {
				fileFlags |= 0x10;
				archiveFlags |= 0x10;
				if(inputArchive.getSig() != SIG.TES3)//tes3 all sorts in the file
					archiveFlags &= -5;
			} else if (ext.equals(".ogg")) {
				fileFlags |= 0x10;
				if(inputArchive.getSig() != SIG.TES3)//tes3 all sorts in the file
					archiveFlags &= -5;
			} else if (ext.equals(".xml")) {
				fileFlags |= 0x100;
			}
		}

		if ((inputArchive.getSig() != SIG.TES3) && ((fileFlags & 2) != 0 && (fileFlags & -3) != 0)) {
			throw new DBException("Texture files must be packaged by themselves");
		}
		insert = true;
		Iterator<Folder> i$ = folders.iterator();
		while (i$.hasNext()) {
			Folder folder = i$.next();
			if (!folder.getName().equals(folderName))
				continue;

			folder.incrementFileCount();
			insert = false;
			break;
		}

		if (insert) {
			Folder folder = new Folder(folderName);
			folder.incrementFileCount();

			for (int i2 = 0; i2 < folders.size(); i2++) {
				Folder listFolder = folders.get(i2);
				if (folder.getHashCode().compareTo(listFolder.getHashCode()) < 0) {
					insert = false;
					folders.add(i2, folder);
					break;
				}
			}

			if (insert) {
				folders.add(folder);
			}
			folderNamesLength += folderName.length() + 1;
			folderCount++;
		}
		fileNamesLength += fileName.length() + 1;
		fileCount++;
	}

	/**
	 * Note this writes out a ArchiveFileBsa version fo archive files, not Btdx or starfields one
	 * @param out
	 * @throws DBException
	 * @throws IOException
	 */
	private void writeArchive(FileChannelRAF out) throws DBException, IOException {
		byte[] buffer = new byte[256];
		byte[] dataBuffer = new byte[32000];
		byte[] compressedBuffer = new byte[8000];
		

		byte[] header;
		//TES3 header is different
		if(inputArchive.getSig() != SIG.TES3 || true) {		
			header = new byte[36];
			//this is the word "BSA\0" compare with BTDX
			header [0] = 66;//(byte)"B".toCharArray()[0]; perhaps?
			header [1] = 83;
			header [2] = 65;
			setInteger(104, header, 4);// 104 is FO3 and TES5, 103 is TES4
			setInteger(36, header, 8);
			setInteger(archiveFlags, header, 12);
			setInteger(folderCount, header, 16);
			setInteger(fileCount, header, 20);
			setInteger(folderNamesLength, header, 24);
			setInteger(fileNamesLength, header, 28);
			setInteger(fileFlags, header, 32);
			
		} else {
			//TES3 == 256
			header = new byte[12];
			setInteger(256, header, 0);
			//tODO: need to write these 2 styles and the rest as well
			//int hashtableOffset = getInteger(header, 4);
			//fileCount = getInteger(header, 8);
			
		}
		out.write(header);
 
		long fileOffset = header.length + folderCount * 16 + fileNamesLength;
		if (fileOffset > 0x7fffffffL) {
			throw new DBException("File offset exceeds 2GB");
		}

		for (Folder folder : folders) {
			setLong(folder.getHashCode().getHash(), buffer, 0);
			setInteger(folder.getFileCount(), buffer, 8);
			setInteger((int)fileOffset, buffer, 12);
			out.write(buffer, 0, 16);
			fileOffset += folder.getName().length() + 2 + folder.getFileCount() * 16;
			if (fileOffset > 0x7fffffffL) {
				throw new DBException("File offset exceeds 2GB");
			}
		}

		int fileIndex = 0;
		for (Folder folder : folders) {
			String folderName = folder.getName();
			byte[] nameBuffer = folderName.getBytes();
			if (nameBuffer.length != folderName.length()) {
				throw new DBException("Encoded folder name is longer than character name");
			}
			buffer [0] = (byte)(nameBuffer.length + 1);
			System.arraycopy(nameBuffer, 0, buffer, 1, nameBuffer.length);
			buffer [nameBuffer.length + 1] = 0;
			out.write(buffer, 0, nameBuffer.length + 2);

			for (int i = 0; i < folder.getFileCount(); i++) {
				ArchiveEntry entry = entries.get(fileIndex++);
				setLong(entry.getFileHashCode().getHash(), buffer, 0);
				setInteger(0, buffer, 8);
				setInteger(0, buffer, 12);
				out.write(buffer, 0, 16);
			}
		}

		for (ArchiveEntry entry : entries) {
			String fileName = entry.getFileName();
			byte[] nameBuffer = fileName.getBytes();
			if (nameBuffer.length != fileName.length()) {
				throw new DBException("Encoded file name is longer than character name");
			}
			System.arraycopy(nameBuffer, 0, buffer, 0, nameBuffer.length);
			buffer [nameBuffer.length] = 0;
			out.write(buffer, 0, nameBuffer.length + 1);
		}

		int currentProgress = 0;
		fileIndex = 0;

		for (ArchiveEntry entry : entries) {
			InputStream in = null;
			Deflater deflater = null;

			try {
				// notice we required the loader to have keep the displayable version which holds the folder name per entry
				//((DisplayableArchiveEntry) entry).getFolderName();
				/*File file = new File(inputArchive.getPath() + "\\" + entry.getFileName()); 
				int residualLength = (int)file.length();
				entry.setFileOffset(out.getFilePointer());
				entry.setFileLength(residualLength);
				in = new FileInputStream(file);*/
				
				
				in = inputArchive.getInputStream(entry);
				
				// convert to etc2 if needed
				if(CONVERT_DDS_to_KTX && entry.getFileName().endsWith(".ktx")) {
					ByteBuffer bbKtx = convertDDStoKTX(in);
					if(bbKtx != null) {
						in = new ByteBufferBackedInputStream(bbKtx);					
						entry.setFileLength(bbKtx.limit());
					} else {
						System.out.println("Conversion failed for "+ ((DisplayableArchiveEntry) entry).getName());
					}
				}
				
				int residualLength = entry.getFileLength();
				
				//NOTICE entry now configured for output only, input permanently broken
				entry.setFileOffset(out.getFilePointer());

				if ((archiveFlags & 0x100) != 0) {
					byte nameBuffer2[] = entry.getFileName().getBytes();
					buffer [0] = (byte)nameBuffer2.length;
					out.write(buffer, 0, 1);
					out.write(nameBuffer2);
				}

				if ((archiveFlags & 4) != 0) {
					setInteger(residualLength, buffer, 0);
					out.write(buffer, 0, 4);
					int compressedLength = 4;
					if (residualLength > 0) {
						deflater = new Deflater(6);
						while (!deflater.finished()) {
							int count;
							if (deflater.needsInput() && residualLength > 0) {
								int length = Math.min(dataBuffer.length, residualLength);
								count = in.read(dataBuffer, 0, length);
								if (count == -1) {
									throw new EOFException("Unexpected end of stream while deflating data");
								}
								residualLength -= count;
								deflater.setInput(dataBuffer, 0, count);
								if (residualLength == 0)
									deflater.finish();
							}
							count = deflater.deflate(compressedBuffer, 0, compressedBuffer.length);
							if (count > 0) {
								out.write(compressedBuffer, 0, count);
								compressedLength += count;
							}
						}
					}
					entry.setCompressed(true);
					entry.setCompressedLength(compressedLength);
				} else {
					int count;
					for (; residualLength > 0; residualLength -= count) {
						count = in.read(dataBuffer);
						if (count == -1) {
							throw new EOFException("Unexpected end of stream while copying data");
						}
						out.write(dataBuffer, 0, count);
					}

					entry.setCompressed(false);
				}

				if (in != null)
					in.close();
				if (deflater != null)
					deflater.end();
			} catch (IOException e) {
				System.out.println("IOException "+ ((DisplayableArchiveEntry) entry).getName());
				if (in != null)
					in.close();
				if (deflater != null)
					deflater.end();			
				e.printStackTrace();
			}

			int newProgress = (++fileIndex * 100) / fileCount;
			if (newProgress >= currentProgress + 5) {
				currentProgress = newProgress;
			}

		}

		long fileOffset2 = header.length + folderCount * 16;
		out.seek(fileOffset2);
		int entryIndex = 0;
		for (Folder folder : folders) {
			int length = out.readByte() & 0xff;
			out.skipBytes(length);

			for (int i = 0; i < folder.getFileCount(); i++) {
				ArchiveEntry entry = entries.get(entryIndex++);
				int count;
				if ((archiveFlags & 0x100) != 0) {
					count = entry.getFileName().getBytes().length + 1;
				} else {
					count = 0;
				}

				if (entry.isCompressed()) {
					count += entry.getCompressedLength();
				} else {
					count += entry.getFileLength();
				}

				setInteger(count, buffer, 0);
				fileOffset2 = entry.getFileOffset();
				if (fileOffset2 > 0x7fffffffL) {
					throw new DBException("File offset exceeds 2GB");
				}
				setInteger((int)fileOffset2, buffer, 4);
				out.skipBytes(8);
				out.write(buffer, 0, 8);
			}
		}
	}

	private static void setInteger(int number, byte buffer[], int offset) {
		buffer [offset] = (byte)number;
		buffer [offset + 1] = (byte)(number >>> 8);
		buffer [offset + 2] = (byte)(number >>> 16);
		buffer [offset + 3] = (byte)(number >>> 24);
	}

	private static void setLong(long number, byte buffer[], int offset) {
		buffer [offset] = (byte)(int)number;
		buffer [offset + 1] = (byte)(int)(number >>> 8);
		buffer [offset + 2] = (byte)(int)(number >>> 16);
		buffer [offset + 3] = (byte)(int)(number >>> 24);
		buffer [offset + 4] = (byte)(int)(number >>> 32);
		buffer [offset + 5] = (byte)(int)(number >>> 40);
		buffer [offset + 6] = (byte)(int)(number >>> 48);
		buffer [offset + 7] = (byte)(int)(number >>> 56);
	}

	private static class Folder {
		private String		name;

		private HashCode	hashCode;

		private int			fileCount2;

		public Folder(String name) {
			this.name = name;
			hashCode = new HashCode(name, true);
		}

		public String getName() {
			return name;
		}

		public HashCode getHashCode() {
			return hashCode;
		}

		public void incrementFileCount() {
			fileCount2++;
		}

		public int getFileCount() {
			return fileCount2;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj != null && (obj instanceof Folder) && hashCode.equals(((Folder)obj).getHashCode()));
		}

	}
	
	/**
	 * Take an input stream in and returns a bytebuffer back out in ktx format
	 * @param inputStream
	 * @return
	 */
	public static ByteBuffer convertDDStoKTX(InputStream inputStream) throws IOException {

		DDSImage ddsImage = DDSImage.read(CompressedTextureLoader.toByteBuffer(inputStream));

		if (ddsImage != null) {
			DDSDecompressor decomp = new DDSDecompressor(ddsImage, 0, null);
			NioImageBuffer decompressedImage = decomp.convertImageNio();
			if(decompressedImage != null) { 
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
						// copy RGB 3 sets out then 1 sets of alpha 
						img = new byte[(bb.capacity() / 4) * 3];
						imgalpha = new byte[(bb.capacity() / 4)];
						for (int i = 0; i < img.length / 3; i++) {
							img[i * 3 + 0] = bb.get();
							img[i * 3 + 1] = bb.get();
							img[i * 3 + 2] = bb.get();
							imgalpha[i] = bb.get();
						}
					} else if (decompressedImage.getImageType() == NioImageBuffer.ImageType.TYPE_BYTE_GRAY) {
						// copy RGB from the 1 byte of L8 data and use RGB (FORMAT.ETC2PACKAGE_R is odd 16 bit thing)
						img = new byte[bb.capacity() * 3];
						for (int i = 0; i < img.length / 3; i++) {
							byte byt = bb.get();
							img[i * 3 + 0] = byt;
							img[i * 3 + 1] = byt;
							img[i * 3 + 2] = byt;
						}
					} else {
						System.err.println("Bad Image Type " + decompressedImage.getImageType());
						return null;
					}
	
					int fmt = ddsImage.getPixelFormat();
					FORMAT format = null;
	
					 if (fmt == DDSImage.D3DFMT_DXT1) {
						if (!decomp.decompressedIsOpaque()) {
							format = FORMAT.ETC2PACKAGE_RGBA1;
						} else {
							format = FORMAT.ETC2PACKAGE_RGB;
						}
					} else if (fmt == DDSImage.D3DFMT_DXT2	|| fmt == DDSImage.D3DFMT_DXT3 || fmt == DDSImage.D3DFMT_DXT4
								|| fmt == DDSImage.D3DFMT_DXT5) {
						if (!decomp.decompressedIsOpaque()) {
							format = FORMAT.ETC2PACKAGE_RGBA;
						} else {
							format = FORMAT.ETC2PACKAGE_RGB;
						}
					}
					//uncompressed options 
					else if (fmt == DDSImage.D3DFMT_R8G8B8 || fmt == DDSImage.D3DFMT_R5G6B5) {
						format = FORMAT.ETC2PACKAGE_RGB;
					} else if (fmt == DDSImage.D3DFMT_L8) {
						format = FORMAT.ETC2PACKAGE_RGB;
					} else if (fmt == DDSImage.D3DFMT_A8R8G8B8	|| fmt == DDSImage.D3DFMT_A4R4G4B4
								|| fmt == DDSImage.D3DFMT_X8R8G8B8) {
						format = FORMAT.ETC2PACKAGE_RGBA;
					} else if (fmt == DDSImage.D3DFMT_A8L8) {
						format = FORMAT.ETC2PACKAGE_RGBA;
					}
					 
					// perhaps normal maps (_n) should be forcibly set to RGB ( not sRGBA)??
					//if(filename.indexOf("_n.dds") > 0)
					//	format = FORMAT.ETC2PACKAGE_RGB;
	
					if (format != null) {
						QuickETC ep = new QuickETC();
						//Note mipmaps on to use on a GPU!
						ByteBuffer ktxBB = ep.compressImageToByteBuffer(img, imgalpha, ddsImage.getWidth(),
								ddsImage.getHeight(), format, true);

						return ktxBB;
					} else {
						System.out.println("format != null not worked out properly! fmt= " + fmt);
					}
				} else {
					System.out.println("decompressedImage.getDataBuffer() not a ByteBuffer");
				}
			} else {
				System.out.println("decompressedImage  == null ");
			}
		} else { 
			System.out.println("ddsImage == null");
		}

		return null;
	}
	public class ByteBufferBackedInputStream extends InputStream {

	    ByteBuffer buf;

	    public ByteBufferBackedInputStream(ByteBuffer buf) {
	        this.buf = buf;
	    }

	    public int read() throws IOException {
	        if (!buf.hasRemaining()) {
	            return -1;
	        }
	        return buf.get() & 0xFF;
	    }

	    public int read(byte[] bytes, int off, int len)
	            throws IOException {
	        if (!buf.hasRemaining()) {
	            return -1;
	        }

	        len = Math.min(len, buf.remaining());
	        buf.get(bytes, off, len);
	        return len;
	    }
	}
}