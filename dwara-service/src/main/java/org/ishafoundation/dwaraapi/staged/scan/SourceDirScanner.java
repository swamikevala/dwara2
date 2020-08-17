package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
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
    private Configuration configuration;
	
    private Pattern folderNameWithoutPrevSeqCodePattern = Pattern.compile("([_-]?)(.*)");

    private String DELETED = "deleted";
	
    private String regexAllowedChrsInFileName = null;
	Pattern allowedChrsInFileNamePattern = null;
	@PostConstruct
	private void loadConfigEntries() {
		regexAllowedChrsInFileName = configuration.getRegexAllowedChrsInFileName();
		allowedChrsInFileNamePattern = Pattern.compile(regexAllowedChrsInFileName);
	}
	
	public List<StagedFileDetails> scanSourceDir(Artifactclass artifactclass, List<String> scanFolderBasePathList) {
		//int libraryclassId = artifactclass.getId();
    	String libraryclassName = artifactclass.getName();
        int sequenceId = artifactclass.getSequenceId(); // getting the primary key of the Sequence table which holds the lastsequencenumber for this group...
        Sequence sequence = null;
        try {
        	sequence = sequenceDao.findById(sequenceId).get();
        }catch (Exception e) {
        	logger.error("Missing sequence table row for " + sequenceId);
        	return null;
		}
    	String extractionRegex = sequence.getArtifactExtractionRegex();
    	boolean isKeepExtractedCode = sequence.isArtifactKeepCode();
    	
        List<StagedFileDetails> ingestReadyFileList = new ArrayList<StagedFileDetails>();
    	for (String nthScanFolderBasePath : scanFolderBasePathList) {
			String sourcePath = nthScanFolderBasePath + File.separator + Action.ingest.name() + File.separator + libraryclassName;
			
			IOFileFilter dirFilter = FileFilterUtils.directoryFileFilter();
			IOFileFilter notCancelledFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(Status.cancelled.toString()));
			IOFileFilter notDeletedFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(DELETED));
			
			//FileFilter allDirectoriesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(dirFilter, notCancelledFolderFilter, notDeletedFolderFilter);
			FileFilter allFilesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(dirFilter, notCancelledFolderFilter, notDeletedFolderFilter);
			if(libraryclassName.contains("audio"))
				allFilesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(notCancelledFolderFilter, notDeletedFolderFilter);
	    	File[] ingestableFiles = new File(sourcePath).listFiles(allFilesExcludingCancelledAndDeletedDirectoryFilter);
	    	if(ingestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseIngestDirectoryList = scanForIngestableFiles(sequence, extractionRegex, isKeepExtractedCode, sourcePath, ingestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseIngestDirectoryList);
	    	}
	    	
	    	// All cancelled medialibrary...
	    	String cancelledOriginSourceDir = sourcePath + File.separator + Status.cancelled.toString();
	    	File[] cancelledIngestableFiles = new File(cancelledOriginSourceDir).listFiles();
	    	if(cancelledIngestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseCancelledDirectoryIngestFileList = scanForIngestableFiles(sequence, extractionRegex, isKeepExtractedCode, cancelledOriginSourceDir, cancelledIngestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseCancelledDirectoryIngestFileList);
	    	}	
	    	
	    	// All deleted medialibrary...
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
        String warning = null;
        int fileCount = 0;
        
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
		
		String fileName = nthIngestableFile.getName();
		if(fileName.length() > 150)
			warning = (StringUtils.isNotBlank(warning) ? warning + " & " : "" ) + "File Name gt 150 characters";
		
		Matcher m = allowedChrsInFileNamePattern.matcher(fileName);
		if(!m.matches())
			warning = (StringUtils.isNotBlank(warning) ? warning + " & " : "" ) + "File Name contains spl characters";
        
		String prevSeqCode = null;
		if(StringUtils.isNotBlank(extractionRegex)) {
			prevSeqCode = getExistingSeqCodeFromLibraryName(fileName, extractionRegex);
		}
		// TODO : Talk to swami - we still need old filename and new filename...
		String customFileName = getCustomLibraryName(fileName, prevSeqCode, sequence, isKeepExtractedCode);
		
		StagedFileDetails nthIngestFile = new StagedFileDetails();
		
		nthIngestFile.setPath(sourcePath);
		nthIngestFile.setFileCount(fileCount);
		nthIngestFile.setTotalSize(size);
		//nthIngestDirectory.setFileSizeInMBytes(totalSizeInMB);
		nthIngestFile.setPrevSequenceCode(prevSeqCode);
		if(StringUtils.isNotBlank(extractionRegex) && isKeepExtractedCode) // if regex present and useExtractCode is true but prevSeqCode is null then throw error...
			nthIngestFile.setPrevSequenceCodeExpected(true);
		nthIngestFile.setName(fileName);
		if(!fileName.equals(customFileName))
			nthIngestFile.setSuggestedName(customFileName);
		String errorType = null;
		if(warning != null)
			errorType = "Warning";// TODO hardcoded for now - fetch it from error type table...
		nthIngestFile.setErrorType(errorType);
		nthIngestFile.setErrorMessage(warning);
		return nthIngestFile;
	}
	
	public String getExistingSeqCodeFromLibraryName(String folderName, String extractionRegex){
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
	private String getCustomLibraryName(String folderName, String prevSeqCode, Sequence sequence, boolean isKeepExtractedCode){
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
	 * @param args[2] - libraryclassId from the contentgroupseed file used in args[0] of the specific contentgroup that need to be tested
	 * @param args[3] - The file containing entries of all ingestableDirectories from all scanfolderbases that has the contentgroup allowed. Mimicks the behaviour of iterating through all scanfolderbase and the list of files copied over to be backed up - signifies this is for testing and not iterating through the scanfolderbase list...
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		SourceDirScanner sds = new SourceDirScanner();
//		//ReflectionTestUtils sds
//		
//		String libraryclassSeedDataJsonFilePath = args[0]; 
//		String scanFolderBaseSeedDataJsonFilePath = args[1];
//		String libraryclassId = args[2];
//		String ingestableDirectoriesList4TestPath = args.length == 4 ? args[3] : null;
//
//		ObjectMapper mapper = new ObjectMapper(); 
//		mapper.enable(SerializationFeature.INDENT_OUTPUT); 
//		Iterable<Artifactclass> libraryclassList = mapper.readValue(new File(libraryclassSeedDataJsonFilePath), new TypeReference<Iterable<Artifactclass>>(){}); 
//		Iterable<ScanFolderBase> scanFolderBaseList = mapper.readValue(new File(scanFolderBaseSeedDataJsonFilePath), new TypeReference<Iterable<ScanFolderBase>>(){});
//
//		Artifactclass cg = null;
//		for (Artifactclass artifactclass : libraryclassList) {
//			if(artifactclass.getContentGroupId() == Integer.parseInt(libraryclassId))
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
//			// This mimicks the sequenceNumberHelper.getPrefixedLastSequenceId(libraryclassId);
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