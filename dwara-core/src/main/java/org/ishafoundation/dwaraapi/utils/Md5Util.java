package org.ishafoundation.dwaraapi.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5Util {
	
	static Logger logger = LoggerFactory.getLogger(Md5Util.class);

	
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
		MessageDigest md = MessageDigest.getInstance(checksumtype.getJavaStyleChecksumtype());
		
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        while (-1 != (n = tin.read(buffer))) {
            md.update(buffer, 0, n);
        }
		return md.digest();
	}
}