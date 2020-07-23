package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TapeStreamer {
	
	private static final Logger logger = LoggerFactory.getLogger(TapeStreamer.class);
	
	public static boolean stream(List<String> commandList, int bufferSize, int skipByteCount, String filePathNameWeNeed, String destinationPath, Checksumtype checksumtype, HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {

		boolean success = true;

		ProcessBuilder pb = new ProcessBuilder(commandList);
		Process proc = pb.start();
		logger.trace("process started...");
		InputStream is = new BufferedInputStream(proc.getInputStream(), bufferSize);

		byte[] chopchunk = new byte[skipByteCount];
		logger.trace("skipped " + is.read(chopchunk, 0, chopchunk.length) + " bytes");

		TarArchiveInputStream tin = null;
		try {
			tin = new TarArchiveInputStream(is);
			TarArchiveEntry entry = getNextTarEntry(tin);
			while (entry != null) {
				String entryPathName = entry.getName();
				// we position to the first files right header already, so if the entry name
				// doesnt match the folder path that means these are the tail part of the
				// restored bytechunk which are not needed...
				if (filePathNameWeNeed != null && !entryPathName.startsWith(filePathNameWeNeed)) {
					logger.trace("possibly all folder content completed...");
					break; // if the file we need is not what we want we break
				}

				// TODO verify this
				if (!entry.isDirectory()) {
					if(checksumtype != null) {
						
						byte[] originalChecksum = filePathNameToChecksumObj.get(entryPathName);
						logger.trace("originalChecksum " + Hex.encodeHexString(originalChecksum));
						byte[] checksumToBeVerified = Md5Util.getChecksum(tin, checksumtype, bufferSize);
						logger.trace("checksumToBeVerified " + Hex.encodeHexString(checksumToBeVerified));
						
						if(Arrays.equals(originalChecksum, checksumToBeVerified)) {
							logger.trace("originalChecksum = checksumToBeVerified. All good");	
						}
						else {
							success = false;
							logger.error("checksum mismatch " + entryPathName);
						}

					}else {
						restoreEntry(tin, bufferSize, destinationPath, entryPathName);
					}
				}

				entry = getNextTarEntry(tin);
			}
			logger.trace("no more entries...");
		} finally {
			if (tin != null)
				tin.close();
			logger.trace("is proc alive : " + proc.isAlive());

			logger.trace("exit : " + proc.exitValue());

			proc.destroy();
		}
		return success;
	}

	private static TarArchiveEntry getNextTarEntry(TarArchiveInputStream tin) {
		TarArchiveEntry entry = null;
		try {
			entry = tin.getNextTarEntry();
			logger.trace("entry found...");
		} catch (Exception e) {
			logger.error("Most probably a Truncated TAR archive exception", e);
		}
		return entry;
	}

	private static void restoreEntry(TarArchiveInputStream tin, int bufferSize, String destinationPath, String entryPathName)
			throws Exception {
			File curfile = new File(destinationPath, entryPathName);
			File parent = curfile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(curfile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, bufferSize);
			IOUtils.copy(tin, bos);
			if (bos != null)
				bos.close();
	}
}
