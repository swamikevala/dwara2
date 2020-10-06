package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
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

	private Set<String> supportedExtns =  new TreeSet<String>();
	private List<Pattern> excludedFileNamesRegexList = new ArrayList<Pattern>();
	private String regexAllowedChrsInFileName = null;
	private Pattern allowedChrsInFileNamePattern = null;

	@PostConstruct
	public void getExcludedFileNamesRegexList() {
		regexAllowedChrsInFileName = config.getRegexAllowedChrsInFileName();
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName, Pattern.UNICODE_CHARACTER_CLASS);

		String[] junkFilesFinderRegexPatternList = config.getJunkFilesFinderRegexPatternList();
		for (int i = 0; i < junkFilesFinderRegexPatternList.length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(junkFilesFinderRegexPatternList[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}

		Iterable<Extension> extensionList = extensionDao.findAllByIgnoreIsTrueOrFiletypesIsNotNull();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toLowerCase());
		}
	}

	public StagedFileDetails evaluateAndGetDetails(Domain domain, Sequence sequence, String sourcePath, File nthIngestableFile){
		long size = 0;
		int fileCount = 0;
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
			errorList.add(error);
		}

		if(sfv != null) {
			// 6- SymLink Loop
			if(sfv.hasSymLinkLoop()) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Self referencing symbolic link loop detected");
				errorList.add(error);
			}
			
			// 7- Unresolved SymLink
			if(sfv.hasUnresolvedSymLink()) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Unresolved sym link found");
				errorList.add(error);
			}
			
			// 8- FilePathNameLength > 3072 
			if(sfv.hasAnyFilePathNameGt3072Chrs()) {
				Error error = new Error();
				error.setType(Errortype.Error);
				error.setMessage("Has file path name length > 3072 chrs");
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
		if(fileName.length() > 150) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact Name gt 150 characters");
			errorList.add(error);
		}

		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if(!m.matches()) {
			Error error = new Error();
			error.setType(Errortype.Warning);
			error.setMessage("Artifact Name contains special characters");
			errorList.add(error);
		}
		return errorList;
	}

	private boolean isStagedFileDupe(){
		return false;
		// TODO
		// same size different name is warning
		// same size with same name exists should be error
	}

	private void validateStagedFileName(){

	}

	private void validateStagedFileCount(){

	}

	private void validateStagedFileSize(){ // size is specific to artifactclass
		// use the same env concept as app.props
	}

//	static void usage() {
//		System.err.println("java Find <path>" +
//				" -name \"<glob_pattern>\"");
//		System.exit(-1);
//	}

//    public static void main(String[] args)
//        throws IOException {
//
//        if (args.length < 3 || !args[1].equals("-name"))
//            usage();
//
//        Path startingDir = Paths.get(args[0]);
//        String pattern = args[2];
//
//        Finder finder = new Finder(pattern);
//        Files.walkFileTree(startingDir, finder);
//        finder.done();
//    }

}

