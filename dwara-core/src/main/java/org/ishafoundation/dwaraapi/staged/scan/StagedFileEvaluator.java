package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StagedFileEvaluator extends Validation{
	
	private static final Logger logger = LoggerFactory.getLogger(StagedFileEvaluator.class);

	@Autowired
	private ExtensionDao extensionDao;
	
	@Autowired
	private FlowelementDao flowelementDao;

	@Autowired
	private Configuration config;

	@Autowired
	private ArtifactDao artifactDao;

	@Autowired
	private SequenceUtil sequenceUtil;
	
	@Autowired
	private ProcessingJobManager processingJobManager;
	
	@Autowired
	private Configuration configuration;

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private Pattern allowedChrsInFileNamePattern = null;
	private Pattern photoSeriesArtifactclassArifactNamePattern = null;
	private String editedTrSeriesFlowelementTaskconfigPathnameRegex = null;
	private Set<String> supportedExtns = null;
	
	@PostConstruct
	public void setItUp() {
		String regexAllowedChrsInFileName = config.getRegexAllowedChrsInFileName();
		//allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName, Pattern.UNICODE_CHARACTER_CLASS); // Reverted this change per latest comment on DU-194
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		photoSeriesArtifactclassArifactNamePattern = Pattern.compile("([0-9]{8})_[A-Z]{3}_" + regexAllowedChrsInFileName); // 20200101_CMM_Adiyogi-Ratham-Guru-Pooja-SK-IYC
		getExcludedFileNamesRegexList();
		Iterable<Extension> extensionList = extensionDao.findAllByIgnoreIsTrueOrFiletypesIsNotNull();
		supportedExtns = new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toLowerCase());
		}
		Flowelement flowelement =  flowelementDao.findByFlowIdAndProcessingtaskIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc("video-edit-tr-proxy-flow", "video-proxy-low-gen");
		editedTrSeriesFlowelementTaskconfigPathnameRegex = flowelement.getTaskconfig().getPathnameRegex();
	}

	public void getExcludedFileNamesRegexList() {
		String[] junkFilesFinderRegexPatternList = config.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}
	
	public StagedFileDetails evaluateAndGetDetails(Sequence sequence, String sourcePath, File nthIngestableFile){
		long size = 0;
		int fileCount = 0;
		
		Set<String> unSupportedExtns = new TreeSet<String>();
		List<Error> errorList = new ArrayList<Error>();
		
		String fileName = nthIngestableFile.getName();
		StagedFileVisitor sfv = null;
		
		
		if(nthIngestableFile.isDirectory()) { // if artifact is a directory
			EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

			sfv = new StagedFileVisitor(nthIngestableFile.getName(), config.getJunkFilesStagedDirName(), excludedFileNamesRegexList, supportedExtns);
			try {
				Files.walkFileTree(nthIngestableFile.toPath(), opts, Integer.MAX_VALUE, sfv);
			} catch (IOException e) {
				// swallow for now
			}
			if(sfv != null) {
				size = sfv.getTotalSize();
				fileCount = sfv.getFileCount();
				unSupportedExtns = sfv.getUnSupportedExtns();
			}
		}else { // if artifact is a file
			size = FileUtils.sizeOf(nthIngestableFile);
			fileCount = 1;
			String fileExtn = FilenameUtils.getExtension(fileName);
			// validate against the supported list of extensions in the system we have
			if(!supportedExtns.contains(fileExtn.toLowerCase()))
				unSupportedExtns.add(fileExtn);
		}

		// 0- For digi artifactclass there should be a mxf subfolder
		if(FilenameUtils.getBaseName(sourcePath).startsWith(DwaraConstants.VIDEO_DIGI_ARTIFACTCLASS_PREFIX) && !(Paths.get(nthIngestableFile.getPath().toString(), "mxf").toFile().exists() || Paths.get(nthIngestableFile.getPath().toString(), "mov").toFile().exists())) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no mxf|mov subfolder");
			errorList.add(error);
		}	
		
		// 1- validateName
		errorList.addAll(validateName(fileName));

		// 1a - validateName for photo* artifactclass
		if(FilenameUtils.getBaseName(sourcePath).startsWith("photo")) { // validation only for photo* artifactclass
			errorList.addAll(validatePhotoName(fileName, allowedChrsInFileNamePattern, (sfv != null && sfv.getPhotoSeriesFileNameValidationFailedFileNames().size() > 0 ? sfv.getPhotoSeriesFileNameValidationFailedFileNames() : null)));
		}
		
		// 2- validateCount
		errorList.addAll(validateFileCount(fileCount));

		// 3- validateSize
		errorList.addAll(validateFileSize(size));

		// 4- dupe check on size against existing artifact
		List<Artifact> alreadyExistingArtifacts = artifactDao.findAllByTotalSizeAndDeletedIsFalse(size);

		if(alreadyExistingArtifacts.size() > 0) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			StringBuffer sb = new StringBuffer();
			for (Artifact artifact : alreadyExistingArtifacts) {
				sb.append(" Id:" + artifact.getId() + " ArtifactName:" + artifact.getName());
			}
			error.setMessage("Artifact probably already exists in dwara. Please double check. Matches" + sb.toString());
			errorList.add(error);
		}
		
		// 4b- For digi artifacts - dupe check on prev-seq-code against existing artifact - there could be multiple artifacts (PART) with same prev seq
		//if(FilenameUtils.getBaseName(sourcePath).startsWith(DwaraConstants.VIDEO_DIGI_ARTIFACTCLASS_PREFIX)) {
		String prevSequenceCode = sequenceUtil.getExtractedCode(sequence, fileName);
		
		List<Artifact> alreadyExistingArtifactList = artifactDao.findAllByPrevSequenceCode(prevSequenceCode);
		if(alreadyExistingArtifactList.size() > 0) {
			for (Artifact artifact : alreadyExistingArtifactList) {
				if(!artifact.isDeleted() && artifact.getWriteRequest() != null && artifact.getWriteRequest().getDetails().getStagedFilename().equals(fileName) && artifact.getWriteRequest().getStatus() != Status.cancelled){ 
					Error error = new Error();
					error.setType(Errortype.Warning);
					StringBuffer sb = new StringBuffer();
					sb.append(" Id:" + artifact.getId() + " ArtifactName:" + artifact.getName());
					error.setMessage("Artifact probably already exists in dwara. Please double check. Matches" + sb.toString());
					errorList.add(error);
					break;
				}
			}
		}

		//}
				
		// 5- Unsupported extns
		if(unSupportedExtns.size() > 0) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Unsupported file extensions. Please review - " + unSupportedExtns);
			// commented as per DU-261 errorList.add(error);
		}

		if(sfv != null) {
			// 6- SymLink Loop
			if(sfv.getSymLinkLoops().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Self referencing symbolic link loop(s) detected " + sfv.getSymLinkLoops());
				errorList.add(error);
			}
			
//			// 7- Unresolved SymLink
//			if(sfv.getUnresolvedSymLinks().size() > 0) {
//				Error error = new Error();
//				error.setType(Errortype.Warning);
//				error.setMessage("Unresolved sym link(s) found " + sfv.getUnresolvedSymLinks());
//				errorList.add(error);
//			}
			
			// 8- FilePathNameLength > 4096 
			if(sfv.getFilePathNamesGt4096Chrs().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Has file path name(s) length > 4096 chrs " + sfv.getFilePathNamesGt4096Chrs());
				errorList.add(error);
			}
			
			// 9- File/Directory Name contains a non-unicode char
			if(sfv.getFileNamesWithNonUnicodeChrs().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Has file name(s) with non-unicode chrs " + sfv.getFileNamesWithNonUnicodeChrs());
				errorList.add(error);
				
			}
		}
		
		// Hack for video-edit-tr* - If the folder structure doesnt match to the standards and arent any files to be proxied
		if(FilenameUtils.getBaseName(sourcePath).startsWith("video-edit-tr")) { // validation only for video-edit-tr* artifactclass
			if(!processingJobManager.isJobToBeCreated("video-proxy-low-gen", nthIngestableFile.getAbsolutePath(), editedTrSeriesFlowelementTaskconfigPathnameRegex)) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Restructure folder. Has no files to be proxied. No match for " + editedTrSeriesFlowelementTaskconfigPathnameRegex);
				errorList.add(error);
			}
		}
		

		StagedFileDetails nthIngestFile = new StagedFileDetails();
		
		Path srcPath = Paths.get(sourcePath);
		nthIngestFile.setUser(srcPath.getName(Paths.get(configuration.getReadyToIngestSrcDirRoot()).getNameCount()).toString());
		nthIngestFile.setName(fileName);		
		nthIngestFile.setPath(sourcePath);
		nthIngestFile.setFileCount(fileCount);
		nthIngestFile.setTotalSize(size);
		nthIngestFile.setErrors(errorList);

		Boolean isKeepExtractedCode = sequence.isKeepCode();
		boolean isForceMatch = (sequence.getForceMatch() != null && sequence.getForceMatch() == 1)  ? true : false;

		if(!isKeepExtractedCode) {
			String prevSeqCode = sequenceUtil.getExtractedCode(sequence, fileName);
			nthIngestFile.setPrevSequenceCode(prevSeqCode);
		}	
		if(isForceMatch)
			nthIngestFile.setPrevSequenceCodeExpected(true);

		// TODO : Talk to swami - do we still need suggested filename now that we are not migrating
		//		String customFileName = getCustomArtifactName(fileName, prevSeqCode, sequence, isKeepExtractedCode);
		//		if(!fileName.equals(customFileName))
		//			nthIngestFile.setSuggestedName(customFileName);

		return nthIngestFile;
	}
	
	public List<Error> validateName(String fileName) {
		return validateName(fileName, allowedChrsInFileNamePattern);
	}
	
	// Used in ingest to validate level 1 on file size and count
	public ArtifactFileDetails getDetails(File nthIngestableFile){
		return invokeVisitor(nthIngestableFile, false);
	}
	
	// Used in ingest move junk files
	public ArtifactFileDetails moveJunkAndGetDetails(File nthIngestableFile){
		return invokeVisitor(nthIngestableFile, true);
	}
	
	private ArtifactFileDetails invokeVisitor(File nthIngestableFile, boolean move){		
		long size = 0;
		int fileCount = 0;
		StagedFileVisitor sfv = null;
		
		Iterable<Extension> extensionList = extensionDao.findAllByIgnoreIsTrueOrFiletypesIsNotNull();
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toLowerCase());
		}
		
		if(nthIngestableFile.isDirectory()) {
			//EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		
			sfv = new StagedFileVisitor(nthIngestableFile.getAbsolutePath(), nthIngestableFile.getName(), config.getJunkFilesStagedDirName(), excludedFileNamesRegexList, supportedExtns, move);
			try {
				if(move)
					Files.walkFileTree(nthIngestableFile.toPath(), sfv); // Dont follow the links when moving...
				else {
					EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
					Files.walkFileTree(nthIngestableFile.toPath(), opts, Integer.MAX_VALUE, sfv);
				}
			} catch (IOException e) {
				// swallow for now
			}
			if(sfv != null) {
				size = sfv.getTotalSize();
				fileCount = sfv.getFileCount();
			}
		}else {
			if(!Files.isSymbolicLink(nthIngestableFile.toPath()) && nthIngestableFile.exists())
				size = FileUtils.sizeOf(nthIngestableFile);
			fileCount = 1;
		}
		
		ArtifactFileDetails afd = new ArtifactFileDetails();
		afd.setTotalSize(size);
		afd.setCount(fileCount);
		
		return afd;
	}
	

}

