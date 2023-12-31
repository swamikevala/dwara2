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

	
	public static byte[] getChecksum(String filepathname) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(filepathname.getBytes());
	    return md.digest();
	}
	
	public static byte[] getChecksum(java.io.File file, Checksumtype checksumtype) throws Exception{
		//TODO : Hardcoded bufferSize - Configure it... What is the optimum buffersize???
		BufferedInputStream bis = null;
		try {
			int bufferSize = 524288; // 512k
			bis = new BufferedInputStream(new FileInputStream(file), bufferSize);
			return getChecksum(bis, checksumtype, bufferSize);
		}
		finally {
			bis.close();
		}
	}
	
	public static byte[] getChecksum(InputStream is, Checksumtype checksumtype, int bufferSize) throws Exception {
		return restoreFileAndGetChecksum(is, checksumtype, bufferSize, null);
	}
	
	public static byte[] restoreFileAndGetChecksum(InputStream is, Checksumtype checksumtype, int bufferSize, BufferedOutputStream bos) throws Exception {
		MessageDigest md = MessageDigest.getInstance(checksumtype.getJavaStyleChecksumtype());
		
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count = 0;
        while (-1 != (n = is.read(buffer))) {
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
		return compareChecksum(filePathNameToChecksum, destinationPath, filePathNameToBeVerified, checksumtype, false); 
	}
	
	public static boolean compareChecksum(HashMap<String, byte[]> filePathNameToChecksum,
			String destinationPath, String filePathNameToBeVerified, Checksumtype checksumtype, boolean deleteFileToBeVerified) throws Exception {
		// calculating the restored file' checksum
		boolean verify = true;
		java.io.File fileToBeVerified = new java.io.File(destinationPath + java.io.File.separator + filePathNameToBeVerified);
		if(fileToBeVerified.isDirectory()) {
			Collection<java.io.File> artifactFileAndDirsList = FileUtils.listFilesAndDirs(fileToBeVerified, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			
			for (java.io.File nthFileToBeVerified : artifactFileAndDirsList) {
				if(nthFileToBeVerified.isDirectory())
					continue;
				verify = compare(filePathNameToChecksum, destinationPath, nthFileToBeVerified, checksumtype);
			}
		}
		else {
			verify = compare(filePathNameToChecksum, destinationPath, fileToBeVerified, checksumtype);
		}
		logger.debug("verification status " + verify);
		if(deleteFileToBeVerified) {
			fileToBeVerified.delete();
			logger.trace(fileToBeVerified + " deleted");
		}
		return verify;
	}
	
	private static boolean compare(HashMap<String, byte[]> filePathNameToChecksum,
			String destinationPath, java.io.File fileToBeVerified, Checksumtype checksumtype) throws Exception {
		boolean verify = true;
		byte[] checksum = getChecksum(fileToBeVerified, checksumtype);
		
		String filePathName = fileToBeVerified.getAbsolutePath();
		filePathName = filePathName.replace(destinationPath + java.io.File.separator, "");
		byte[] originalChecksum = filePathNameToChecksum.get(filePathName);
		
		if(!Arrays.equals(checksum, originalChecksum)) {
			verify = false;
			logger.error("Checksum mismatch " + filePathName + " restored checksum : " + checksum + " original checksum : " + originalChecksum);
		}
		return verify;	
	}


}