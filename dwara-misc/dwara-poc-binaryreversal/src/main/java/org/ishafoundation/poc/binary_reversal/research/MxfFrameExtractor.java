package org.ishafoundation.poc.binary_reversal.research;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;

public class MxfFrameExtractor {
	
	private static String frameStartHexPattern = "06 0E 2B 34 02 53 01 01 0D 01 03 01 14 02 01 00".replaceAll("\\s", "");
	
	public static void main(String[] args) throws Exception {
		
		String sourceMxfFilePathname = args[0];
		String mxfFileName = StringUtils.substringBefore(new File(sourceMxfFilePathname).getName(),"_");
		String nthFrameRoot = args[1] + File.separator ;
		
    	int bufferSize = 524288;
    	byte[] frameStartIdentifierPattern = Hex.decodeHex(frameStartHexPattern);
    	
    	InputStream is = new BufferedInputStream(new FileInputStream(sourceMxfFilePathname), bufferSize);
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	
    	int count = 0;
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        int index = -1;
        byte[] data = new byte[0];
        while (-1 != (n = is.read(buffer))) {
        	byteArrayOutputStream.write(buffer, 0, n);
        	data = byteArrayOutputStream.toByteArray();
        	byte[] slice = Arrays.copyOfRange(data, 1, data.length);
        	System.out.println("dl - " + data.length);
        	index = HexPatternFinderUtil.indexOf(slice, frameStartIdentifierPattern);
        	System.out.println("index - " + index);
        	if(index != -1) {
            	index = index + 1;
            	System.out.println("added index - " + index);

            	byte[] headerOrNthFrameData = new byte[index];
            	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            	InputStream bis = new BufferedInputStream(byteArrayInputStream, bufferSize);
            	IOUtils.read(bis, headerOrNthFrameData, 0, index);
            	System.out.println("dl after - " + data.length);
            	String fileToBeGeneratedForValidation = null;
            	
            	if(count == 0)
            		fileToBeGeneratedForValidation = nthFrameRoot + mxfFileName + ".hdr";
            	else
            		fileToBeGeneratedForValidation = nthFrameRoot + mxfFileName + ".frm" + count;
            	
            	IOUtils.write(headerOrNthFrameData, new FileOutputStream(fileToBeGeneratedForValidation));

            	String fileToBeGeneratedForMd5 = nthFrameRoot + mxfFileName + count + ".md5" ;
            	// byte[] revBinariedMxfDigest =  ChecksumUtil.getChecksum(new File(fileToBeGeneratedForValidation), Checksumtype.md5);
        		byte[] revBinariedMxfDigest =  ChecksumUtil.getChecksum(new ByteArrayInputStream(headerOrNthFrameData), Checksumtype.md5, bufferSize);
            	IOUtils.write(DatatypeConverter.printHexBinary(revBinariedMxfDigest).toUpperCase(), new FileOutputStream(fileToBeGeneratedForMd5));

            	byteArrayOutputStream.reset();
            	byteArrayOutputStream.write(data, index, data.length - index);
            	data = byteArrayOutputStream.toByteArray();
            	System.out.println("dl again - " + data.length);
            	System.out.println("");
            	System.out.println("****************");
            	System.out.println("");
//            	if(count > 115)
//            		break;
            	count = count + 1;
            	index = -1;
        	}
        }
	}
}
