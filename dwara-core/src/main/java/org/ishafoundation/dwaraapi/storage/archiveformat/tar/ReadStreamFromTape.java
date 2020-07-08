package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;

public class ReadStreamFromTape {

	// 3 usecases
	// file - no need to loop

	// directory - need to loop all files, subfolder etc., - Do we need to validate
	// the no. of blocks we read from tape ? No - as we just restore the needed
	// block count as the size of the file we want

	// verify full archve - checksum - adler - md5 hash - merkel - data - stram -
	// checksum

	private static int bufferSize = 524288;

	public static void decompress(String action, int noOfTapeBlocksToBeRead, int skipByteCount,
			String filenamepathWeNeed, String option) throws IOException {

		File out = new File("/data/ingested/tar-poc/output");

		String restoreCommand = "dd if=/dev/tape/by-id/scsi-1IBM_ULT3580-TD5_1497199456-nst bs=" + bufferSize
				+ " count=" + noOfTapeBlocksToBeRead;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add(restoreCommand);

		ProcessBuilder pb = new ProcessBuilder(commandList);
		Process proc = pb.start();
		System.out.println("process started...");
		InputStream is = new BufferedInputStream(proc.getInputStream(), bufferSize);

		byte[] chopchunk = new byte[skipByteCount];
		System.out.println("skipped " + is.read(chopchunk, 0, chopchunk.length) + " bytes");

		TarArchiveInputStream tin = null;
		try {
			//tin = new TarArchiveInputStream(is, 524288, 512);
			tin = new TarArchiveInputStream(is);
			TarArchiveEntry entry = getNextTarEntry(tin);
			if (action.equals("verify") || action.equals("directory")) {
				System.out.println("directory...");
				while (entry != null) {
					// we position to the first files right header already, so if the entry name
					// doesnt match the folder path that means these are the tail part of the
					// restored bytechunk which are not needed...
					if (filenamepathWeNeed != null && !entry.getName().startsWith(filenamepathWeNeed)) {
						System.out.println("possibly all folder content completed...");
						break; // if the file we need is not the directory we break it.
					}

					// TODO verify this
					if (!entry.isDirectory()) {
						processEntry(action, tin, entry, out, option);
					}

					entry = getNextTarEntry(tin);
				}
				System.out.println("no more entries...");
			} else {
				System.out.println("single file...");
				if (entry.getName().equals(filenamepathWeNeed))
					processEntry(action, tin, entry, out, option);
				else
					System.err.println("not the right file...");
			}
		} finally {
			if (tin != null)
				tin.close();
			System.out.println("is proc alive : " + proc.isAlive());

			System.out.println("exit : " + proc.exitValue());

			proc.destroy();
		}

	}

	public static TarArchiveEntry getNextTarEntry(TarArchiveInputStream tin) {
		TarArchiveEntry entry = null;
		try {
			entry = tin.getNextTarEntry();
			System.out.println("entry found...");
		} catch (Exception e) {
			System.err.println("Most probably a Truncated TAR archive exception");
			e.printStackTrace();
		}
		return entry;
	}

	public static void processEntry(String action, TarArchiveInputStream tin, TarArchiveEntry entry, File out, String option)
			throws IOException {

		System.out.println("on target...");

		File curfile = new File(out, entry.getName());

		BufferedOutputStream bos = null;
		if (!action.equals("verify")) {

			File parent = curfile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(curfile);
			bos = new BufferedOutputStream(fos, bufferSize);
		}

		long chksum = 0;
		if(option.equals("bit"))
			chksum = readBitByBit(action, tin, bos);
		else if(option.equals("avl"))
			chksum = readAsBufferedUsingAvailable(action, tin, bos);
		else if(option.equals("siz")) {
			//IOUtils.copy(tin, bos);
			chksum = copy(tin, bos); // mimick of IOUtils.copy but has the crc calculated and the "verify" call write avoids.
		}
		if (bos != null)
			bos.close();

		System.out.println("read completed...");
		System.out.println("output file..." + curfile);
		
		System.out.println("file size  .. " + FileUtils.sizeOf(curfile));
		File srcfile = new File("/data/user/pgurumurthy/ingest/pub-video/forAravindhAnna/1G-5G", entry.getName());
//		File srcfile = new File("C:\\data\\ingested", entry.getName());
//		File srcfile = new File("C:\\Users\\prakash\\tar-poc\\testextract~", entry.getName());
//		File srcfile = new File("C:\\Users\\prakash\\tar-poc\\extract_buffered_test", entry.getName());
		System.out.println("src file size  .. " + FileUtils.sizeOf(srcfile));
		
		System.out.println("chksum.. " + chksum);
		System.out.println("src file chksum  .. " + getCrc(srcfile));
		System.out.println("----------");
	}

	private static long readBitByBit(String action, TarArchiveInputStream tin, BufferedOutputStream bos)
			throws IOException {
		CRC32 crc = new CRC32();
		// We are reading bit by bit - Pretty slow not very efficient...
		int byte_;
		while ((byte_ = tin.read()) != -1) {
			crc.update(byte_);
			if (!action.equals("verify"))
				bos.write(byte_);
		}
		return crc.getValue();
	}

	private static long readAsBufferedUsingAvailable(String action, InputStream tin, OutputStream bos)
			throws IOException {
		CRC32 crc = new CRC32();
		int available;
		while ((available = tin.available()) > 0) {
			int buffersize = bufferSize;
			if (available < buffersize)
				buffersize = available;

			byte[] content = new byte[buffersize];
			tin.read(content);
			crc.update(content);
			if (!action.equals("verify"))
				bos.write(content);
		}
		return crc.getValue();
	}

	
	// mimick of IOUtils.copy but with 2 difference
	// 1) but has the crc calculated and 
	// 2) the write is avoided for "verify" calls
	private static long copy(InputStream tin, OutputStream bos) throws IOException {
		CRC32 crc = new CRC32();
		
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count=0;
        while (-1 != (n = tin.read(buffer))) {
        	if(bos != null) {
        		bos.write(buffer, 0, n);
        	}
        	/*
        	// Try SHA timer for
        	110 GB 
        	
        	//writing to disk - 2mts 
        	Restoring from tape - 7 mts - Reading and writing
        	SHA 256 - 4 mts
        	
        	
        	700 MB
        	
        	100 - 1 mt
        		- 4/7 mts = 40 secs
        		
        	100 - 
        	
        	rewrite and migrate confluence documentations with note on archiveformat.restore_verify = true
        	jobs involved, etc 
        	
        	// Find the optimum buffer size to be used for BufferedI/OStreams
        	
        	// Neeed to document the Interfaces for the user specific ArchiveFormat
        	*/
            crc.update(buffer, 0, n);
            count += n;
        }
        System.out.println(count);
		return crc.getValue();
	}

	private static long readAsBufferedUsingSize(String action, TarArchiveInputStream tin, TarArchiveEntry entry,
			BufferedOutputStream bos, File curfile) throws IOException {
		CRC32 crc = new CRC32();

		// if filesize < buffersize set the byte array size to filesize
		long filesize = entry.getSize();
		System.out.println("entry filesize - " + filesize);
		int buffersize = bufferSize;
		if (filesize < bufferSize)
			buffersize = (int) filesize;
		byte[] content = new byte[buffersize];

		int readBytes;
		long totalBytesRead = 0; // Total bytes read so far
		int neededFullBufferedCnt = 1;
		
//		byte[] chopchunk = new byte[512*3];
//		System.out.println("skipped " + tin.read(chopchunk, 0, chopchunk.length) + " bytes");
		
		if(filesize > 0)
			neededFullBufferedCnt = (int) (filesize/buffersize);
		int loopCnt = 1;
		while ((readBytes = tin.read(content)) > 0) { //
			if(readBytes < buffersize) {
				System.out.println("needed " + neededFullBufferedCnt + "full buffered loops but " + loopCnt + " had just " + readBytes);
			}
			crc.update(content);
			if (!action.equals("verify")) {
				bos.write(content);
				bos.flush();
				try {
					//System.out.println("flushed filesize " + FileUtils.sizeOf(curfile));
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			if(loopCnt > neededFullBufferedCnt) {
				System.out.println("size 0.. breaking");
				break;
			}
			if(loopCnt == neededFullBufferedCnt) {
				totalBytesRead = loopCnt * buffersize;
				System.out.println("totalBytesRead " + totalBytesRead);
				
				long balanceFileSizeToBeRead = filesize - totalBytesRead;
				System.out.println("balanceFileSizeToBeRead " + balanceFileSizeToBeRead);
				
				buffersize = (int) balanceFileSizeToBeRead;
				System.out.println("balanceFileSizeToBeRead < bufferSize " + buffersize);
			}
			/*
			totalBytesRead = totalBytesRead + readBytes;
			System.out.println("totalBytesRead " + totalBytesRead);
			
			long balanceFileSizeToBeRead = filesize - totalBytesRead;
			System.out.println("balanceFileSizeToBeRead " + balanceFileSizeToBeRead);
			
			if(balanceFileSizeToBeRead == 0) {
//				System.out.println(new String("***888***"));
//				System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		//System.out.println(new String(content));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
				System.out.println("size 0.. breaking");
				break;
			}
			else if (balanceFileSizeToBeRead < bufferSize) {
				buffersize = (int) balanceFileSizeToBeRead;
				System.out.println("balanceFileSizeToBeRead < bufferSize " + buffersize);
//				System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		//System.out.println(new String(content));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
//        		System.out.println(new String("*"));
			}
			*/
			content = new byte[buffersize];
			loopCnt++;

		}
		System.out.println("totalBytesRead " + totalBytesRead);
		return crc.getValue();
	}

	
	private static long getCrc(File file) {
		CRC32 crc = new CRC32();
		try {
			crc.update(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			// TODO : Swallow it or cascade to top?
			System.err.println("Unable to generate File crc " + file.getAbsolutePath());
		}
		return crc.getValue();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String action = args[0];
			int noOfTapeBlocksToBeRead = Integer.parseInt(args[1]);
			int skipByteCount = Integer.parseInt(args[2]);
			String filenamepathWeNeed = args[3];
			String option = args[4];
			decompress(action, noOfTapeBlocksToBeRead, skipByteCount, filenamepathWeNeed, option);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
