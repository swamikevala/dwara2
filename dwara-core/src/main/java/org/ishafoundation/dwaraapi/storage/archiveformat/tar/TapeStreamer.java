package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TapeStreamer {

	private static final Logger logger = LoggerFactory.getLogger(TapeStreamer.class);

	public static TapeStreamerResponse stream(List<String> commandList, int bufferSize, int skipByteCount, String filePathNameWeNeed, boolean isFilePathNameWeNeedIsDirectory,
			boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
			HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {

		HashMap<String, Integer> filePathNameToHeaderBlockCnt = new LinkedHashMap<String, Integer>();
		
		TapeStreamerResponse tsr = new TapeStreamerResponse();
		tsr.setSuccess(true);
		tsr.setFilePathNameToHeaderBlockCnt(filePathNameToHeaderBlockCnt);


		ProcessBuilder pb = new ProcessBuilder(commandList);
		Process proc = pb.start();
		logger.trace("process started...");
		InputStream is = new BufferedInputStream(proc.getInputStream(), bufferSize);
		// Dont do anything with errorstream - mbuffer returns broken pipe error - InputStream es = new BufferedInputStream(proc.getErrorStream(), bufferSize);
		byte[] chopchunk = new byte[skipByteCount];
		logger.trace("skipped " + is.read(chopchunk, 0, chopchunk.length) + " bytes");

		TarArchiveInputStream tin = null;
		try {
			tin = new TarArchiveInputStream(is);
			long totalNoOfBytesRead = 0L;
			TarArchiveEntry entry = getNextTarEntry(tin);
			while (entry != null) {
				String entryPathName = entry.getName();
				logger.trace("tar entry - " + entryPathName);
				long headerBlockBytes = tin.getBytesRead() - totalNoOfBytesRead;
				logger.trace("headerBlockBytes - " + headerBlockBytes);
				if(entry.isDirectory())
					entryPathName = FilenameUtils.getPathNoEndSeparator(entryPathName);
				filePathNameToHeaderBlockCnt.put(entryPathName, (int) (headerBlockBytes/512));
				
				// we position to the first files right header already, so if the entry name
				// doesnt match the folder path that means these are the tail part of the
				// restored bytechunk which are not needed...
				if (filePathNameWeNeed != null && !entryPathName.startsWith(filePathNameWeNeed)) { // if filePathNameWeNeed is a directory, get all the files that startwith the requested directory name else break
					logger.trace("possibly all folder content completed...");
					break;
				}

				if (!entry.isDirectory()) {
					if (toBeRestored) {
						File curfile = new File(destinationPath, entryPathName);
						File parent = curfile.getParentFile();
						if (!parent.exists()) {
							parent.mkdirs();
						}
						FileOutputStream fos = null;
						BufferedOutputStream bos = null;

						if (toBeVerified) { // Restore with checksum validation
							fos = new FileOutputStream(curfile);
							bos = new BufferedOutputStream(fos, bufferSize);
							
							byte[] originalChecksum = filePathNameToChecksumObj.get(entryPathName);
							logger.trace("originalChecksum " + Hex.encodeHexString(originalChecksum));
		
							byte[] checksumToBeVerified = ChecksumUtil.restoreFileAndGetChecksum(tin, checksumtype, bufferSize, bos);
							logger.trace("checksumToBeVerified " + Hex.encodeHexString(checksumToBeVerified));
	
							if (Arrays.equals(originalChecksum, checksumToBeVerified)) {
								logger.trace("originalChecksum = checksumToBeVerified. All good");
								logger.info(entryPathName + " restored to " + destinationPath);
							} else {
								tsr.setSuccess(false);
								logger.error("checksum mismatch " + entryPathName);
							}
						}
						else {
							String linkName = entry.getLinkName();
							if(StringUtils.isNotBlank(linkName)) {
								logger.trace("linkName - " + linkName);
								Path linkPath = Paths.get(destinationPath, linkName);
								logger.trace("linkPath - " + linkPath);
								if(entry.isSymbolicLink()) {
									// create a symlink
									Files.createSymbolicLink(curfile.toPath(), Paths.get(linkName));
									logger.info(curfile.toPath() + " sym linked to --> " + linkName);
								}
								else if(entry.isLink() && linkPath.toFile().exists()) { // only if the file to be linked exists...
									// creat a hardlink
									Files.createLink(curfile.toPath(), linkPath);
									logger.info(curfile.toPath() + " linked to --> " + linkPath);
								}
						        
								final byte[] buffer = new byte[bufferSize];
						        int n = 0;
						        long count=0;
						        while (-1 != (n = tin.read(buffer))) {
						        	count += n;
						        }
								logger.debug("Read " + count + " from tar input stream");
							}
							else {
								fos = new FileOutputStream(curfile);
								bos = new BufferedOutputStream(fos, bufferSize);
								
								IOUtils.copy(tin, bos, bufferSize);
								logger.info(entryPathName + " restored to " + destinationPath);
							}
						}
						if (bos != null)
							bos.close();

					} else if (!toBeRestored && toBeVerified) {// Just on the fly checksum validation...

						byte[] originalChecksum = filePathNameToChecksumObj.get(entryPathName);
						logger.trace("originalChecksum " + Hex.encodeHexString(originalChecksum));
						byte[] checksumToBeVerified = ChecksumUtil.getChecksum(tin, checksumtype, bufferSize);
						logger.trace("checksumToBeVerified " + Hex.encodeHexString(checksumToBeVerified));

						if (Arrays.equals(originalChecksum, checksumToBeVerified)) {
							logger.trace("originalChecksum = checksumToBeVerified. All good");
						} else {
							tsr.setSuccess(false);
							logger.error("checksum mismatch " + entryPathName);
						}

					}

				}
				else {// for directories just create it so even empty directories are restored
					if(toBeRestored){
						File curfile = new File(destinationPath, entryPathName);
						if (!curfile.exists()) {
							curfile.mkdirs();
						}
					}
				}

				// If filePathNameWeNeed is a file then no need to loop further - break
				if(!isFilePathNameWeNeedIsDirectory)
					break;
				
				totalNoOfBytesRead = tin.getBytesRead();
				entry = getNextTarEntry(tin);
			}
			logger.trace("no more entries...");
		} catch (Exception e) {
			logger.error("Unable to read tar stream " + e.getMessage(), e);
			throw e;
		} finally {
			if(is != null)
				is.close();
			if (tin != null)
				tin.close();
			logger.trace("is proc alive : " + proc.isAlive());
			if (!proc.isAlive())
				logger.trace("exit : " + proc.exitValue());

			proc.destroy();
		}
		return tsr;
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
}
