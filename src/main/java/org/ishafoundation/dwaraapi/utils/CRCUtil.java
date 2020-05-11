package org.ishafoundation.dwaraapi.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRCUtil {
	
	static Logger logger = LoggerFactory.getLogger(CRCUtil.class);
	
	public static long getCrc(File file){
        CRC32 crc = new CRC32();
        try {
			crc.update(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			// TODO : Swallow it or cascade to top?
			logger.error("Unable to generate File crc " + file.getAbsolutePath(), e);
		}
		return crc.getValue();
	}
}
