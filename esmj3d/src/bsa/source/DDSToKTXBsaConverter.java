package bsa.source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.Deflater;

import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import bsaio.ArchiveFile.SIG;
import bsaio.DBException;
import bsaio.HashCode;
import bsaio.displayables.Displayable;
import texture.DDSToKTXConverter;
import tools.io.FileChannelRAF;

/**
 * This is primarily a dds to ktx archive converter, but it is the basis of all bsa source archive create tasks
 */
/* BSA fallout file format is as follows
 
 https://en.uesp.net/wiki/Skyrim_Mod:Archive_File_Format
 
 header = new byte[36]; //this is the word "BSA\0" compare with BTDX 
 header[0] = 66;  //(byte)"B".toCharArray()[0]; perhaps? 
 header[1] = 83; 
 header[2] = 65; 
 setInteger(104, header, 4); // 104 is FO3 and TES5, 103 is TES4
 setInteger(header.length, header, 8); // this is a pointer to folder index, which is 36 or straight after the header
 setInteger(archiveFlags, header, 12); 
 setInteger(folderCount, header, 16); 
 setInteger(fileCount, header, 20);
 setInteger(folderNamesLength, header, 24); //Total length of all folder names, including \0's but not including the prefixed length byte. 
 setInteger(fileNamesLength, header, 28); 
 setInteger(fileFlags, header, 32);
 
 
 --Folder Index 
 filePosition is (header.length) 
 section length is (folderCount * 16)
 
 for each folder 
 	setLong(folder.getHashCode().getHash(), buffer, 0); 
 	setInteger(folder.getFileCount(), buffer, 8);
 	setInteger((int)fileOffset, buffer, 12); //Offset to file records for this folder. (Subtract totalFileNameLength to get the actual offset within the file.)
 
 //fileOffset is the end of the folder, past the file name section section and into the folder heap section below,
 //meaning every folder name len must be kept track of as it's written out
 
 --Folder Heap 
 filePosition is (offset = header.length + (folderCount * 16)); 
 section length is ((folderCount + folderNamesLength) + (fileCount * 16)) long 
 -- folderNamesLength doesn't include the len byte at the front, so add folderCount on to it
 
 for each folder 
 	String folderName = plus a 0 plus an empty char (length + 2)//TODO: what? surely plus a len plus a nul
 
 	section length is folder.getFileCount() * 16  
 	for (int i = 0; i < folder.getFileCount(); i++) { 
		ArchiveEntry entry = entries.get(fileIndex++); 
		setLong(entry.getFileHashCode().getHash(), buffer, 0); //crazy fileNamesLength added on as that's taken off at load time (some sort of history cock up) 
		setInteger(0, buffer, 8); // int of size
 		setInteger(0, buffer, 12); // int location within file heap 
 	}
}
 
--File Names 
filePosition is (offset = header.length + (folderCount * 16) + (folderCount + folderNamesLength) + (fileCount * 16));
section length is fileNamesLength
 
// file names, but oddly just in order with no len to start each one! parsed by looking for nulls 
for (ArchiveEntry entry : entries) { 
	byte[] nameBuffer = entry.getFileName().getBytes(); 
	buffer[nameBuffer.length] = 0;
	out.write(buffer, 0, nameBuffer.length + 1); 
}
 
 
 -- File Heap 
 filePosition is (offset = header.length + (folderCount * 16) + (folderCount + folderNamesLength) + (fileCount * 16) + fileNamesLength) 
 section length is really huge, might have the name repeated
 
 
 for (ArchiveEntry entry : entries) { 
 	if ((archiveFlags & 0x100) != 0) 
 		nameLen 1byte then name bytes[] 
 	size int given	above in the folders file locations 
 	content bytes[]
 
 
*/
public class DDSToKTXBsaConverter extends Thread {

	public static int									NUM_THREADS			= 4;

	private static final boolean						CONVERT_DDS_to_KTX	= true;

	private FileChannel									outputArchiveFile;

	private FileChannel									outputArchiveFileReader;

	private ArchiveFile									inputArchive;

	private StatusUpdateListener						statusDialog;

	private boolean										completed;

	private int											archiveFlags;

	private int											fileFlags;

	private int											folderCount;

	private int											fileCount;

	private int											folderNamesLength;

	private int											fileNamesLength;

	private ArrayList<ArchiveEntry>						entries;

	private ArrayList<Folder>							folders;

	// to support restarts
	private HashMap<ArchiveEntry, ArchiveEntryExtras>	extraInfo;

	int													currentProgress		= 0;
	int													entriesWritten		= 0;

	/**
	 * 
	 * @param outputArchiveFile this is the writable interface, if this is not a partially written file it must have
	 *            been deleted or truncated
	 * @param outputArchiveFileReader this is the readable interface, this will be used to determine if the file has
	 *            been partially written and if so conversion will continue from that point
	 * @param inputArchive this MUST have been loaded as displayable=true so we have the names on entries
	 * @param statusDialog
	 */
	public DDSToKTXBsaConverter(FileChannel outputArchiveFile, FileChannel outputArchiveFileReader,
								ArchiveFile inputArchive, StatusUpdateListener statusDialog) {
		completed = false;
		this.outputArchiveFile = outputArchiveFile;
		this.outputArchiveFileReader = outputArchiveFileReader;
		this.inputArchive = inputArchive;
		this.statusDialog = statusDialog;
	}

	@Override
	public void run() {
		FileChannelRAF out = null;
		try {
			entries = new ArrayList<ArchiveEntry>(256);
			folders = new ArrayList<Folder>(256);
			extraInfo = new HashMap<ArchiveEntry, ArchiveEntryExtras>();

			archiveFlags = 3; // this is  2  and 1 being folders and filenames :  4 is compressed, (0x100==256) is required for names to be written with the file entry

			if (inputArchive.getSig() != SIG.TES3)//tes3 no compression
				archiveFlags |= 4;
			fileFlags = 0;

			List<ArchiveEntry> inEntries = inputArchive.getEntries();
			// notice we require the loader to have keep the displayable version which holds the folder name per entry
			for (ArchiveEntry entry : inEntries) {
				insertEntry(entry);
			}

			if (fileCount != 0) {
				//if not empty then this output File must have a current progress written into the start of it (by this class)
				out = new FileChannelRAF(outputArchiveFile);
				writeArchive(out, outputArchiveFileReader);
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

	private void insertEntry(ArchiveEntry inEntry) throws DBException {
		String folderName = ((Displayable)inEntry).getFolderName();
		
		
		
		//folderName = folderName.substring(baseName.length() + 1);
		if (folderName.length() > 254) {
			throw new DBException("Maximum folder path length is 254 characters");
		}

		if (CONVERT_DDS_to_KTX && ((Displayable)inEntry).getFileName().endsWith(".dds")) {
			((Displayable)inEntry).setFileName(((Displayable)inEntry).getFileName().replace(".dds", ".ktx"));
		}

		String fileName = ((Displayable)inEntry).getName();
		if (fileName.length() > 254) {
			throw new DBException("Maximum file name length is 254 characters");
		}

		boolean insert = true;

		int count = entries.size();
		int i = 0;
		while (i < count) {
			ArchiveEntry listEntry = entries.get(i);
			int diff = inEntry.compareTo(listEntry);
			if (diff == 0) {
				throw new DBException("Hash collision: '"	+ ((Displayable)inEntry).getName() + "' and '"
										+ ((Displayable)listEntry).getName() + "'");
			}
			if (diff < 0) {
				insert = false;
				entries.add(i, inEntry);
				break;
			}
			i++;
		}

		if (insert) {
			entries.add(inEntry);
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
				if (inputArchive.getSig() != SIG.TES3)//tes3 all sorts in the file
					archiveFlags &= -5;
			} else if (ext.equals(".ogg")) {
				fileFlags |= 0x10;
				if (inputArchive.getSig() != SIG.TES3)//tes3 all sorts in the file
					archiveFlags &= -5;
			} else if (ext.equals(".xml")) {
				fileFlags |= 0x100;
			}
		}

		// FO3 DLC don't conform to this requirement
		//if ((inputArchive.getSig() != SIG.TES3) && ((fileFlags & 2) != 0 && (fileFlags & -3) != 0)) {
		//	throw new DBException("Texture files must be packaged by themselves");
		//}
		
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

	// this is a memebr variable which makes the writing to the file very single thread
	private long pos = 0;
	/**
	 * Note this writes out a ArchiveFileBsa version of archive files, not tes3, Btdx or starfields one
	 * @param out
	 * @throws DBException
	 * @throws IOException
	 */
	private void writeArchive(FileChannelRAF out2, FileChannel outReader) throws DBException, IOException {

		// First things first, let's see if the outputs readable channel has a marker for partial completion and if start from that point		
		// partial writes are indicated by an int at 4 and a counted int at 8 (which are completed by setting to 104 and 36, respectively)
		FileChannel ch = out2.getChannel();

		ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);// 2 ints worth
		int bytesCount = outReader.read(byteBuffer, 4);// from the 4th byte
		int peekInComplete = byteBuffer.getInt(0);
		int lastGoodEntryWritten = byteBuffer.getInt(4); // if partial file it's the last known good write, otherwise position of the folder index (36)		

		byte[] buffer = new byte[256];
		byte[] dataBuffer = new byte[32000];
		byte[] compressedBuffer = new byte[8000];

		byte[] header = new byte[36];

		if (peekInComplete != ArchiveFile.PARTIAL_FILE || lastGoodEntryWritten == -1) {
			lastGoodEntryWritten = -1;// clear the value out and ensure we start entry writing at the first entry
			
			//TES3 header is different, but we don't want to use it anyway
			if (inputArchive.getSig() != SIG.TES3 || true) {

				//this is the word "BSA\0" compare with BTDX
				header[0] = 66;//(byte)"B".toCharArray()[0]; perhaps?
				header[1] = 83;
				header[2] = 65;
				//setInteger(104, header, 4);// 104 is FO3 and TES5, 103 is TES4
				//setInteger(header.length, header, 8); // this is the folder offset, notice it is the length of the header at 36

				setInteger(ArchiveFile.PARTIAL_FILE, header, 4);// mark as incomplete so far, will be completed at the end of this method		
				setInteger(-1, header, 8); // -1 means put the folders back and start again

				setInteger(archiveFlags, header, 12);
				setInteger(folderCount, header, 16);
				setInteger(fileCount, header, 20);
				setInteger(folderNamesLength, header, 24);
				setInteger(fileNamesLength, header, 28);
				setInteger(fileFlags, header, 32);

			} else {
				// this is a bugger of a format best not to bother with it for this purpose
				//TES3 == 256
				header = new byte[12];
				setInteger(256, header, 0);
				setInteger(header.length, header, 4);
				//TODO: need to write these 2 styles and the rest as well
				//int hashtableOffset = getInteger(header, 4);
				//fileCount = getInteger(header, 8);
			}
			ch.write(ByteBuffer.wrap(header), pos);
			pos += header.length;
			
			// keep track as we write so we can record where folder details live in the folder heap			
			// this offset is now pointing past the folder index, into the folder heap
			long fileOffset = header.length + folderCount * 16;
			if (fileOffset > 0x7fffffffL) {
				throw new DBException("File offset exceeds 2GB");
			}

			// folder index
			for (Folder folder : folders) {
				setLong(folder.getHashCode().getHash(), buffer, 0);
				setInteger(folder.getFileCount(), buffer, 8);
				setInteger((int)fileOffset + fileNamesLength, buffer, 12);// notice the crazy oddity of this needing to have fileNamesLength added, loading takes that value off the offset value ahhh!
				ch.write(ByteBuffer.wrap(buffer, 0, 16), pos);
				pos += 16;
				// measure the distance past this folders heap data, for the next folder to point at as its heap start pos
				fileOffset += folder.getName().length() + 2 + folder.getFileCount() * 16;
				if (fileOffset > 0x7fffffffL) {
					throw new DBException("File offset exceeds 2GB");
				}
			}

			// folder heap, a variable len name (+2), then for each file (folders fileCount from the index list) 16 bytes of hash, len, and pos 
			int fileIndex = 0;
			for (Folder folder : folders) {
				String folderName = folder.getName();
				byte[] nameBuffer = folderName.getBytes();
				if (nameBuffer.length != folderName.length()) {
					throw new DBException("Encoded folder name is longer than character name");
				}
				buffer[0] = (byte)(nameBuffer.length + 1); // 1 byte len
				System.arraycopy(nameBuffer, 0, buffer, 1, nameBuffer.length);
				buffer[nameBuffer.length + 1] = 0; // null at end
				ch.write(ByteBuffer.wrap(buffer, 0, nameBuffer.length + 2), pos);// 1byte len to start and null at end
				pos += nameBuffer.length + 2;
				for (int i = 0; i < folder.getFileCount(); i++) {
					ArchiveEntry entry = entries.get(fileIndex++);

					// In order to easily update these 2 numbers when the actual file content is written 
					// we record this file info pointer for use later
					extraInfo.put(entry, new ArchiveEntryExtras(pos));

					setLong(entry.getFileHashCode().getHash(), buffer, 0);
					setInteger(0, buffer, 8); // int of size (unknown for now)
					setInteger(0, buffer, 12); // int location (unknown for now)					
					ch.write(ByteBuffer.wrap(buffer, 0, 16), pos);
					pos += 16;
				}
			}

			// file names, but oddly just in order with no len to start each one!
			for (ArchiveEntry entry : entries) {
				String fileName = ((Displayable)entry).getFileName();
				byte[] nameBuffer = fileName.getBytes();
				if (nameBuffer.length != fileName.length()) {
					throw new DBException("Encoded file name is longer than character name");
				}
				System.arraycopy(nameBuffer, 0, buffer, 0, nameBuffer.length);
				buffer[nameBuffer.length] = 0;// put a null at the end, notice no len at start
				ch.write(ByteBuffer.wrap(buffer, 0, nameBuffer.length + 1), pos);
				pos += nameBuffer.length + 1;
			}

			//pos is now pointing at the start of the file heap

		} else {
			// jump forward with no writes but spot the file header positions on the way			

			long writeStartPos = 0;

			// folders count *  (hash of 8, bytecount of 4, and offset of 4 ) (past the file index section)
			long fileOffset = header.length + folderCount * 16;

			int fileIndex = 0;
			for (Folder folder : folders) {
				fileOffset += folder.getName().getBytes().length + 2;// 1byte len to start and null at end

				// must go through to get the extra info updated
				for (int i = 0; i < folder.getFileCount(); i++) {
					ArchiveEntry entry = entries.get(fileIndex);
					// In order to easily update these 2 numbers when the new content is written and confirmed good 
					// we record this file info pointer for use later
					extraInfo.put(entry, new ArchiveEntryExtras(fileOffset));

					// is this the last good written file? if so grab the location of the point just after it
					if (fileIndex == lastGoodEntryWritten) {
						byteBuffer.rewind();
						bytesCount = outReader.read(byteBuffer, fileOffset + 8);// absolute read and skip hash 8 bytes
						int fileSize = byteBuffer.getInt(0);
						int offset = byteBuffer.getInt(4); 					 

						writeStartPos = fileSize + offset;
					}

					fileOffset += 16; //hash of 8, bytecount of 4, and offset of 4 (into file heap section)	

					fileIndex++;
				}
			}

			// file names, but oddly just in order with no len to start each one!
			fileOffset += fileNamesLength;

			// in case of trouble just start again
			if (writeStartPos <= 0) {
				// flag away skipping any thing
				lastGoodEntryWritten = -1;				
				pos = fileOffset;// this is now pointing to the start of the heap
				System.out.println("Couldn't find a good writeStartPos, restarting. lastGoodEntryWritten = "
									+ lastGoodEntryWritten);
			} else {
				System.out.println("jumping forward with incomplete set to "	+ lastGoodEntryWritten + " file pos="
									+ writeStartPos);
				pos = writeStartPos;
			}

		}

		// Multi-threaded below, very non linear

		Deflater deflater = new Deflater(6);

		ExecutorService es = Executors.newFixedThreadPool(NUM_THREADS);
		List<Callable<Object>> todo = new ArrayList<Callable<Object>>();

		// give it nulls so we can put things in it in order
		ArrayList<ArchiveEntryOutput> entriesToWrite = new ArrayList<ArchiveEntryOutput>();
		for (int i = 0; i < NUM_THREADS * 2; i++) {
			entriesToWrite.add(null);
		}
		ArrayList<ArchiveEntryOutput> entriesToWriteNow = new ArrayList<ArchiveEntryOutput>();
		
		// create a single writer, called in the loop below to write out the prepped batch of entries
		Runnable entryWriter = new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < entriesToWriteNow.size(); i++) {
					ArchiveEntryOutput aeo = entriesToWriteNow.get(i);
					if (aeo != null) {
						InputStream in = null;
						ArchiveEntry entry = aeo.entry;
						try {
							//System.out.println("entry to write " + entry.getFileName());
							in = aeo.in;

							int residualLength = entry.getFileLength();

							long fileOffsetStart = pos;

							if ((archiveFlags & 0x100) != 0) {
								byte nameBuffer2[] = ((Displayable)entry).getFileName().getBytes();
								buffer[0] = (byte)nameBuffer2.length;
								ch.write(ByteBuffer.wrap(buffer, 0, 1), pos);
								pos += 1;
								ch.write(ByteBuffer.wrap(nameBuffer2), pos);
								pos += nameBuffer2.length;
								
							}

							//Note either whole archive compressed or not, this is not per entry
							if ((archiveFlags & 4) != 0) {
								setInteger(residualLength, buffer, 0);
								ch.write(ByteBuffer.wrap(buffer, 0, 4), pos);
								pos += 4;
								int compressedLength = 4;
								if (residualLength > 0) {

									while (!deflater.finished()) {
										int count;
										if (deflater.needsInput() && residualLength > 0) {
											int length = Math.min(dataBuffer.length, residualLength);
											count = in.read(dataBuffer, 0, length);
											if (count == -1) {
												throw new EOFException(
														"Unexpected end of stream while deflating data");
											}
											residualLength -= count;
											deflater.setInput(dataBuffer, 0, count);
											if (residualLength == 0)
												deflater.finish();
										}
										count = deflater.deflate(compressedBuffer, 0, compressedBuffer.length);
										if (count > 0) {
											ch.write(ByteBuffer.wrap(compressedBuffer, 0, count), pos);
											pos += count;
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
									ch.write(ByteBuffer.wrap(dataBuffer, 0, count), pos);
									pos += count;
								}

								entry.setCompressed(false);
							}

							// now we jump back to teh file index and set the len and pos ints
							int byteLen;
							if ((archiveFlags & 0x100) != 0) {
								byteLen = ((Displayable)entry).getFileName().getBytes().length + 1;
							} else {
								byteLen = 0;
							}

							if (entry.isCompressed()) {
								byteLen += entry.getCompressedLength();
							} else {
								byteLen += entry.getFileLength();
							}
						 
							// update the file index with the len and pos data we now know
							setInteger(byteLen, buffer, 0);
							setInteger((int)fileOffsetStart, buffer, 4);
							long indexPosition = extraInfo.get(entry).entryHeaderFilePos;
							//System.out.println("byteLen " + byteLen);
							//System.out.println("fileOffsetStart " + fileOffsetStart);										
							//System.out.println("indexPosition " + indexPosition);																		
							indexPosition += 8; // this is the file hash skipped
							// this is int size and int location
							ch.write(ByteBuffer.wrap(buffer, 0, 8), indexPosition);// doesn't touch pos

							if (in != null)
								in.close();
							if (deflater != null)
								deflater.reset();
						} catch (IOException e) {
							System.out.println("IOException " + ((Displayable)entry).getName());
							try {
								if (in != null)
									in.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (deflater != null)
								deflater.reset();
							e.printStackTrace();
						}

						int newProgress = (++entriesWritten * 100) / fileCount;
						if (newProgress >= currentProgress + 1) {
							currentProgress = newProgress;
							//System.out.println("Conversion Progress " + currentProgress);
							if (statusDialog != null)
								statusDialog.updateProgress(currentProgress);
						}
					}
				}

				try {
					// record at the header how far we've written successfully
					// notice index of last written is 1 less than count of written
					setInteger(entriesWritten - 1, buffer, 0);
					//System.out.println("Written count into second int " + entriesWritten);
					ch.write(ByteBuffer.wrap(buffer, 0, 4), 8);// doesn't touch pos
				} catch (IOException e) {
					e.printStackTrace();
				}

				entriesToWriteNow.clear();
				//System.out.println("entriesToWriteNow.clear()");
			}
		};
		
		

		int writeOrder = 0;
		//lastGoodEntryWritten will be -1 (if this is not a restart)
		entriesWritten = lastGoodEntryWritten + 1;
		for (int entryIdx = entriesWritten; entryIdx < entries.size(); entryIdx++) {

			final ArchiveEntry entryToProcess = entries.get(entryIdx);
			final int order = writeOrder++;
			//System.out.println("entry to process " + entryToProcess.getFileName());	
			
			todo.add(Executors.callable(new Runnable() {
				@Override
				public void run() {
					//System.out.println("considering " + ((DisplayableArchiveEntry)entryToProcess).getName());

					ArchiveEntryOutput aeo = new ArchiveEntryOutput();
					InputStream in = null;

					// notice we required the loader to have kept the displayable version which holds the folder name per entry
					try {
						in = inputArchive.getInputStream(entryToProcess);

						// convert to etc2 if needed
						if (CONVERT_DDS_to_KTX && ((Displayable)entryToProcess).getFileName().endsWith(".ktx")) {
							//System.out.println("converting  " +((DisplayableArchiveEntry)entryToProcess).getName());
							ByteBuffer bbKtx = DDSToKTXConverter.convertDDSToKTX(in,
									((Displayable)entryToProcess).getName());
							if (bbKtx != null) {
								in = new ByteBufferBackedInputStream(bbKtx);
								entryToProcess.setFileLength(bbKtx.limit());
							} else {
								System.out.println(
										"Conversion failed for " + ((Displayable)entryToProcess).getName());
							}
						} else {
							aeo.in = in;
						}

						aeo.entry = entryToProcess;
						aeo.in = in;

						// put it in in order as we are a multi-thread op
						entriesToWrite.set(order, aeo);
						//System.out.println("adding entry to list now " + ((DisplayableArchiveEntry)aeo.entry).getName() + " at " + order);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}));
			
		 

			// 0 base makes this odd, e.g. num thread 4 will mean "mod % 4 == 3" means 3,7,11...
			if ((entryIdx % NUM_THREADS == (NUM_THREADS - 1)) || entryIdx == entries.size() - 1) {
				//System.out.println("entryIdx " + entryIdx + " about to call invokeAll");

				// before executing the batch of converters add a task to the end to write out the previous converted batch
				// 2 lists so writing and processing are not interwoven
				entriesToWriteNow.addAll(entriesToWrite);
				// must keep them as null so the order of writing can be maintained
				// System.out.println("entriesToWrite set back to nulls");
				for (int i = 0; i < NUM_THREADS * 2; i++) {
					entriesToWrite.set(i, null);
				}
				todo.add(Executors.callable(entryWriter));
				try {
					List<Future<Object>> answers = es.invokeAll(todo);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				writeOrder = 0;// reset for the next round
				
				todo.clear();

			}

		}
		
		// one final run through to add the last entries prepared
		entriesToWriteNow.addAll(entriesToWrite);
		todo.add(Executors.callable(entryWriter));
		try {
			List<Future<Object>> answers = es.invokeAll(todo);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		es.shutdown();
		if (deflater != null)
			deflater.end();

		setInteger(104, buffer, 0);
		setInteger(header.length, buffer, 4);
		ch.write(ByteBuffer.wrap(buffer, 0, 8), 4);// doesn't touch pos2
	}
	
	

	private class ArchiveEntryOutput {
		ArchiveEntry	entry;
		InputStream		in;		// this guy gets swapped about during conversion		
	}

	// these are just little endian bytes
	private static void setInteger(int number, byte buffer[], int offset) {
		buffer[offset] = (byte)number;
		buffer[offset + 1] = (byte)(number >>> 8);
		buffer[offset + 2] = (byte)(number >>> 16);
		buffer[offset + 3] = (byte)(number >>> 24);
	}

	private static void setLong(long number, byte buffer[], int offset) {
		buffer[offset] = (byte)(int)number;
		buffer[offset + 1] = (byte)(int)(number >>> 8);
		buffer[offset + 2] = (byte)(int)(number >>> 16);
		buffer[offset + 3] = (byte)(int)(number >>> 24);
		buffer[offset + 4] = (byte)(int)(number >>> 32);
		buffer[offset + 5] = (byte)(int)(number >>> 40);
		buffer[offset + 6] = (byte)(int)(number >>> 48);
		buffer[offset + 7] = (byte)(int)(number >>> 56);
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

	public class ByteBufferBackedInputStream extends InputStream {

		ByteBuffer buf;

		public ByteBufferBackedInputStream(ByteBuffer buf) {
			this.buf = buf;
		}

		@Override
		public int read() throws IOException {
			if (!buf.hasRemaining()) {
				return -1;
			}
			return buf.get() & 0xFF;
		}

		@Override
		public int read(byte[] bytes, int off, int len) throws IOException {
			if (!buf.hasRemaining()) {
				return -1;
			}

			len = Math.min(len, buf.remaining());
			buf.get(bytes, off, len);
			return len;
		}
	}

	public interface StatusUpdateListener {
		public void updateProgress(int currentProgress);
	}

	private class ArchiveEntryExtras {
		// used to point back to the entries index so we can quickly set the byte count and offset position in the file heap
		public long entryHeaderFilePos = -1;

		public ArchiveEntryExtras(long entryHeaderFilePos) {
			this.entryHeaderFilePos = entryHeaderFilePos;
		}
	}
}