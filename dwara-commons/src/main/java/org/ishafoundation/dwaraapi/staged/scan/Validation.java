package org.ishafoundation.dwaraapi.staged.scan;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {
	
	private static final Logger logger = LoggerFactory.getLogger(Validation.class);
	private static SimpleDateFormat photoSeriesArtifactNameDateFormat = new SimpleDateFormat("yyyyMMdd");
	
	public List<Error> validateName(String fileName, Pattern allowedChrsInFileNamePattern) {
		List<Error> errorList = new ArrayList<Error>();
		if(fileName.length() > 238) { // 238 because we need to add sequence number and when inserting catalog to catdv it becomes like 2006/09/VDL1154_
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name gt 238 characters");
			errorList.add(error);
		}

		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if(!m.matches()) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name contains special characters");
			errorList.add(error);
		}
		
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		try {
			decoder.decode(ByteBuffer.wrap(fileName.getBytes()));		           
		} catch (CharacterCodingException ex) {		        
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name contains non-unicode characters");
			errorList.add(error);
		} 

		return errorList;
	}
	
	
	// 1a - validateName for photo* artifactclass
	public List<Error> validatePhotoName(String fileName, Pattern photoSeriesArtifactclassArifactNamePattern, Set<String> photoSeriesFileNameValidationFailedFileNames) { // validation only for photo* artifactclass
		List<Error> errorList = new ArrayList<Error>();
		Matcher m = photoSeriesArtifactclassArifactNamePattern.matcher(fileName);
		if(!m.matches()) { 
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name should be in yyyyMMdd_XXX_* pattern");
			errorList.add(error);
		} else {
			// should i check for a valid date here???
			try {
				photoSeriesArtifactNameDateFormat.parse(m.group(1));
			} catch (ParseException e) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Artifact Name date should be in yyyyMMdd pattern");
				errorList.add(error);
			}
		}
		
		if(photoSeriesFileNameValidationFailedFileNames != null) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("File Names shoud be in yyyymmdd_xxx_dddd pattern");
			errorList.add(error);
			logger.error(fileName + " has following files failing validation "  + photoSeriesFileNameValidationFailedFileNames);
		}
		
		return errorList;
	}

	public List<Error> validateFileCount(int fileCount) {
		List<Error> errorList = new ArrayList<Error>();
		if(fileCount == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no non-junk files inside");
			errorList.add(error);
		}
		return errorList;
	}
	
	public List<Error> validateFileSize(long size) {
		List<Error> errorList = new ArrayList<Error>();
		long configuredSize = 1048576; // 1MB // TODO whats the size we need to compare against?
		if(size == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact size is 0");
			errorList.add(error);
		}else if(size < configuredSize) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact is less than 1 MiB");
			errorList.add(error);
		}
		return errorList;
	}
}
