package org.ishafoundation.dwaraapi.staged.scan;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.ishafoundation.dwaraapi.api.resp.staged.scan.StagedFileDetails;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.SequenceUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SourceDirScanner {
    
	private static final Logger logger = LoggerFactory.getLogger(SourceDirScanner.class);
	
	@Autowired
	protected SequenceUtil sequenceUtil;
	
	@Autowired
    private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
    private StagedFileEvaluator stagedFileEvaluator;
	
//	private Pattern folderNameWithoutPrevSeqCodePattern = Pattern.compile("([_-]?)(.*)");
    private String DELETED = "deleted";
	
	public List<StagedFileDetails> scanSourceDir(Artifactclass artifactclass, List<String> scanFolderBasePathList) {
		//int artifactclassId = artifactclass.getId();
    	String artifactclassName = artifactclass.getId();
    	Domain domain = artifactclass.getDomain();
        String sequenceId = artifactclass.getSequenceId(); // getting the primary key of the Sequence table which holds the lastsequencenumber for this group...
        Sequence sequence = configurationTablesUtil.getSequence(sequenceId);
        if(sequence == null) {
        	logger.error("Missing sequence table row for " + sequenceId);
        	return null;
		}
    	
        List<StagedFileDetails> ingestReadyFileList = new ArrayList<StagedFileDetails>();
    	for (String nthScanFolderBasePath : scanFolderBasePathList) {
			String sourcePath = nthScanFolderBasePath + File.separator + Action.ingest.name() + File.separator + artifactclassName;
			
			IOFileFilter notCancelledFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(Status.cancelled.toString()));
			IOFileFilter notDeletedFolderFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(DELETED));
			
			FileFilter allFilesExcludingCancelledAndDeletedDirectoryFilter = FileFilterUtils.and(notCancelledFolderFilter, notDeletedFolderFilter);//FileFilterUtils.and(dirFilter, notCancelledFolderFilter, notDeletedFolderFilter);
	    	File[] ingestableFiles = new File(sourcePath).listFiles(allFilesExcludingCancelledAndDeletedDirectoryFilter);
	    	if(ingestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseIngestDirectoryList = scanForIngestableFiles(domain, sequence, sourcePath, ingestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseIngestDirectoryList);
	    	}
	    	
	    	// All cancelled artifact...
	    	String cancelledOriginSourceDir = sourcePath + File.separator + Status.cancelled.toString();
	    	File[] cancelledIngestableFiles = new File(cancelledOriginSourceDir).listFiles();
	    	if(cancelledIngestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseCancelledDirectoryIngestFileList = scanForIngestableFiles(domain, sequence, cancelledOriginSourceDir, cancelledIngestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseCancelledDirectoryIngestFileList);
	    	}	
	    	
	    	// All deleted artifact...
	    	String deletedOriginSourceDir = sourcePath + File.separator + DELETED;
	    	File[] deletedIngestableFiles = new File(deletedOriginSourceDir).listFiles();
	    	if(deletedIngestableFiles != null) {
	    		List<StagedFileDetails> nthScanFolderBaseDeletedDirectoryIngestFileList = scanForIngestableFiles(domain, sequence, deletedOriginSourceDir, deletedIngestableFiles);
	    		ingestReadyFileList.addAll(nthScanFolderBaseDeletedDirectoryIngestFileList);
	    	}		    	
		}
		
		return ingestReadyFileList;
	}
	
	private List<StagedFileDetails> scanForIngestableFiles(Domain domain, Sequence sequence, String sourcePath, File[] ingestableFiles) {
		
		List<StagedFileDetails> ingestFileList = new ArrayList<StagedFileDetails>();
    	for (int i = 0; i < ingestableFiles.length; i++) {
			File nthIngestableFile = ingestableFiles[i];
			StagedFileDetails nthIngestFile = stagedFileEvaluator.evaluateAndGetDetails(domain, sequence, sourcePath, nthIngestableFile);
			ingestFileList.add(nthIngestFile);
		}
    	return ingestFileList;
	}

	// All isha specific logic should go out...
	// TODO - make it generic - 
	// TODO - Also at the time of scanning compare against the seq code and err out
//	private String getCustomArtifactName(String folderName, String prevSeqCode, Sequence sequence, boolean isKeepExtractedCode){
//		String customFolderName = folderName;
//		if(isKeepExtractedCode) { // For contentgroups where isKeepExtractedCode is true just use the prevSeqCode(seqId that was in the folder name originally)
//			String folderNameToBe = null;
//			String seqIdPrefix = null;
//			if(StringUtils.isNotBlank(prevSeqCode)) {
//				String prevSeqCodeStrippedfolderName = folderName.replace(prevSeqCode, "");
//				folderNameToBe = prevSeqCodeStrippedfolderName;
//				seqIdPrefix = prevSeqCode;
//			}
//			else {
//				// if there isnt a prevSeqCode then use lastSequenceId
//				folderNameToBe = folderName;
//				Integer currentNumber = SequenceUtil.incrementCurrentNumber(sequence);
//				seqIdPrefix = sequence.getPrefix() + currentNumber;
//			}
//			Matcher m = folderNameWithoutPrevSeqCodePattern.matcher(folderNameToBe); 	
//			// ensure the code is followed by _. 
//			if(m.find()) {
//				String tailPartOfFolderNameAfterSeparator = m.group(2);
//				customFolderName = seqIdPrefix + "_" + tailPartOfFolderNameAfterSeparator;
//			}			
//		}
//		else { // For contentgroups where isKeepExtractedCode is false we use the original folder Name
//			customFolderName = folderName;
//		}	
//		return customFolderName;		
//	}
}
