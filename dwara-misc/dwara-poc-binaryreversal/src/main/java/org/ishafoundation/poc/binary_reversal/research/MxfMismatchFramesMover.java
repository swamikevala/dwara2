package org.ishafoundation.poc.binary_reversal.research;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class MxfMismatchFramesMover {
	static Pattern p = Pattern.compile("([0-9]*).md5 and");
	public static void main(String[] args) throws Exception {
		
		String binaryRevRootDir = "/data/dwara/user/pgurumurthy/binaryreversal";
		String fileName = args[0]; // T169 L79 etc.
		String comparisonDirName = "comp";
		
		String rootPath = binaryRevRootDir + File.separator + fileName + File.separator + comparisonDirName; // /data/dwara/user/pgurumurthy/binaryreversal/T169/comp
		
		// arrived at using "diff -q orig/md5 rev/md5 > origVsRevMd5Difference.log"
		String logFileName = "origVsRevMd5Difference.log";
		
		String logFilePathname = rootPath + File.separator + logFileName; // /data/dwara/user/pgurumurthy/binaryreversal/T169/comp/origVsRevMd5Difference.log

		String origDirName = "orig";
		String revDirName = "rev";

		String md5DirName = "md5";
		String framesDirName = "frames";
		
		String mismatchDirName = "mismatch";

		String origMd5DirName =  rootPath + File.separator + origDirName + File.separator + md5DirName;
		String origMd5MismatchDirName =  origMd5DirName + File.separator + mismatchDirName;
		String origFramesDirName =  rootPath + File.separator + origDirName + File.separator + framesDirName;
		String origFramesMismatchDirName =  origFramesDirName + File.separator + mismatchDirName;
		String revMd5DirName =  rootPath + File.separator + revDirName + File.separator + md5DirName;
		String revMd5MismatchDirName =  revMd5DirName + File.separator + mismatchDirName;
		String revFramesDirName =  rootPath + File.separator + revDirName + File.separator + framesDirName;
		String revFramesMismatchDirName =  revFramesDirName + File.separator + mismatchDirName;
		List<String> diffList = FileUtils.readLines(new File(logFilePathname));
		for (String nthLine : diffList) {
			Matcher m = p.matcher(nthLine);
			String nthFrame = null;
			if(m.find()) {
				nthFrame = m.group(1);
			}
			
			File origMd5File = new File(origMd5DirName + File.separator + fileName + ".mxf" + nthFrame + ".md5");
			File origMd5MismatchDir = new File(origMd5MismatchDirName);
			FileUtils.moveFileToDirectory(origMd5File, origMd5MismatchDir, true);
			
			File revMd5File = new File(revMd5DirName + File.separator + fileName + ".mxf" + nthFrame + ".md5");
			File revMd5MismatchDir = new File(revMd5MismatchDirName);
			FileUtils.moveFileToDirectory(revMd5File, revMd5MismatchDir, true);
			
			File origFramesFile = new File(origFramesDirName + File.separator + fileName + ".mxf.frm" + nthFrame);
			File origFramesMismatchDir = new File(origFramesMismatchDirName);
			FileUtils.moveFileToDirectory(origFramesFile, origFramesMismatchDir, true);
			
			File revFramesFile = new File(revFramesDirName + File.separator + fileName + ".mxf.frm" + nthFrame);
			File revFramesMismatchDir = new File(revFramesMismatchDirName);
			FileUtils.moveFileToDirectory(revFramesFile, revFramesMismatchDir, true);
		}
	}

}
