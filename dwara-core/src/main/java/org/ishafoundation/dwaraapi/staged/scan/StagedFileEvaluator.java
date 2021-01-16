package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StagedFileEvaluator {

	@Autowired
	private ExtensionDao extensionDao;

	@Autowired
	private Configuration config;

	@Autowired
	private DomainUtil domainUtil;

	@Autowired
	protected SequenceUtil sequenceUtil;

	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private Pattern allowedChrsInFileNamePattern = null;

	@PostConstruct
	public void getExcludedFileNamesRegexList() {
		String regexAllowedChrsInFileName = config.getRegexAllowedChrsInFileName();
		//allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName, Pattern.UNICODE_CHARACTER_CLASS); // Reverted this change per latest comment on DU-194
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);

		String[] junkFilesFinderRegexPatternList = config.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}
	
	public StagedFileDetails evaluateAndGetDetails(Domain domain, Sequence sequence, String sourcePath, File nthIngestableFile){
		long size = 0;
		int fileCount = 0;
		
		Iterable<Extension> extensionList = extensionDao.findAllByIgnoreIsTrueOrFiletypesIsNotNull();
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toLowerCase());
		}
		
		Set<String> unSupportedExtns = new TreeSet<String>();
		List<Error> errorList = new ArrayList<Error>();
		
		String fileName = nthIngestableFile.getName();
		StagedFileVisitor sfv = null;
		if(nthIngestableFile.isDirectory()) {
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
		}else {
			size = FileUtils.sizeOf(nthIngestableFile);
			fileCount = 1;
			String fileExtn = FilenameUtils.getExtension(fileName);
			// validate against the supported list of extensions in the system we have
			if(!supportedExtns.contains(fileExtn.toLowerCase()))
				unSupportedExtns.add(fileExtn);
		}

		// 0- For digi artifactclass there should be a mxf subfolder
		if(FilenameUtils.getBaseName(sourcePath).startsWith(DwaraConstants.VIDEO_DIGI_ARTIFACTCLASS_PREFIX) && !Paths.get(nthIngestableFile.getPath().toString(), "mxf").toFile().exists()) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no mxf subfolder");
			errorList.add(error);
		}	
		
		// 1- validateName
		errorList.addAll(validateName(fileName));
		
		// 2- validateCount
		if(fileCount == 0) {
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Folder has no non-junk files inside");
			errorList.add(error);
		}

		// 3- validateSize
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
		};

		// 4- dupe check on size against existing artifact
		ArtifactRepository<Artifact> domainSpecificArtifactRepository = domainUtil.getDomainSpecificArtifactRepository(domain);
		List<Artifact> alreadyExistingArtifacts = domainSpecificArtifactRepository.findAllByTotalSize(size);

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
				error.setType(Errortype.Warning);
				error.setMessage("Self referencing symbolic link loop(s) detected " + sfv.getSymLinkLoops());
				errorList.add(error);
			}
			
			// 7- Unresolved SymLink
			if(sfv.getUnresolvedSymLinks().size() > 0) {
				Error error = new Error();
				error.setType(Errortype.Warning);
				error.setMessage("Unresolved sym link(s) found " + sfv.getUnresolvedSymLinks());
				errorList.add(error);
			}
			
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

		StagedFileDetails nthIngestFile = new StagedFileDetails();
		nthIngestFile.setName(fileName);		
		nthIngestFile.setPath(sourcePath);
		nthIngestFile.setFileCount(fileCount);
		nthIngestFile.setTotalSize(size);
		nthIngestFile.setErrors(errorList);

		Boolean isKeepExtractedCode = sequence.isKeepCode();
		Boolean isForceMatch = sequence.getForceMatch();

		if(!isKeepExtractedCode) {
			String prevSeqCode = sequenceUtil.getExtractedCode(sequence, fileName);
			nthIngestFile.setPrevSequenceCode(prevSeqCode);
		}	
		if(isForceMatch != null && isForceMatch)
			nthIngestFile.setPrevSequenceCodeExpected(true);

		// TODO : Talk to swami - do we still need suggested filename now that we are not migrating
		//		String customFileName = getCustomArtifactName(fileName, prevSeqCode, sequence, isKeepExtractedCode);
		//		if(!fileName.equals(customFileName))
		//			nthIngestFile.setSuggestedName(customFileName);

		return nthIngestFile;
	}

	private List<Error> validateName(String fileName) {
		List<Error> errorList = new ArrayList<Error>();
		if(fileName.length() > 245) { // 245 because we need to add sequence number
			Error error = new Error();
			error.setType(Errortype.Error);
			error.setMessage("Artifact Name gt 245 characters");
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

	// Used in ingest to validate level 1 on file size and count
	public ArtifactFileDetails getDetails(File nthIngestableFile){
		return invokeVisitor(nthIngestableFile, false);
	}
	
	// Used in ingest move junk files
	public ArtifactFileDetails moveJunkAndGetDetails(File nthIngestableFile){
		return invokeVisitor(nthIngestableFile, true);
	}
	
	public ArtifactFileDetails invokeVisitor(File nthIngestableFile, boolean move){		
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
				//Files.walkFileTree(nthIngestableFile.toPath(), opts, Integer.MAX_VALUE, sfv);
				Files.walkFileTree(nthIngestableFile.toPath(), sfv); // Dont follow the links when moving...
			} catch (IOException e) {
				// swallow for now
			}
			if(sfv != null) {
				size = sfv.getTotalSize();
				fileCount = sfv.getFileCount();
			}
		}else {
			size = FileUtils.sizeOf(nthIngestableFile);
			fileCount = 1;
		}
		
		ArtifactFileDetails afd = new ArtifactFileDetails();
		afd.setTotalSize(size);
		afd.setCount(fileCount);
		
		return afd;
	}
	

}

