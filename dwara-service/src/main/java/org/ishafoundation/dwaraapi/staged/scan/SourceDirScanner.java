package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
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
    private IStagedFileValidator stagedFileValidator;


	private Pattern folderNameWithoutPrevSeqCodePattern = Pattern.compile("([_-]?)(.*)");
    private String DELETED = "deleted";
	
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

		nthIngestFile.setErrors(stagedFileValidator.validate(nthIngestFile));
		
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
		else { // For contentgroups where isKeepExtractedCode is false we use the original folder Name
			customFolderName = folderName;
		}	
		return customFolderName;		
	}

	private String getExistingSeqCodeFromArtifactName(String folderName, String extractionRegex){
		String existingSeqCodeFromFolderName = null;
		
		Pattern p = Pattern.compile(extractionRegex);
		Matcher m = p.matcher(folderName);  		
		if(m.find())
			existingSeqCodeFromFolderName = m.group();
		return existingSeqCodeFromFolderName;
	}
}
