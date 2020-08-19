package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SourceDirScanner {
    
	private static final Logger logger = LoggerFactory.getLogger(SourceDirScanner.class);
	
	@Autowired
    private SequenceDao sequenceDao;

	@Autowired
    private ExtensionDao extensionDao;
	
	@Autowired
    private CommandLineExecuter commandLineExecuter;
	
	@Autowired
    private Configuration configuration;
	
    private Pattern folderNameWithoutPrevSeqCodePattern = Pattern.compile("([_-]?)(.*)");

    private String DELETED = "deleted";
	
    private String regexAllowedChrsInFileName = null;
	Pattern allowedChrsInFileNamePattern = null;
	private List<Pattern> excludedFileNamesRegexList = null;
	
	@PostConstruct
	private void loadConfigEntries() {
		regexAllowedChrsInFileName = configuration.getRegexAllowedChrsInFileName();
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
		excludedFileNamesRegexList = new ArrayList<Pattern>();
		for (int i = 0; i < configuration.getJunkFilesFinderRegexPatternList().length; i++) {
			Pattern nthJunkFilesFinderRegexPattern = Pattern.compile(configuration.getJunkFilesFinderRegexPatternList()[i]);
			excludedFileNamesRegexList.add(nthJunkFilesFinderRegexPattern);
		}
	}
	
	public List<StagedFileDetails> scanSourceDir(Artifactclass artifactclass, List<String> scanFolderBasePathList) {
		//int artifactclassId = artifactclass.getId();
    	String artifactclassName = artifactclass.getName();
        int sequenceId = artifactclass.getSequenceId(); // getting the primary key of the Sequence table which holds the lastsequencenumber for this group...
        Sequence sequence = null;
        try {
        	sequence = sequenceDao.findById(sequenceId).get();
        }catch (Exception e) {
        	logger.error("Missing sequence table row for " + sequenceId);
        	return null;
		}
    	String extractionRegex = sequence.getArtifactExtractionRegex();
    	Boolean isKeepExtractedCode = sequence.isArtifactKeepCode();
    	
        List<StagedFileDetails> ingestReadyFileList = new ArrayList<StagedFileDetails>();
    	for (String nthScanFolderBasePath : scanFolderBasePathList) {
			String sourcePath = nthScanFolderBasePath + File.separator + Action.ingest.name() + File.separator + artifactclassName;
			
			//IOFileFilter dirFilter = FileFilterUtils.directoryFileFilter(); We need not show only directories but can also show files - Like Audio and 
			IOFileFilter notCancelledFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(Status.cancelled.toString()));
			IOFileFilter notDeletedFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(DELETED));
			
			//FileFilter allDirectoriesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(dirFilter, notCancelledFolderFilter, notDeletedFolderFilter);
			FileFilter allFilesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(notCancelledFolderFilter, notDeletedFolderFilter);//FileFilterUtils.and(dirFilter, notCancelledFolderFilter, notDeletedFolderFilter);
//			if(artifactclassName.contains("audio"))
//				allFilesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(notCancelledFolderFilter, notDeletedFolderFilter);
	    	File[] ingestableFiles = new File(sourcePath).listFiles(allFilesExcludingCancelledAndDeletedDirectoryFilter);
	    	if(ingestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseIngestDirectoryList = scanForIngestableFiles(sequence, extractionRegex, isKeepExtractedCode, sourcePath, ingestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseIngestDirectoryList);
	    	}
	    	
	    	// All cancelled mediaartifact...
	    	String cancelledOriginSourceDir = sourcePath + File.separator + Status.cancelled.toString();
	    	File[] cancelledIngestableFiles = new File(cancelledOriginSourceDir).listFiles();
	    	if(cancelledIngestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseCancelledDirectoryIngestFileList = scanForIngestableFiles(sequence, extractionRegex, isKeepExtractedCode, cancelledOriginSourceDir, cancelledIngestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseCancelledDirectoryIngestFileList);
	    	}	
	    	
	    	// All deleted mediaartifact...
	    	String deletedOriginSourceDir = sourcePath + File.separator + DELETED;
	    	File[] deletedIngestableFiles = new File(deletedOriginSourceDir).listFiles();
	    	if(deletedIngestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseDeletedDirectoryIngestFileList = scanForIngestableFiles(sequence, extractionRegex, isKeepExtractedCode, deletedOriginSourceDir, deletedIngestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseDeletedDirectoryIngestFileList);
	    	}		    	
		}
		
		return ingestReadyFileList;
	}
	
	private List<StagedFileDetails> scanForIngestableFiles(Sequence sequence, String extractionRegex, boolean isKeepExtractedCode, String sourcePath, File[] ingestableFiles) {
		
		List<StagedFileDetails> ingestFileList = new ArrayList<StagedFileDetails>();
    	for (int i = 0; i < ingestableFiles.length; i++) {
			File nthIngestableFile = ingestableFiles[i];
			StagedFileDetails nthIngestFile = getFileAttributes(nthIngestableFile, sequence, extractionRegex, isKeepExtractedCode, sourcePath);
			ingestFileList.add(nthIngestFile);
		}
    	return ingestFileList;
	}

	
	private StagedFileDetails getFileAttributes(File nthIngestableFile, Sequence sequence, String extractionRegex, boolean isKeepExtractedCode, String sourcePath) {
        long size = 0;
        int fileCount = 0;
        
        String fileName = nthIngestableFile.getName();
        
        // added to try catch for the test api not to throw an error when looking for a non-existing folder
        try {
        	size = FileUtils.sizeOf(nthIngestableFile);
        }catch (Exception e) {
			// swallowing it...
		}
        
        if(nthIngestableFile.isDirectory()) {
            try {
            	fileCount = FileUtils.listFiles(nthIngestableFile, null, true).size();
            }catch (Exception e) {
				// swallowing it...
			}
        }else {
        	fileCount = 1;
        }
		
		StagedFileDetails nthIngestFile = new StagedFileDetails();
		nthIngestFile.setName(fileName);		
		nthIngestFile.setPath(sourcePath);
		nthIngestFile.setFileCount(fileCount);
		nthIngestFile.setTotalSize(size);
		//nthIngestDirectory.setFileSizeInMBytes(totalSizeInMB);
		
		String warning = validate(nthIngestFile).toString();
		if(warning != null) {
			String errorType = null;
			errorType = "Warning";// TODO hardcoded for now - fetch it from error type table...
			nthIngestFile.setErrorType(errorType);
			nthIngestFile.setErrorMessage(warning);
		}

		String prevSeqCode = null;
		if(StringUtils.isNotBlank(extractionRegex)) {
			prevSeqCode = getExistingSeqCodeFromArtifactName(fileName, extractionRegex);
		}
		
		// TODO : Talk to swami - we still need suggested filename...
		String customFileName = getCustomArtifactName(fileName, prevSeqCode, sequence, isKeepExtractedCode);

		nthIngestFile.setPrevSequenceCode(prevSeqCode);
		if(StringUtils.isNotBlank(extractionRegex) && isKeepExtractedCode) // if regex present and useExtractCode is true but prevSeqCode is null then throw error...
			nthIngestFile.setPrevSequenceCodeExpected(true);
		
		if(!fileName.equals(customFileName))
			nthIngestFile.setSuggestedName(customFileName);
		
		return nthIngestFile;
	}

	private StringBuffer validate(StagedFileDetails stagedFileDetails) {
		StringBuffer messageBuilder = new StringBuffer();
		
//    	File mediaLibraryFile = FileUtils.getFile(stagedFileDetails.getPath(), stagedFileDetails.getName());
//    	if(!mediaLibraryFile.exists())
//    		appendMessage(messageBuilder, "The following artifact doesnt exist - " + mediaLibraryFile.getAbsolutePath());

		// validateCount
		if(stagedFileDetails.getFileCount() == 0)
			appendMessage(messageBuilder, "Folder has no files inside");
		
		// validateSize
		// TODO whats the size?
		long configuredSize = 1024; 
		if(stagedFileDetails.getTotalSize() < configuredSize) {
			appendMessage(messageBuilder, "Folder is less than " + configuredSize);
		};
		
		//messageBuilder = validateName(stagedFileDetails.getName(), messageBuilder);
		validateName(stagedFileDetails.getName(), messageBuilder);
		
		checkUnsupportedExtensions(stagedFileDetails, messageBuilder);
		
		setPermissions(stagedFileDetails, messageBuilder);
		
		return messageBuilder;
	}
	
	private void appendMessage(StringBuffer messageBuilder, String message) {
		if(messageBuilder.length() > 0)
			messageBuilder.append(" & ");
		
		messageBuilder.append(message);
	}
	
	private StringBuffer validateName(String fileName, StringBuffer messageBuilder) {
		if(fileName.length() > 150)
			appendMessage(messageBuilder, "File Name gt 150 characters");


		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if(!m.matches()) {

			appendMessage(messageBuilder, "File Name contains spl characters");
		}
		return messageBuilder;
	}


	private void checkUnsupportedExtensions(StagedFileDetails stagedFileDetails, StringBuffer messageBuilder) {
		Iterable<Extension> extensionList = extensionDao.findAll();
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toUpperCase());
			supportedExtns.add(extension.getId().toLowerCase());
		}

    	// Step 2 - Iterate through all Files under the artifact Directory and check if their extensions are supported in our system.
    	String artifactName = stagedFileDetails.getName();
    	String originFolderPath = stagedFileDetails.getPath();
    	
    	File mediaLibraryFile = FileUtils.getFile(originFolderPath, artifactName);
    	
		Collection<File> allFilesInTheSystem = null;
        if(mediaLibraryFile.isDirectory()) {
        	allFilesInTheSystem = FileUtils.listFiles(mediaLibraryFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	    }else {
	    	allFilesInTheSystem = new ArrayList<File>();
	    	allFilesInTheSystem.add(mediaLibraryFile);
	    }
        
		Set<String> unSupportedExtns =  new TreeSet<String>();
		// iterate the files and get extensions
		for (Iterator<File> iterator = allFilesInTheSystem.iterator(); iterator.hasNext();) {
			File nthFile = (File) iterator.next();
			String nthFileName = nthFile.getName();
			String nthFileExtn = FilenameUtils.getExtension(nthFileName);
			
//			// excluding known useless files
//			String[] excludedExtns = config.getFileExtensionsToBeExcludedFromValidation();
//			List<String> supportedVideoExtnsAsList =  new ArrayList<String>(Arrays.asList(excludedExtns));
//			if (supportedVideoExtnsAsList.contains(nthFileExtn.toUpperCase())) 
//				continue;
			
			// skipping junk files...
			boolean isJunkFile = false;
			for (Iterator<Pattern> iterator2 = excludedFileNamesRegexList.iterator(); iterator2.hasNext();) {
				Pattern nthJunkFilesFinderRegexPattern = iterator2.next();
				Matcher m = nthJunkFilesFinderRegexPattern.matcher(nthFileName);
				if(m.matches()) {
					isJunkFile = true;
					break;
				}
			}
			if(isJunkFile) 
				continue;
			
			// validate against the supported list of extensions in the system we have
			if(!supportedExtns.contains(nthFileExtn))
				unSupportedExtns.add(nthFileExtn);
		}
		
    	// Step 3 - Throw exception with unsupported extns list	
		if(unSupportedExtns.size() > 0)
			appendMessage(messageBuilder, "There are unSupported Extns in the list. Please review them..." + unSupportedExtns);
		
	}
	
	private void setPermissions(StagedFileDetails stagedFileDetails, StringBuffer messageBuilder) {
		if(configuration.isLibraryFileSystemPermissionsNeedToBeSet()) {
		
			String script = configuration.getLibraryFile_ChangePermissionsScriptPath();
			String artifactName = stagedFileDetails.getName();
			String sourcePath = stagedFileDetails.getPath();// holds something like /data/user/pgurumurthy/ingest/pub-video
			File toBeIngestedFile = FileUtils.getFile(sourcePath, artifactName);
			
			CommandLineExecutionResponse setPermsCommandLineExecutionResponse = null;
			try {
				setPermsCommandLineExecutionResponse = changeFilePermissions(script, sourcePath, artifactName);
				
				if(!setPermsCommandLineExecutionResponse.isComplete()) {					
					appendMessage(messageBuilder, "Unable to set permissions to " + toBeIngestedFile + ". " + setPermsCommandLineExecutionResponse.getFailureReason());
				}

			} catch (Exception e) {
				String message = "Unable to set permissions to " + toBeIngestedFile + " : " + e.getMessage();
				appendMessage(messageBuilder, message);
				logger.warn(message);
			}
			
		}
	}
	
    private CommandLineExecutionResponse changeFilePermissions(String script, String sourcePath, String artifactName) throws Exception {
		String parts[] = sourcePath.split("/");
		String user = parts[3];
		String artifactclassName = parts[5];

		List<String> setFilePermissionsCommandParamsList = new ArrayList<String>();
		setFilePermissionsCommandParamsList.add("sudo");
		setFilePermissionsCommandParamsList.add(script);
		setFilePermissionsCommandParamsList.add(user);
		setFilePermissionsCommandParamsList.add(artifactclassName);
		setFilePermissionsCommandParamsList.add(artifactName);

		CommandLineExecutionResponse setPermsCommandLineExecutionResponse = commandLineExecuter.executeCommand(setFilePermissionsCommandParamsList);
		return setPermsCommandLineExecutionResponse;
    }
    
	public String getExistingSeqCodeFromArtifactName(String folderName, String extractionRegex){
		String existingSeqCodeFromFolderName = null;
		
		Pattern p = Pattern.compile(extractionRegex);
		Matcher m = p.matcher(folderName);  		
		if(m.find())
			existingSeqCodeFromFolderName = m.group();
		return existingSeqCodeFromFolderName;
	}
	
	// All isha specific logic should go out...
	// TODO - make it generic - 
	// TODO - Also at the time of scanning compare against the seq code and err out
	private String getCustomArtifactName(String folderName, String prevSeqCode, Sequence sequence, boolean isKeepExtractedCode){
		String customFolderName = folderName;
		if(isKeepExtractedCode) { // For contentgroups where isKeepExtractedCode is true just use the prevSeqCode(seqId that was in the folder name originally)
			String folderNameToBe = null;
			String seqIdPrefix = null;
			if(StringUtils.isNotBlank(prevSeqCode)) {
				String prevSeqCodeStrippedfolderName = folderName.replace(prevSeqCode, "");
				folderNameToBe = prevSeqCodeStrippedfolderName;
				seqIdPrefix = prevSeqCode;
			}
			else {
				// if there isnt a prevSeqCode then use lastSequenceId
				folderNameToBe = folderName;
				Integer currentNumber = SequenceUtil.incrementCurrentNumber(sequence);
				seqIdPrefix = sequence.getPrefix() + currentNumber;
			}
			Matcher m = folderNameWithoutPrevSeqCodePattern.matcher(folderNameToBe); 	
			// ensure the code is followed by _. 
			if(m.find()) {
				String tailPartOfFolderNameAfterSeparator = m.group(2);
				customFolderName = seqIdPrefix + "_" + tailPartOfFolderNameAfterSeparator;
			}			
		}
		else { // For contentgroups where isKeepExtractedCode is false we just prefix the sequenceId to the original folder Name
//			if(sequence.getSequenceRef() != null)
//				sequence.getSequenceRef().incrementCurrentNumber();
//			else
//				sequence.incrementCurrentNumber();
			
//			Integer currentNumber = null; 
//			if(sequence.getSequenceRef() != null) {
//				sequence.getSequenceRef().incrementCurrentNumber();
//				currentNumber = sequence.getSequenceRef().getCurrrentNumber();
//			}
//			else {
//				sequence.incrementCurrentNumber();
//				currentNumber = sequence.getCurrrentNumber();
//			}
//			System.out.println(currentNumber);
			
			//customFolderName = sequence.getPrefix() + sequence.getLastSequenceNumber() + "_" + folderName;
			// Not showing the sequence id during scan...
			customFolderName = folderName;
		}	
		return customFolderName;		
	}
	
	/**
	 * 
	 * @param args[0] - The content Group seed json file
	 * @param args[1] - The scanFolderBase seed json file
	 * @param args[2] - artifactclassId from the contentgroupseed file used in args[0] of the specific contentgroup that need to be tested
	 * @param args[3] - The file containing entries of all ingestableDirectories from all scanfolderbases that has the contentgroup allowed. Mimicks the behaviour of iterating through all scanfolderbase and the list of files copied over to be backed up - signifies this is for testing and not iterating through the scanfolderbase list...
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		SourceDirScanner sds = new SourceDirScanner();
//		//ReflectionTestUtils sds
//		
//		String artifactclassSeedDataJsonFilePath = args[0]; 
//		String scanFolderBaseSeedDataJsonFilePath = args[1];
//		String artifactclassId = args[2];
//		String ingestableDirectoriesList4TestPath = args.length == 4 ? args[3] : null;
//
//		ObjectMapper mapper = new ObjectMapper(); 
//		mapper.enable(SerializationFeature.INDENT_OUTPUT); 
//		Iterable<Artifactclass> artifactclassList = mapper.readValue(new File(artifactclassSeedDataJsonFilePath), new TypeReference<Iterable<Artifactclass>>(){}); 
//		Iterable<ScanFolderBase> scanFolderBaseList = mapper.readValue(new File(scanFolderBaseSeedDataJsonFilePath), new TypeReference<Iterable<ScanFolderBase>>(){});
//
//		Artifactclass cg = null;
//		for (Artifactclass artifactclass : artifactclassList) {
//			if(artifactclass.getContentGroupId() == Integer.parseInt(artifactclassId))
//				cg = artifactclass;
//		}
//		
//		List<FileDetails>  ingestDirectoryList = null;
//		if(ingestableDirectoriesList4TestPath != null) {
//			List<String> entriesFromFile = FileUtils.readLines(new File(ingestableDirectoriesList4TestPath));
//			
//			File[] ingestableDirectories = new File[entriesFromFile.size()];
//			int cnt = 0;
//			for (Iterator<String> iterator = entriesFromFile.iterator(); iterator.hasNext();) {
//				String nthDirPath = (String) iterator.next();
//				ingestableDirectories[cnt] = new File(nthDirPath);
//				cnt += 1;
//			}
//			
//			// This mimicks the sequenceNumberHelper.getPrefixedLastSequenceId(artifactclassId);
//			String seqId = null;
//			Sequence sequence = new Sequence();
//			sequence.incrementLastNumber();
//
//			if(cg.getSequenceNumberId() == 1)
//				sequence.setPrefix("");
//			else if(cg.getSequenceNumberId() == 2)
//				sequence.setPrefix("A");
//			else if(cg.getSequenceNumberId() == 3)
//				sequence.setPrefix("Z");
//			else
//				sequence.setPrefix("DV");
//			
//			
//			ingestDirectoryList = sds.scanForIngestableFiles(sequence, cg.getRegex(), cg.isKeepExtractedCode(), "NeedsEffort-NotTheTestingGoal", ingestableDirectories);
//		}
//		else {
//			ingestDirectoryList = sds.scanSourceDir(cg, scanFolderBaseList);
//		}
//		
//		FileUtils.write(new File(ingestableDirectoriesList4TestPath + ".out"), mapper.writeValueAsString(ingestDirectoryList));	
//		System.out.println("Completed... Result File is here :: " + ingestableDirectoriesList4TestPath + ".out");
	}

}
