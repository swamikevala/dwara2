package org.ishafoundation.poc.binary_reversal.research;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class MxfFramer {
	
	static String outputFile = null;
	public static void main(String[] args) throws Exception {
		String rootFolder = args[0];
		String fileName = args[1];
		String origOrRev = args[2];
		String path = rootFolder + File.separator + fileName;
		outputFile = path + File.separator + fileName + "-" + origOrRev + "-Framed.mxf";

//		int bufferSize = 524288;
//	    final byte[] buffer = new byte[bufferSize];
//	    int n = 0;
//	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//	    byte[] data = new byte[0];
	    
	    
	    

	    
		// header
	    flushData((path + File.separator + fileName + ".hdr"), false);
	    	
		// individual frames
		for (int i = 1; i <= 20; i++) {
			System.out.println(i);
			flushData((path + File.separator + "comp" + File.separator + origOrRev + File.separator + fileName + ".mxf.frm" + i), true);
//			if(i>1)
//				break;
//			is = new BufferedInputStream(new FileInputStream(path + File.separator + "comp" + File.separator + "orig1" + File.separator + fileName + ".mxf.frm" + i), bufferSize);
//		    while (-1 != (n = is.read(buffer))) {
//		    	byteArrayOutputStream.reset();
//	        	byteArrayOutputStream.write(buffer, 0, n);
//	        	data = byteArrayOutputStream.toByteArray();
//		    	FileUtils.writeByteArrayToFile(new File(outputFile), data, true);
//		    }
		}
		
		// footer
		flushData(path + File.separator + fileName + ".ftr", true);
//		is = new BufferedInputStream(new FileInputStream(path + File.separator + "T169.ftr"), bufferSize);
//	    while (-1 != (n = is.read(buffer))) {
//	    	byteArrayOutputStream.reset();
//        	byteArrayOutputStream.write(buffer, 0, n);
//        	data = byteArrayOutputStream.toByteArray();
//	    	FileUtils.writeByteArrayToFile(new File(outputFile), data, true);
//	    }
		
	}
	
	public static void flushData(String inputFilePath, boolean append) throws Exception {
		byte[] generatedMxfByteArray = FileUtils.readFileToByteArray(new File(inputFilePath));
		FileUtils.writeByteArrayToFile(new File(outputFile), generatedMxfByteArray, append);
	}
	
//	public static void flushData2(String filePathName, boolean append) {
//		InputStream is = new BufferedInputStream(new FileInputStream(filePathName), bufferSize);
//		
//	    while (-1 != (n = is.read(buffer))) {
//	    	byteArrayOutputStream.reset();
//        	byteArrayOutputStream.write(buffer, 0, n);
//        	data = byteArrayOutputStream.toByteArray();
//	    	FileUtils.writeByteArrayToFile(new File(outputFile), data, append);
//	    }
//	}
}
