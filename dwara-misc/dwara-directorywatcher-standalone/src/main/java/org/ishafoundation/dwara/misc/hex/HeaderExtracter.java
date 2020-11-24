package org.ishafoundation.dwara.misc.hex;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

public class HeaderExtracter {

    public static void main(String[] args) throws Exception {
    	int bufferSize = 524288;
    	byte[] pattern = Hex.decodeHex("06 0E 2B 34 02 53 01 01 0D 01 03 01 14 02 01 00".replaceAll("\\s", ""));
    	
    	InputStream is = new BufferedInputStream(new FileInputStream(args[0]), bufferSize);
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        long count = 1;
        while (-1 != (n = is.read(buffer))) {
        	System.out.println(count);
        	byteArrayOutputStream.write(buffer, 0, n);
        	byte[] data = byteArrayOutputStream.toByteArray();
        	int headerLength = HexPatternFinderUtil.indexOf(data, pattern);
        	
        	if(headerLength != -1) {
        		System.out.println("found it after " + count + " loops");
            	byte[] headerData = new byte[headerLength];
            	System.out.println("headerLength " + headerLength);
            	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            	InputStream bis = new BufferedInputStream(byteArrayInputStream, bufferSize);
            	IOUtils.read(bis, headerData, 0, headerLength);
            	//"C:\\Users\\prakash\\P22267_sample.hdr"
            	IOUtils.write(headerData, new FileOutputStream(args[1]));
        		break;
        	}
        	count += 1;
        	
        }
        
//    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
//    	
//    	byte[] data = FileUtils.readFileToByteArray(new File("C:\\Users\\prakash\\P22267_sample.mxf"));
//    	
//    	data.length
//    	int headerLength = indexOf(data, pattern);
//    	byte[] headerData = new byte[headerLength];
//    	
//    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
//    	int bufferSize = 8192;
//    	InputStream is = new BufferedInputStream(byteArrayInputStream, bufferSize);
//    	IOUtils.read(is, headerData, 0, headerLength);
//    	
//    	IOUtils.write(headerData, new FileOutputStream("C:\\Users\\prakash\\P22267_sample.hdr"));
	}
}
