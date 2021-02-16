package org.ishafoundation.dwara.misc.watcher;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwara.misc.common.Status;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactValidator {
	
	private static Logger logger = LoggerFactory.getLogger(ArtifactValidator.class);
	
	static String justNeededExtns[] = {"qc","log", "md5", "mxf"};
	static String neededPlusOptionalExtns[] = {"qc","log", "md5", "mxf", "jpg", "mp4"};
	
	static Pattern dvFullTapeNameRegEx = Pattern.compile("([A-Z]+)([0-9]*)");
	static Pattern sequenceLeadingZeroRegEx = Pattern.compile("([0]+)([0-9]*)");
	static Pattern dvMultiPartTapeNameRegEx = Pattern.compile("([A-Z]+)([0-9]*)_PART_([0-9]*)");
	
	static ArtifactValidationResponse validateName(String artifactName){
		ArtifactValidationResponse avr = new ArtifactValidationResponse();
		
		if(artifactName.contains(" ")) { // For now just space
			avr.setValid(false);
			avr.setFailureReason("Artifact Name contains space");
			return avr;
		}
		
		Matcher dvFullTapeName = dvFullTapeNameRegEx.matcher(artifactName); 	
		if(dvFullTapeName.find()) {
			String sequencePart = dvFullTapeName.group(2);
			Matcher sequence = sequenceLeadingZeroRegEx.matcher(sequencePart);
			if(sequence.matches()) {
				avr.setValid(false);
				avr.setFailureReason("Artifact Name contains leading zeroes");
				return avr;
			}
		}

		if(artifactName.contains("PART")) { // For now just space
			Matcher dvMultiPartTapeName = dvMultiPartTapeNameRegEx.matcher(artifactName);
			if(dvMultiPartTapeName.matches()) {
				String sequencePart = dvMultiPartTapeName.group(3);
				Matcher sequence = sequenceLeadingZeroRegEx.matcher(sequencePart);
				if(sequence.matches()) {
					avr.setValid(false);
					avr.setFailureReason("Artifact Part Name contains leading zeroes");
					return avr;
				}
			}else {
				avr.setValid(false);
				avr.setFailureReason("Artifact with Parts should be something like \"AB123_PART_1\"");
				return avr;
			}
		}
		
		avr.setValid(true);
		return avr;
	}
	
	static ArtifactValidationResponse validateFiles(File artifactFileObj) {
		ArtifactValidationResponse avr = new ArtifactValidationResponse();
			
		String artifactName = artifactFileObj.getName();
		
		Collection<File> files = FileUtils.listFilesAndDirs(artifactFileObj, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			// if any file named other than the artifact name
			if(!FilenameUtils.getBaseName(file.getName()).equals(artifactName)) {
				String failureReason = "Files named other than " + artifactName + " present";
				logger.error(failureReason);
				avr.setValid(false);
				avr.setFailureReason(failureReason);
				return avr;
			}
		}
		
		// any extra files??
		Collection<File> neededPlusOptionalFiles = FileUtils.listFiles(artifactFileObj, neededPlusOptionalExtns, false);
		if((files.size() - 1) > neededPlusOptionalFiles.size()) {
			String failureReason = "Extra files present in " + artifactName;
			logger.error(failureReason);
			avr.setValid(false);
			avr.setFailureReason(failureReason);
			return avr;
		}
		
		avr.setValid(true);
		return avr;
	}
	
	// do we have all needed files??
	static ArtifactValidationResponse neededFiles(File artifactFileObj, Collection<File> neededFiles) {
		ArtifactValidationResponse avr = new ArtifactValidationResponse();
		
		String artifactName = artifactFileObj.getName();
		
		if(neededFiles.size() != 4) {
			String failureReason = "Not all mandatory files present in " + artifactName;
			logger.error(failureReason);
			avr.setValid(false);
			avr.setFailureReason(failureReason);
			return avr;
		}
		
		avr.setValid(true);
		return avr;
	}
	
	static boolean validateChecksum(Path artifactPath, Collection<File> files) throws Exception {
		String expectedMd5 = null;
		String actualMd5 = null;
		for (File file : files) {
			String fileName = file.getName();
			if(fileName.endsWith(".md5")) {
				expectedMd5 = StringUtils.substringBefore(FileUtils.readFileToString(file), "  ").trim().toUpperCase();
			}
			else if(fileName.endsWith(".mxf")) {
				logger.info(String.format("%s %s", artifactPath, Status.verifying));
				byte[] digest =  ChecksumUtil.getChecksum(file, Checksumtype.md5);
				actualMd5 = DatatypeConverter.printHexBinary(digest).toUpperCase();
			}
		}
		logger.info(artifactPath + " expectedMd5 " + expectedMd5 + " actualMd5 " + actualMd5);
		if(expectedMd5.equals(actualMd5)) {
			logger.info(String.format("%s %s", artifactPath, Status.verified));
			return true;
		}else {
			logger.error(artifactPath + "MD5 expected != actual, Now what??? ");
			logger.info(String.format("%s %s", artifactPath, Status.md5_mismatch));
			return false;
		}
	}
	
	
	public static void main(String[] args) {
		File artifactFileObj = new File(args[0]);

		if(!artifactFileObj.isDirectory())
			return;

		String artifactName = artifactFileObj.getName();

		
		ArtifactValidationResponse avr = ArtifactValidator.validateName(artifactName);
		if(!avr.isValid()) { 
			System.out.println("Name not valid - " + avr.getFailureReason());
			return;
		}
		
		avr = ArtifactValidator.validateFiles(artifactFileObj);
		if(!avr.isValid()) { 
			System.out.println("Folder structure not valid - " +  avr.getFailureReason());
			return;
		}
		

		Collection<File> neededFiles = FileUtils.listFiles(artifactFileObj, ArtifactValidator.justNeededExtns, false);
		avr = ArtifactValidator.neededFiles(artifactFileObj, neededFiles);
		if(!avr.isValid()){
			System.out.println("Needed files - " +  avr.getFailureReason());
			return;
		}
		boolean isChecksumValid;
		try {
			isChecksumValid = ArtifactValidator.validateChecksum(artifactFileObj.toPath(), neededFiles);
			if(!isChecksumValid){
				System.out.println("Check sum mismatch");
				return;
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}