package org.ishafoundation.dwaraapi.storage.storagetype;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.DestinationDao;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.TFileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.ArtifactVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.FileVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.TFileVolumeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.TFile;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.json.ArtifactVolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.ArtifactVolumeStatus;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.job.JobCreator;
import org.ishafoundation.dwaraapi.storage.StorageResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.model.DiskJob;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagelevel.IStoragelevel;
import org.ishafoundation.dwaraapi.storage.storagelevel.block.label.LabelManager;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.VolumeFinalizer;
import org.ishafoundation.dwaraapi.utils.VolumeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractStoragetypeJobProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractStoragetypeJobProcessor.class);
	
	@Autowired
	private JobDao jobDao; 

	@Autowired
	private VolumeDao volumeDao;

	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
	private DestinationDao destinationDao;
	
	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private TFileDao tFileDao;
	
	@Autowired
	private TFileVolumeDao tFileVolumeDao;
	
	@Autowired
	private Map<String, IStoragelevel> storagelevelMap;
		
	@Autowired
	private ArtifactVolumeDao artifactVolumeDao;
	
	@Autowired
	private FileVolumeDao fileVolumeDao;
	
	@Autowired
	private VolumeUtil volumeUtil;
	
	@Autowired
	private VolumeFinalizer volumeFinalizer;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private LabelManager labelManager;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private CommandLineExecuter commandLineExecutor;
	
	public AbstractStoragetypeJobProcessor() {
		logger.debug(this.getClass().getName());
	}
	
    protected void beforeInitialize(SelectedStorageJob selectedStorageJob) throws Exception {
    	
    	
    }
    
	public StorageResponse initialize(SelectedStorageJob selectedStorageJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeInitialize(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.initialize(selectedStorageJob);
    	
		afterInitialize(selectedStorageJob);

		return storageResponse; 
    }
	
	protected void afterInitialize(SelectedStorageJob selectedStorageJob) {
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		Volume volume = storageJob.getVolume();
		volume = volumeDao.save(volume);
		logger.trace("Volume " + volume.getId() + " attached to dwara succesfully");

		Sequence sequence = volume.getGroupRef().getSequence(); 
		sequence.incrementCurrentNumber(); //setCurrrentNumber(sequence.getCurrrentNumber() + 1);
		Sequence updatedSequence = sequenceDao.save(sequence);
		logger.trace(sequence.getId() + " currentNumber updated to " + updatedSequence.getCurrrentNumber());
		
		Job job = storageJob.getJob();
		job.setVolume(volume);
		job.setGroupVolume(volume.getGroupRef());
		jobDao.save(job);
		logger.trace("Job " + job.getId() + " updated with the formatted Volume " + volume.getId() + " succesfully");
	}

    public StorageResponse copy(SelectedStorageJob selectedStorageJob) throws Throwable{
    	logger.info("Copying job " + selectedStorageJob.getStorageJob().getJob().getId());
    	StorageResponse storageResponse = null;
//    	beforeWrite(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.copy(selectedStorageJob);

//    	afterWrite(selectedStorageJob, storageResponse);
    	return storageResponse; 
    }
    
    protected void beforeWrite(SelectedStorageJob selectedStorageJob) throws Exception {
    	
    	labelManager.writeArtifactLabelTemporarilyOnDisk(selectedStorageJob);
    }
    
    public StorageResponse write(SelectedStorageJob selectedStorageJob) throws Throwable{
    	logger.info("Writing job " + selectedStorageJob.getStorageJob().getJob().getId());
    	StorageResponse storageResponse = null;
    	beforeWrite(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.write(selectedStorageJob);

    	afterWrite(selectedStorageJob, storageResponse);
    	return storageResponse; 
    }
    
    /**
     * We had to convert filePathName String to Hex while comparing tarResponse vs DB.filePathname to support non-ascii characters like emojis, carriage return(\r char) etc.
     * Assuming when non-ascii characters 
     * 1) like emoji are used in the filename that the characters are output in octal (escaped by a "\") - for eg., tar responds with \360\237\231\217 for folded hands/namaskar emoji
     * 2) like carriage return - tar responds with \r
     * @param filepathName
     * @return
     */
	private static String getHexString(String filepathName) throws Exception {
		StringBuffer filepathNameHexSB = new StringBuffer();
		for(int i = 0; i < filepathName.length(); i++){
			char nthChar = filepathName.charAt(i);
			String hexString = null;
			if(String.valueOf(nthChar).equals("\\")) {
				try {
					char[] dst = new char[3];	
					filepathName.getChars(i+1, i+4, dst, 0);
					hexString = Integer.toHexString(Integer.parseInt(String.valueOf(dst),8));
					i = i +3;
				}catch (Exception e) {
					char[] dst = new char[2];	
					filepathName.getChars(i, i+2, dst, 0);
					String cntrlChar = String.valueOf(dst);
					if(cntrlChar.equals("\\r"))
						hexString = "0D".toLowerCase();
					else if(cntrlChar.equals("\\n"))
						hexString = "0A".toLowerCase();
					else 
						throw new Exception("Control char " + cntrlChar + " not supported - " + filepathName);
					i = i +1;
				}
			}else {
				hexString = Integer.toHexString(nthChar);
			}
			filepathNameHexSB.append(hexString);
		}
		return filepathNameHexSB.toString();
	}
    
    protected void afterWrite(SelectedStorageJob selectedStorageJob, StorageResponse storageResponse) throws Exception {
		List<ArchivedFile> archivedFileList = null;

    	StorageJob storagejob = selectedStorageJob.getStorageJob();
    	
		Artifact artifact = storagejob.getArtifact();
		int artifactId = artifact.getId();
		
		Volume volume = storagejob.getVolume();
		
		// Get a map of Paths and their File object
		HashMap<String, ArchivedFile> filePathNameHexToArchivedFileObj = new LinkedHashMap<String, ArchivedFile>();
		if(volume.getStoragelevel() == Storagelevel.block) { //could use if(storageResponse != null && storageResponse.getArchiveResponse() != null) { but archive and block are NOT mutually exclusive
			archivedFileList = storageResponse.getArchiveResponse().getArchivedFileList();
			for (Iterator<ArchivedFile> iterator = archivedFileList.iterator(); iterator.hasNext();) {
				ArchivedFile archivedFile = (ArchivedFile) iterator.next();
				String filePathName = archivedFile.getFilePathName();
				String filePathNameHex = null;
				try {
					filePathNameHex = getHexString(filePathName);
				}catch (Exception e) {
					logger.error("Not able to hex " + filePathName, e);
					throw e;
				}
				logger.trace("AFList - " + filePathName + ":" + filePathNameHex);
				filePathNameHexToArchivedFileObj.put(filePathNameHex, archivedFile);
			}
		}
		
		List<TFile> artifactTFileList = tFileDao.findAllByArtifactIdAndDeletedIsFalse(artifact.getId());
		HashMap<String, TFile> filePathNameToTFileObj = new LinkedHashMap<String, TFile>();
		for (TFile tFile : artifactTFileList) {
			filePathNameToTFileObj.put(tFile.getPathname(), tFile);
		}
		List<TFileVolume> toBeAddedTFileVolumeTableEntries = new ArrayList<TFileVolume>();
		for (Iterator<TFile> iterator = artifactTFileList.iterator(); iterator.hasNext();) {
			TFile nthTFile = iterator.next();
			String filePathname = FilenameUtils.separatorsToUnix(nthTFile.getPathname());
			String filePathNameHex = Hex.encodeHexString(filePathname.getBytes());
			logger.trace("Tfile - " + filePathname + ":" + filePathNameHex);
			TFileVolume tfileVolume = new TFileVolume(nthTFile.getId(), volume);
			ArchivedFile archivedFile = filePathNameHexToArchivedFileObj.get(filePathNameHex);
			//logger.info(archivedFile.toString());
			if(archivedFile != null) { // if(volume.getStoragelevel() == Storagelevel.block) { - need to check if the file is archived anyway even if its block, so going with the archivedFile check alone
				Integer volumeBlock = archivedFile.getVolumeBlock();
				tfileVolume.setVolumeStartBlock(volumeBlock);
				tfileVolume.setArchiveBlock(archivedFile.getArchiveBlock());
				if(archivedFile.getLinkName() != null && (nthTFile.getSymlinkPath() == null && nthTFile.getSymlinkFileId() == null)) { // only hard links and no soft links
					TFile tFile = filePathNameToTFileObj.get(archivedFile.getLinkName());
					if(tFile != null) // if a link is internally referencing link
						tfileVolume.setHardlinkFileId(tFile.getId());
				}
			}
			toBeAddedTFileVolumeTableEntries.add(tfileVolume); // Should we add null entries...
		}
		
	    if(toBeAddedTFileVolumeTableEntries.size() > 0) {
	    	tFileVolumeDao.saveAll(toBeAddedTFileVolumeTableEntries);
	    	logger.info("TFileVolume records created successfully");
	    }
		
		List<File> artifactFileList = fileDao.findAllByArtifactIdAndDeletedFalse(artifact.getId());
		HashMap<String, File> filePathNameToFileObj = new LinkedHashMap<String, File>();
		for (File file : artifactFileList) {
			filePathNameToFileObj.put(file.getPathname(), file);
		}
		// NOTE: We need filevolume entries even when response from storage layer is null(Only archiveformats return the file breakup storage details... Other non archive writes dont...)
		// So we need to iterate on the files than on the archived file response...
		// OBSERVATION: The written file order on volume and the listed file varies...
		Integer artifactStartVolumeBlock = null;
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		for (Iterator<File> iterator = artifactFileList.iterator(); iterator.hasNext();) {
			File nthFile = iterator.next();
			String filePathname = FilenameUtils.separatorsToUnix(nthFile.getPathname());
			String filePathNameHex = Hex.encodeHexString(filePathname.getBytes());
			logger.trace("File - " + filePathname + ":" + filePathNameHex);
//			if(nthFile.getChecksum() != null && StringUtils.isNotBlank(FilenameUtils.getExtension(filePathname))) { //if file is not folder
//				String readyToIngestPath =  "C:\\data\\ingested"; // TODO Hardcoded
//				java.io.File file = new java.io.File(readyToIngestPath + java.io.File.separator + nthFile.getPathname());
//				nthFile.setChecksum(Md5Util.getChecksum(file, volume.getChecksumtype()));
//			}
			
			FileVolume fileVolume = new FileVolume(nthFile.getId(), volume);// lets just let users use the util consistently
			
			// TODO
			//fileVolume.setVerifiedAt(verifiedAt);
			//fileVolume.setEncrypted(encrypted);

			ArchivedFile archivedFile = filePathNameHexToArchivedFileObj.get(filePathNameHex);
			if(archivedFile != null) { // if(volume.getStoragelevel() == Storagelevel.block) { - need to check if the file is archived anyway even if its block, so going with the archivedFile check alone
				Integer volumeBlock = archivedFile.getVolumeBlock();
				if(volumeBlock == null) {
					// need to look the previous job's last file end block and append it with current job's - volumeBlock = 
				}
				if(filePathname.equals(artifact.getName())) {
					artifactStartVolumeBlock = volumeBlock;
				}
				fileVolume.setVolumeStartBlock(volumeBlock);
				fileVolume.setArchiveBlock(archivedFile.getArchiveBlock());
				if(archivedFile.getLinkName() != null && (nthFile.getSymlinkPath() == null && nthFile.getSymlinkFileId() == null)) { // only hard links and no soft links
					File file = filePathNameToFileObj.get(archivedFile.getLinkName());
					if(file != null) // if a link is internally referencing link
						fileVolume.setHardlinkFileId(file.getId());
				}
			}
			toBeAddedFileVolumeTableEntries.add(fileVolume); // Should we add null entries...
			// TODO Should we report if archivedFile == null, file not archived...
		}
		
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	fileVolumeDao.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.info("FileVolume records created successfully");
	    }
	    
	    ArtifactVolume artifactVolume = new ArtifactVolume(artifact.getId(), volume); // lets just let users use the util consistently
	    artifactVolume.setName(artifact.getName());
	    artifactVolume.setJob(storagejob.getJob());
	    artifactVolume.setStatus(ArtifactVolumeStatus.current);
	    if(volume.getStoragelevel() == Storagelevel.block) {
		    ArtifactVolumeDetails artifactVolumeDetails = new ArtifactVolumeDetails();
		    
		    ArchiveResponse archiveResponse = storageResponse.getArchiveResponse();
			String archiveId = archiveResponse.getArchiveId();// For tar it will not be available...;
			artifactStartVolumeBlock = archiveResponse.getArtifactStartVolumeBlock();
		    Integer artifactEndVolumeBlock = archiveResponse.getArtifactEndVolumeBlock();
		    artifactVolumeDetails.setArchiveId(archiveId);
		    artifactVolumeDetails.setStartVolumeBlock(artifactStartVolumeBlock);
		    artifactVolumeDetails.setEndVolumeBlock(artifactEndVolumeBlock);
		    
		    artifactVolume.setDetails(artifactVolumeDetails);
	    }
	    artifactVolume = artifactVolumeDao.save(artifactVolume);
	    long usedCapacity = 0L;
	    if(volume.getStoragelevel() == Storagelevel.block) {
			selectedStorageJob.setArtifactStartVolumeBlock(artifactVolume.getDetails().getStartVolumeBlock());
			selectedStorageJob.setArtifactEndVolumeBlock(artifactVolume.getDetails().getEndVolumeBlock());
			
			logger.info("ArtifactVolume - " + artifactVolume.getId().getArtifactId() + " " + artifactVolume.getName() + " " + artifactVolume.getId().getVolumeId() + " " + artifactVolume.getDetails().getStartVolumeBlock() + " " + artifactVolume.getDetails().getEndVolumeBlock());
	    	int lastArtifactOnVolumeEndVolumeBlock = artifactVolume.getDetails().getEndVolumeBlock();
	    	logger.trace("lastArtifactOnVolumeEndVolumeBlock " + lastArtifactOnVolumeEndVolumeBlock);
	    	logger.trace("volume.getDetails().getBlocksize() - " + volume.getDetails().getBlocksize());
	    	usedCapacity = (long) volume.getDetails().getBlocksize() * lastArtifactOnVolumeEndVolumeBlock;
	    }else {
	    	if(volume.getStoragetype() == Storagetype.disk) {
	    		DiskJob diskJob = (DiskJob) selectedStorageJob;
	    		if(System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
	    			Path destDiskpath = Paths.get(diskJob.getMountPoint(), volume.getId());
	    			try {
	    				CommandLineExecutionResponse cler = commandLineExecutor.executeCommand("diskutil info " + destDiskpath.toString());
	    				if(cler.isComplete())
	    					usedCapacity = getUsedSpace(cler.getStdOutResponse());
	    			}catch (Exception e) {
						// TODO: handle exception
					}
	    		}else
	    			throw new Exception("OS not supported to find used space...");
	    	}
	    	
	    	
	    	
	    }
    	logger.trace("usedCapacity - " + usedCapacity);
		volume.setUsedCapacity(usedCapacity);
		volumeDao.save(volume);

		labelManager.writeArtifactLabel(selectedStorageJob);

    	boolean isVolumeNeedToBeFinalized = volumeUtil.isVolumeNeedToBeFinalized(volume, usedCapacity);
    	if(isVolumeNeedToBeFinalized) {
    		logger.info("Triggering a finalization request for volume - " + volume.getId());
    		
    		volumeFinalizer.finalize(volume.getId(), DwaraConstants.SYSTEM_USER_NAME);
    	}
    }
    
    // TODO - Move this to Disk specific utility
    private String usedSpaceRegex = "(.*) Used Space:(.*)\\((.*) Bytes\\)";
    private String totalSpaceRegex = "(.*) Total Space:(.*)\\((.*) Bytes\\)";
    private String freeSpaceRegex = "(.*) Free Space:(.*)\\((.*) Bytes\\)";
    
    private Pattern usedSpaceRegexPattern = Pattern.compile(usedSpaceRegex);
    private Pattern totalSpaceRegexPattern = Pattern.compile(totalSpaceRegex);
    private Pattern freeSpaceRegexPattern = Pattern.compile(freeSpaceRegex);

    private long getUsedSpace(String response) {
    	Long usedSpace = 0L;
		Matcher m = usedSpaceRegexPattern.matcher(response);
		if(m.find()) {
			usedSpace = Long.parseLong(m.group(3));
		}else {
			m = totalSpaceRegexPattern.matcher(response);
			
			if(m.find()) {
				long totalSpace = Long.parseLong(m.group(3));
				m = freeSpaceRegexPattern.matcher(response);
				if(m.find()) {
					long freeSpace = Long.parseLong(m.group(3));
					usedSpace = totalSpace - freeSpace;
				}
			}
		}
				
    	return usedSpace;
    }
    private void updateFileVolumeTable(SelectedStorageJob selectedStorageJob, StorageResponse storageResponse) {
    	StorageJob storageJob = selectedStorageJob.getStorageJob();
	
		Request request = storageJob.getJob().getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
		
		Volume volume = storageJob.getVolume();
		List<FileVolume> toBeAddedFileVolumeTableEntries = new ArrayList<FileVolume>();
		
		org.ishafoundation.dwaraapi.db.model.transactional.File fileToBeRestored = selectedStorageJob.getFile();
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList = selectedStorageJob.getArtifactFileList();
		for (File nthFile : fileList) {
			String filePathname = nthFile.getPathname();
			if(filePathname.startsWith(fileToBeRestored.getPathname())) {
				FileVolume fileVolume = fileVolumeDao.findByIdFileIdAndIdVolumeId(nthFile.getId(), volume.getId());

				//if(requestedAction == Action.restore_process && DwaraConstants.RESTORE_AND_VERIFY_FLOW_NAME.equals(request.getDetails().getFlowName())) // called during normal restore with verify option
					//fileVolume.setVerifiedAt(LocalDateTime.now());
				
				if(requestedAction == Action.ingest && volume.getStoragelevel() == Storagelevel.block) {// && storageJob.getJob().getStoragetaskActionId() == Action.verify) { // Only for block - TODO Should we move this to block specific impl??? 
					Map<String, Integer> archivedFilePathNameToHeaderBlockCnt = storageResponse.getArchiveResponse().getArchivedFilePathNameToHeaderBlockCnt();
					if(archivedFilePathNameToHeaderBlockCnt != null) {
						Integer headerBlockCnt = archivedFilePathNameToHeaderBlockCnt.get(filePathname);
						if(headerBlockCnt != null)
							fileVolume.setHeaderBlocks(headerBlockCnt);
					}
				}
				toBeAddedFileVolumeTableEntries.add(fileVolume);
			}
		}
	    if(toBeAddedFileVolumeTableEntries.size() > 0) {
	    	fileVolumeDao.saveAll(toBeAddedFileVolumeTableEntries);
	    	logger.info("FileVolume records updated with headerblock details successfully");
	    }
	}
	
    protected void beforeFinalize(SelectedStorageJob selectedStorageJob) throws Exception {}
    
	public StorageResponse finalize(SelectedStorageJob selectedStorageJob) throws Throwable{
		StorageResponse storageResponse = null;
    	beforeFinalize(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.finalize(selectedStorageJob);

    	afterFinalize(selectedStorageJob);
    	return storageResponse; 
    }
	
	protected void afterFinalize(SelectedStorageJob selectedStorageJob) {
		Volume volume = selectedStorageJob.getStorageJob().getVolume();
		volume.setFinalized(true);
		volume.setFinalizedAt(LocalDateTime.now());
		volumeDao.save(volume);
		logger.trace("Volume " + volume.getId() + " finalized succesfully");
		
		/* commenting as we have to capture the archive/volume_end_block for easier restores...
		List<TFileVolume> toBeDeletedTFileVolumeTableEntries = tFileVolumeDao.findAllByIdVolumeId(volume.getId());
		List<Integer> tFileIdList = new ArrayList<Integer>();
		for (TFileVolume tFileVolume : toBeDeletedTFileVolumeTableEntries) {
			int tFileId = tFileVolume.getId().getFileId();
			tFileIdList.add(tFileId);
		}
		tFileVolumeDao.deleteAll(toBeDeletedTFileVolumeTableEntries);
		logger.trace("TFileVolume entries for " + volume.getId() + " deleted succesfully");
		
		// Delete TFile entries only when no file entries are there for any of the volume
		List<TFileVolume> tFileVolumeTableEntries = tFileVolumeDao.findAllByIdFileId(tFileIdList.get(0));
		if(tFileVolumeTableEntries == null || tFileVolumeTableEntries.size() == 0) {
			for (Integer nthTFileId : tFileIdList) {
				tFileDao.deleteById(nthTFileId);	
			}
			logger.trace("All TFile entries deleted succesfully");
		}
		*/
	}


    protected void beforeRestore(SelectedStorageJob selectedStorageJob) throws Exception {
    	StorageJob storageJob = selectedStorageJob.getStorageJob();
    	//storageJob.setTargetLocationPath(storageJob.getTargetLocationPath() + java.io.File.separator + configuration.getRestoreInProgressFileIdentifier());
    	int fileIdToBeRestored = storageJob.getFileId();
		
		org.ishafoundation.dwaraapi.db.model.transactional.File file = fileDao.findById(fileIdToBeRestored).get();
		selectedStorageJob.setFile(file);
		
		// TODO : Not sure if we need to pass the destination id or path -- Destination destination = configurationTablesUtil.getDestination(storageJob.getDestination());
		String destPath = storageJob.getDestinationPath();
		if(destPath != null) {
			Destination destination = destinationDao.findByPath(destPath);
			selectedStorageJob.setUseBuffering(destination.isUseBuffering());
		}
		
    	Artifact artifact = file.getArtifact(); 
    	storageJob.getJob().setInputArtifactId(artifact.getId());
		storageJob.setArtifact(artifact);
		
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList = fileDao.findAllByArtifactIdAndDeletedFalse(artifact.getId());
		selectedStorageJob.setArtifactFileList(fileList);
		selectedStorageJob.setFilePathNameToChecksum(getSourceFilesChecksum(fileList));

		Volume volume = storageJob.getVolume();
		
		ArtifactVolume artifactVolume = artifactVolumeDao.findByIdArtifactIdAndIdVolumeId(artifact.getId(), volume.getId());
		selectedStorageJob.setArtifactVolume(artifactVolume);

		String filePathNameToBeRestored = file.getPathname();
		
		// If artifactName is renamed then use the artifactNameOnTape (from artifactVolume.getName()) rather than the artifactName on file.getPathname();
		String artifactName = artifact.getName();
		String artifactNameOnVolume = artifactVolume.getName();
		if(!artifactNameOnVolume.equals(artifactName)) {
			filePathNameToBeRestored = filePathNameToBeRestored.replace(artifactName, artifactNameOnVolume);
		}
		selectedStorageJob.setFilePathNameToBeRestored(filePathNameToBeRestored);
    }
    
	private HashMap<String, byte[]> getSourceFilesChecksum(List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList){
		// caching the source file' checksum...
		HashMap<String, byte[]> filePathNameToChecksumObj = new LinkedHashMap<String, byte[]>();
		for (org.ishafoundation.dwaraapi.db.model.transactional.File nthFile : fileList) {
			String filePathName = nthFile.getPathname();
			byte[] checksum = nthFile.getChecksum();
			filePathNameToChecksumObj.put(filePathName, checksum);
		}
		return filePathNameToChecksumObj;
	}
    
	public StorageResponse restore(SelectedStorageJob selectedStorageJob) throws Throwable{
		logger.info("Restoring job " + selectedStorageJob.getStorageJob().getJob().getId());
		StorageResponse storageResponse = null;
    	beforeRestore(selectedStorageJob);
    	
    	IStoragelevel iStoragelevel = getStoragelevelImpl(selectedStorageJob);
    	storageResponse = iStoragelevel.restore(selectedStorageJob);

    	afterRestore(selectedStorageJob, storageResponse);
    	return storageResponse; 
    }
	
	protected void afterRestore(SelectedStorageJob selectedStorageJob, StorageResponse storageResponse) throws Exception {
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		//commandLineExecuter.executeCommand("chmod -R 777 " + storageJob.getTargetLocationPath());
		
		Request request = storageJob.getJob().getRequest();
		org.ishafoundation.dwaraapi.enumreferences.Action requestedAction = request.getActionId();
	
		//if(requestedAction == Action.ingest || (requestedAction == Action.restore_process && DwaraConstants.RESTORE_AND_VERIFY_FLOW_NAME.equals(request.getDetails().getFlowName())))
		if(requestedAction == Action.ingest)
			updateFileVolumeTable(selectedStorageJob, storageResponse); // update the headerblocks
		
		
		
		if(requestedAction == Action.restore || requestedAction == Action.restore_process || requestedAction == Action.rewrite) { // for ingest and restore_process with dependent jobs this happens in the scheduler... 
			// upon completion moving the file to the original requested dest path		
			org.ishafoundation.dwaraapi.db.model.transactional.File file = selectedStorageJob.getFile();
			
			// NOTE : The variable names take a swap here - Dont be confused and tempted to change it
			String restoredFilePathName = selectedStorageJob.getFilePathNameToBeRestored(); // After restore we need to swap the names of soft renamed entries. 
			String filePathNameToBeRestored = file.getPathname();
			
			String srcPath = storageJob.getTargetLocationPath() + java.io.File.separator + restoredFilePathName;
			
			String timeCodeStart = storageJob.getTimecodeStart();
			String timeCodeEnd = storageJob.getTimecodeEnd();
			boolean pfr = false;
			if(timeCodeStart != null)
				pfr = true;
			
			if(pfr)
				//srcPath = storageJob.getTargetLocationPath() + java.io.File.separator + restoredFilePathName.replace(".mkv", "_" + timeCodeStart.replace(":", "-") + "_" + timeCodeEnd.replace(":", "-") + ".pfr");
				srcPath = storageJob.getTargetLocationPath() + java.io.File.separator + restoredFilePathName.replace(".mkv", "_" + timeCodeStart.replace(":", "-") + "_" + timeCodeEnd.replace(":", "-") + ".mxf");

			String destPath = srcPath.replace(restoredFilePathName,filePathNameToBeRestored);
			
			if(requestedAction != Action.restore_process) // we still dont know for restore_process if there is a dependent job to be created or not. Even to determine if a job need to be created or not the restored artifact with its correct name is needed 
				destPath = destPath.replace(java.io.File.separator + configuration.getRestoreInProgressFileIdentifier(), "");

			if(!srcPath.equals(destPath))
				move(srcPath, destPath);
			
			// Had to do this in 2 steps for restore_process because we need the renamed artifact to determine if dependent jobs need to be created or not
			if(requestedAction == Action.restore_process && (jobCreator.hasDependentJobsToBeCreated(storageJob.getJob()).size() == 0)) { // if no dependent jobs then move it out of the temp folder... 
				move(destPath, destPath.replace(java.io.File.separator + configuration.getRestoreInProgressFileIdentifier(), ""));
			}
		}
	}
	
	private void move(String srcPath, String destPath) throws Exception {
		logger.trace("src " + srcPath);
		logger.trace("dest " + destPath);
		
		java.io.File srcFile = new java.io.File(srcPath);
		java.io.File destFile = new java.io.File(destPath);

		if(srcFile.isFile())
			Files.createDirectories(Paths.get(FilenameUtils.getFullPathNoEndSeparator(destPath)));		
		else
			Files.createDirectories(Paths.get(destPath));

		Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
		logger.info("Moved restored files from " + srcPath + " to " + destPath);
	}
	
	private IStoragelevel getStoragelevelImpl(SelectedStorageJob selectedStorageJob){
		Storagelevel storagelevel = selectedStorageJob.getStorageJob().getVolume().getStoragelevel();
		return storagelevelMap.get(storagelevel.name()+DwaraConstants.STORAGELEVEL_SUFFIX);//+"Storagelevel");
	}

}
