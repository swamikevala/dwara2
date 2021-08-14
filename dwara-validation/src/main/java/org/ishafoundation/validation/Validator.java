package org.ishafoundation.validation;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.staged.scan.Errortype;
import org.ishafoundation.dwaraapi.staged.scan.Error;
import org.ishafoundation.dwaraapi.staged.scan.StagedFileVisitor;

public class Validator {

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private Pattern allowedChrsInFileNamePattern = null;
	private Pattern photoSeriesArtifactclassArifactNamePattern = null;
	private static SimpleDateFormat photoSeriesArtifactNameDateFormat = new SimpleDateFormat("yyyyMMdd");

	public void validator(String pathName) {
		// String folderName = pathName.substring(pathName.lastIndexOf("\\")+1);
		File folder = new File(pathName);
		File[] listOfFiles = folder.listFiles();
		
		
		String[] junkFilesFinderRegexPatternList = new String[] { "\\\\._[^\\\\/]*$", "\\\\.DS_Store$",
				"\\\\.fseventsd$", "\\\\..Spotlight-V100$", "\\\\.TemporaryItems$", "\\\\.Trashes$",
				"\\\\.VolumeIcon.icns$", "\\\\.fcpcache$", "\\\\.AppleDouble$" };

		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}

		long size = 0;
		int fileCount = 0;

		// List<Error> errorList = new ArrayList<Error>();
		for (File file : listOfFiles) {
			StagedFileVisitor sfv = null;
			if (file.isDirectory()) {
				EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
				Set<String> set = new TreeSet<String>();

				sfv = new StagedFileVisitor(file.getName(), ".dwara-ignored", excludedFileNamesRegexList, set);
				try {
					Files.walkFileTree(file.toPath(), opts, Integer.MAX_VALUE, sfv);
				} catch (IOException e) {
					// swallow for now
				}

				if (sfv != null) {
					size = sfv.getTotalSize();
					fileCount = sfv.getFileCount();
					// unSupportedExtns = sfv.getUnSupportedExtns();
				}

			} else { // if artifact is a file
				size = FileUtils.sizeOf(file);
				fileCount = 1;
				String fileExtn = FilenameUtils.getExtension(file.getName());
				// validate against the supported list of extensions in the system we have
				/*
				 * if(!supportedExtns.contains(fileExtn.toLowerCase()))
				 * unSupportedExtns.add(fileExtn);
				 */
			}

			List<Error> errorList = new ArrayList<Error>();
			String fileName = file.getName();
			System.out.println(fileName);
			/*
			 * if(fileName.length() > 245) { // 245 because we need to add sequence number
			 * Error error = new Error(); error.setType(Errortype.Error);
			 * error.setMessage("Artifact Name gt 245 characters"); errorList.add(error); }
			 * 
			 * Matcher m = allowedChrsInFileNamePattern.matcher(fileName); if(!m.matches())
			 * { Error error = new Error(); error.setType(Errortype.Error);
			 * error.setMessage("Artifact Name contains special characters");
			 * errorList.add(error); }
			 * 
			 * CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder(); try {
			 * decoder.decode(ByteBuffer.wrap(fileName.getBytes())); } catch (
			 * CharacterCodingException ex) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("Artifact Name contains non-unicode characters");
			 * errorList.add(error);
			 * 
			 * } //System.out.println(FilenameUtils.getBaseName(pathName));
			 * if(FilenameUtils.getBaseName(pathName).startsWith("photo")) { // validation
			 * only for photo* artifactclass //System.out.println("In photo"); Matcher m1 =
			 * photoSeriesArtifactclassArifactNamePattern.matcher(fileName);
			 * if(!m1.matches()) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("Artifact Name should be in yyyyMMdd_XXX_* pattern");
			 * errorList.add(error); } else { // should i check for a valid date here??? try
			 * { photoSeriesArtifactNameDateFormat.parse(m1.group(1)); } catch
			 * (ParseException e) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("Artifact Name date should be in yyyyMMdd pattern");
			 * errorList.add(error); } }
			 * 
			 * if(anyPhotoSeriesFileNameValidationFailures) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("File Names shoud be in yyyymmdd_xxx_dddd pattern");
			 * errorList.add(error); //logger.error(fileName +
			 * " has following files failing validation " +
			 * sfv.getPhotoSeriesFileNameValidationFailedFileNames()); } } if(fileCount ==
			 * 0) { Error error = new Error(); error.setType(Errortype.Error);
			 * error.setMessage("Artifact Folder has no non-junk files inside");
			 * errorList.add(error); } long configuredSize = 1048576; // 1MB // TODO whats
			 * the size we need to compare against? if(size == 0) { Error error = new
			 * Error(); error.setType(Errortype.Error);
			 * error.setMessage("Artifact size is 0"); errorList.add(error); }else if(size <
			 * configuredSize) { Error error = new Error();
			 * error.setType(Errortype.Warning);
			 * error.setMessage("Artifact is less than 1 MiB"); errorList.add(error); };
			 * if(sfv != null) { // 6- SymLink Loop if(sfv.getSymLinkLoops().size() > 0) {
			 * Error error = new Error(); error.setType(Errortype.Error);
			 * error.setMessage("Self referencing symbolic link loop(s) detected " +
			 * sfv.getSymLinkLoops()); errorList.add(error); }
			 * if(sfv.getFilePathNamesGt4096Chrs().size() > 0) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("Has file path name(s) length > 4096 chrs " +
			 * sfv.getFilePathNamesGt4096Chrs()); errorList.add(error); }
			 * 
			 * // 9- File/Directory Name contains a non-unicode char
			 * if(sfv.getFileNamesWithNonUnicodeChrs().size() > 0) { Error error = new
			 * Error(); error.setType(Errortype.Error);
			 * error.setMessage("Has file name(s) with non-unicode chrs " +
			 * sfv.getFileNamesWithNonUnicodeChrs()); errorList.add(error);
			 * 
			 * }
			 * 
			 * }
			 */
			errorList = nameValidator(pathName, file,errorList);
			errorList = sizeLengthValidator( file,sfv,errorList,size,fileCount);
			errorList = FileValidator(sfv,errorList);
			System.out.println(size+" "+fileCount);
			for (Error error : errorList) {
				System.out.println(error.getMessage());
			}
			if (sfv != null) {
				for (String file_name : sfv.getPhotoSeriesFileNameValidationFailedFileNames()) {
					System.out.println(file_name);
				}
			}

			System.out.println("\n");

		}
		// System.out.println(listOfFiles.length);

	}

	static void usage() {
		System.err.println(
				"usage: java -cp dwara-watcher-2.0.jar org.ishafoundation.dwaraapi.staged.scan.ValidatorEgForAum "
						+ "filePathName");

		System.err.println("where,");
		System.err.println("args[0] - filePathName - C:\\data\\forLTO\\Sadhguru_At_NewYork_2050");
		System.exit(-1);
	}

	public List<Error> nameValidator(String pathName, File file, List<Error> errorList) {
		String fileName = file.getName();
		String regexAllowedChrsInFileName = "[\\w-.]*";
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		photoSeriesArtifactclassArifactNamePattern = Pattern
				.compile("([0-9]{8})_[A-Z]{3}_" + regexAllowedChrsInFileName);
		if (fileName.length() > 245) { // 245 because we need to add sequence number
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name gt 245 characters");
			errorList.add(error);
		}

		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if (!m.matches()) {
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
		// System.out.println(FilenameUtils.getBaseName(pathName));
		if (FilenameUtils.getBaseName(pathName).startsWith("photo")) { // validation only for photo* artifactclass
			// System.out.println("In photo");
			Matcher m1 = photoSeriesArtifactclassArifactNamePattern.matcher(fileName);
			if (!m1.matches()) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Artifact Name should be in yyyyMMdd_XXX_* pattern");
				errorList.add(error);
			} else {
				// should i check for a valid date here???
				try {
					photoSeriesArtifactNameDateFormat.parse(m1.group(1));
				} catch (ParseException e) {
					Error error = new Error();
					error.setType(Errortype.Error);
					error.setMessage("Artifact Name date should be in yyyyMMdd pattern");
					errorList.add(error);
				}
			}

			/*
			 * if(anyPhotoSeriesFileNameValidationFailures) { Error error = new Error();
			 * error.setType(Errortype.Error);
			 * error.setMessage("File Names shoud be in yyyymmdd_xxx_dddd pattern");
			 * errorList.add(error); //logger.error(fileName +
			 * " has following files failing validation " +
			 * sfv.getPhotoSeriesFileNameValidationFailedFileNames()); }
			 */
		}

		return errorList;

	}

	public List<Error> sizeLengthValidator(File file, StagedFileVisitor sfv, List<Error> errorList, long size , long fileCount) {

		//long size = 0;
		//int fileCount = 0;
		if (sfv != null) {
			size = sfv.getTotalSize();
			fileCount = sfv.getFileCount();
			// unSupportedExtns = sfv.getUnSupportedExtns();
		} else { // if artifact is a file
			size = FileUtils.sizeOf(file);
			fileCount = 1;
			String fileExtn = FilenameUtils.getExtension(file.getName());
			// validate against the supported list of extensions in the system we have
			/*
			 * if(!supportedExtns.contains(fileExtn.toLowerCase()))
			 * unSupportedExtns.add(fileExtn);
			 */
		}
		if (fileCount == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no non-junk files inside");
			errorList.add(error);
		}
		long configuredSize = 1048576; // 1MB // TODO whats the size we need to compare against?
		if (size == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact size is 0");
			errorList.add(error);
		} else if (size < configuredSize) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact is less than 1 MiB");
			errorList.add(error);
		}

		return errorList;

	}

	public List<Error> FileValidator(StagedFileVisitor sfv, List<Error> errorList) {
		if (sfv != null) {
			// 6- SymLink Loop
			if (sfv.getSymLinkLoops().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Self referencing symbolic link loop(s) detected " + sfv.getSymLinkLoops());
				errorList.add(error);
			}
			if (sfv.getFilePathNamesGt4096Chrs().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Has file path name(s) length > 4096 chrs " + sfv.getFilePathNamesGt4096Chrs());
				errorList.add(error);
			}

			// 9- File/Directory Name contains a non-unicode char
			if (sfv.getFileNamesWithNonUnicodeChrs().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Has file name(s) with non-unicode chrs " + sfv.getFileNamesWithNonUnicodeChrs());
				errorList.add(error);

			}

		}

		return errorList;

	}

	public static void main(String[] args) {
		if (args.length != 1)
			usage();

		Validator validator = new Validator();
		/*
		 * Scanner myObj = new Scanner(System.in); // Create a Scanner object
		 * System.out.println("Enter pathname");
		 */

		String pathName = args[0];
		validator.validator(pathName);
		/*
		 * File folder = new File(pathName); File[] listOfFiles = folder.listFiles();
		 * for(File file : listOfFiles){ File [] files = file.listFiles();
		 * if(files.length>0){ for(File photofile : files){
		 * validator.photoPub(photofile,pathName); }} else
		 * {validator.photoPub(file,pathName); } } }
		 */
	}
}
