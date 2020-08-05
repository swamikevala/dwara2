package org.ishafoundation.dwaraapi.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumUtil {
	
	static Logger logger = LoggerFactory.getLogger(ChecksumUtil.class);

	
	public static byte[] getChecksum(java.io.File file, Checksumtype checksumtype) throws Exception{

		// TODO : read byte chunks than full file - Added only for testing... 
//		MessageDigest md = null;
//		 try {
//			 md = MessageDigest.getInstance(checksumtype.getJavaStyleChecksumtype());
//		     md.update(FileUtils.readFileToByteArray(file));
//		} catch (Exception e) {
//			// TODO : Swallow it or cascade to top?
//			logger.error("Unable to generate File checksum " + file.getAbsolutePath(), e);
//		}
//		return md.digest();

		//TODO : Hardcoded bufferSize - Configure it... What is the optimum buffersize???
		int bufferSize = 524288; // 512k
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), bufferSize);
		return getChecksum(bis, checksumtype, bufferSize);
	}
	
	public static byte[] getChecksum(InputStream tin, Checksumtype checksumtype, int bufferSize) throws Exception {
		return restoreFileAndGetChecksum(tin, checksumtype, bufferSize, null);
	}
	
	public static byte[] restoreFileAndGetChecksum(InputStream tin, Checksumtype checksumtype, int bufferSize, BufferedOutputStream bos) throws Exception {
		MessageDigest md = MessageDigest.getInstance(checksumtype.getJavaStyleChecksumtype());
		
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count = 0;
        while (-1 != (n = tin.read(buffer))) {
    		if (bos != null) {
				bos.write(buffer, 0, n);
			}
            md.update(buffer, 0, n);
            count += n;
        }
        if (bos != null)
        	logger.trace("restoredBytes " + count);
		return md.digest();
	}
	
	public static boolean compareChecksum(HashMap<String, byte[]> filePathNameToChecksum,
			String destinationPath, String filePathNameToBeVerified, Checksumtype checksumtype) throws Exception {
		
		// calculating the restored file' checksum
		boolean verify = true;
		java.io.File artifactToBeVerified = new java.io.File(destinationPath + java.io.File.separator + filePathNameToBeVerified);
		Collection<java.io.File> artifactFileAndDirsList = FileUtils.listFilesAndDirs(artifactToBeVerified, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (java.io.File file : artifactFileAndDirsList) {
			if(file.isDirectory())
				continue;
			byte[] checksum = getChecksum(file, checksumtype);
			
			String filePathName = file.getAbsolutePath();
			filePathName = filePathName.replace(destinationPath + java.io.File.separator, "");
			byte[] originalChecksum = filePathNameToChecksum.get(filePathName);
			
			if(!Arrays.equals(checksum, originalChecksum)) {
				verify = false;
				logger.error("Checksum mismatch " + filePathName + " restored checksum : " + checksum + " original checksum : " + originalChecksum);
			}	
		}
		logger.debug("verification status " + verify);
		artifactToBeVerified.delete();
		logger.trace(artifactToBeVerified + " deleted");
		return verify;
	}
}