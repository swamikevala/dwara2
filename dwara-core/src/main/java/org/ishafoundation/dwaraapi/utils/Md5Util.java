package org.ishafoundation.dwaraapi.utils;

import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5Util {
	
	static Logger logger = LoggerFactory.getLogger(Md5Util.class);

	
	public static byte[] getChecksum(java.io.File file, Checksumtype checksumtype) {
		 
		MessageDigest md = null;
		 try {
			 md = MessageDigest.getInstance(checksumtype.getJavaStyleChecksumtype());
		     md.update(FileUtils.readFileToByteArray(file));
		} catch (Exception e) {
			// TODO : Swallow it or cascade to top?
			logger.error("Unable to generate File checksum " + file.getAbsolutePath(), e);
		}
		return md.digest();
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