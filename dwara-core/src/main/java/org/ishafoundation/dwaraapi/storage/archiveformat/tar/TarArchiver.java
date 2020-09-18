package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepositoryUtil;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.TarResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.DeviceLockFactory;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/*
 * 
 * Tar returns something like
 * 
	block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/
	block 1: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/._.DS_Store
	block 2: -rwxrwxrwx root/root         0 2019-08-13 15:33 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/.DS_Store
	block 3: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/
	block 4: -rwxrwxrwx root/root  12878883 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074746.mp4
	block 25160: -rwxrwxrwx root/root  24063587 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/2 CD/20190701_074810.mp4
	block 72161: drwxrwxrwx root/root         0 2020-07-01 12:08 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/
	block 72162: -rwxrwxrwx root/root   7353665 2019-07-25 13:51 Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9/1 CD/20190701_071239.mp4

 * and the next archive again starts with block 0
	block 0: drwxrwxrwx root/root         0 2020-07-01 12:08 Shiva-Shambho_Everywhere_18-Nov-1980_Drone/
	block 1: -rwxrwxrwx root/root   7353665 2019-11-21 16:36 Shiva-Shambho_Everywhere_18-Nov-1980_Drone/DJI_0001.MOV
 	
 	The most important blocks that we need to track in our system after an ingest to successfully restore are as below  
		filevolume(each file)
			archiveblock (from response) 
			volumeblock (derived = artifactStartVolumeBlock + archiveblock/blockingfactor) , where artifactStartVolumeBlock = artifactvolume.start_volume_block shown below...
		artifactvolume
			start_volume_block - 0 OR (previous end volume block + 1), + 1 because we need to start afresh
			end_volume_block - artifactStartVolumeBlock + end volume block of the last file in the archive...
			
	So when we read the file, we need to seek and postion the tape head and fetch the noOfTapeBlocksToBeRead and skipsomebyte(a file could be starting anywhere in the volume block)
		verify(verify is mostly on artifact level??? TODO Check with Swami)
			seek = filevolume.volumeblock
			noOfTapeBlocksToBeRead = artifactVolume.end_volume_block - artifactVolume.start_volume_block + 1
			skipByteCount = 0
			
		restore
			seek = filevolume.volumeblock
		
			full artifact
				same as verify
		
				noOfTapeBlocksToBeRead = artifactVolume.end_volume_block - artifactVolume.start_volume_block + 1
				skipByteCount = 0
			
			1 folder
				iterate through all the files matching the folder name in the artifact and get the boundary file's (first and last file's) volumeblock
				noOfBlocksToBeRead = artifactVolume.getDetails().getStart_volume_block() + lastFileEndVolumeBlock - firstFileVolumeBlock; 
				skipByteCount = use the formula``` on the firstfile
			1 file
				noOfTapeBlocksToBeRead = artifactVolume.start_volume_block + file end volume block + 1(because 0 start) - seekedblock
				skipByteCount = use the formula``` on this file 
				
				skipByteCount formula``` = (fileArchiveBlock + header - reminderblocks''') * archiveformatBlockize
				
				reminderblocks''' = ((fileArchiveBlock/blockingFactor) * blockingFactor), NOTE we use integerdivision here which means (25160/1024)*1024 is != 25160, but 24576
 */
@Component("tar"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class TarArchiver implements IArchiveformatter {
	
	private static final Logger logger = LoggerFactory.getLogger(TarArchiver.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;
	
	@Autowired
	private DeviceLockFactory deviceLockFactory;

	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String junkFilesStagedDirName = archiveformatJob.getSelectedStorageJob().getJunkFilesStagedDirName();
		
		int tarBlockingFactor = volumeBlocksize/archiveformatBlocksize;
		String tarCopyCommand = "tar cvvv -R -b " + tarBlockingFactor + " -f " + deviceName + " " + artifactNameToBeWritten + " --exclude=" + junkFilesStagedDirName;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + tarCopyCommand);
		
		String commandOutput = null;
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			logger.debug(" Tar write " +  tarCopyCommand);
			commandOutput = executeCommand(commandList, artifactNameToBeWritten, volumeBlocksize);
//		}
		
		logger.trace("Before parsing tar response - " + commandOutput);
		TarResponseParser tarResponseParser = new TarResponseParser();
		TarResponse tarResponse = tarResponseParser.parseTarResponse(commandOutput);
		logger.trace("Parsed tar response object - " + tarResponse);	
		
		return convertTarResponseToArchiveResponse(tarResponse, archiveformatJob.getSelectedStorageJob(), artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {
		String commandOutput = null;
		CommandLineExecutionResponse tarCopyCommandLineExecutionResponse = retriableCommandLineExecutorImpl.executeCommandWithRetriesOnSpecificError(tarCommandParamsList, DwaraConstants.DRIVE_BUSY_ERROR, false);
		if(tarCopyCommandLineExecutionResponse.isComplete()) {
			commandOutput = tarCopyCommandLineExecutionResponse.getStdOutResponse();
		}else {
			logger.error("tar command execution failed " + tarCopyCommandLineExecutionResponse.getFailureReason());
			throw new Exception("Unable to execute tar command successfully");
		}
		return commandOutput;
	}

	private ArchiveResponse convertTarResponseToArchiveResponse(TarResponse tarResponse, SelectedStorageJob storagetypeJob, String artifactName, int volumeBlocksize, int archiveformatBlocksize){
		ArchiveResponse archiveResponse = new ArchiveResponse();

		int artifactStartVolumeBlock = getArtifactStartVolumeBlock(storagetypeJob);
		
		int artifactTotalVolumeBlocks = 0;
		
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, volumeBlocksize); 
		
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> taredFileList = tarResponse.getFileList();
		File lastTaredFile = null;
		for (Iterator<File> iterator = taredFileList.iterator(); iterator.hasNext();) {
			File taredFile = (File) iterator.next();
			ArchivedFile archivedFile = new ArchivedFile();
			String filePathName = taredFile.getFilePathName();
			if(filePathName.endsWith("/"))
				filePathName = FilenameUtils.getFullPathNoEndSeparator(filePathName);
			archivedFile.setFilePathName(filePathName);
			
			int archiveBlock = taredFile.getArchiveBlock();
			archivedFile.setArchiveBlock(archiveBlock);
			
			// running total of volume block start
			int volumeBlock =  TarBlockCalculatorUtil.getFileVolumeBlock(artifactStartVolumeBlock, archiveBlock, blockingFactor); 
			archivedFile.setVolumeBlock(volumeBlock); 
			
			archivedFileList.add(archivedFile);
			if(!iterator.hasNext())
				lastTaredFile = taredFile;
		}
		
		artifactTotalVolumeBlocks = TarBlockCalculatorUtil.getFileVolumeEndBlock(lastTaredFile.getFilePathName(), lastTaredFile.getArchiveBlock(), lastTaredFile.getFileSize(), archiveformatBlocksize, blockingFactor);
		
		archiveResponse.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		archiveResponse.setArtifactEndVolumeBlock(artifactStartVolumeBlock + artifactTotalVolumeBlocks - TarBlockCalculatorUtil.INCLUSIVE_BLOCK_ADJUSTER); // - 1 because svb inclusive
		archiveResponse.setArchivedFileList(archivedFileList);
		return archiveResponse;
	}

	// Get the last/most recent artifact_volume records' end volume block + TapeMarkblock and + 1(+1 because next archive starts afresh)
	private int getArtifactStartVolumeBlock(SelectedStorageJob storagetypeJob) {
		Domain domain = storagetypeJob.getStorageJob().getDomain();
		Volume volume = storagetypeJob.getStorageJob().getVolume();
		
		int lastArtifactOnVolumeEndVolumeBlock = artifactVolumeRepositoryUtil.getLastArtifactOnVolumeEndVolumeBlock(domain, volume);
	
		int artifactStartVolumeBlock = TarBlockCalculatorUtil.FIRSTARCHIVE_START_BLOCK; 
		if(lastArtifactOnVolumeEndVolumeBlock > 0)
			artifactStartVolumeBlock =  lastArtifactOnVolumeEndVolumeBlock + TarBlockCalculatorUtil.TAPEMARK_BLOCK + TarBlockCalculatorUtil.ARTIFACTLABEL_BLOCK + TarBlockCalculatorUtil.TAPEMARK_BLOCK + TarBlockCalculatorUtil.NEXTARCHIVE_FRESH_START_BLOCK ; //+ 2, because one block for tapemark and another one for the next archive to start in a fresh volume block...
		
		return artifactStartVolumeBlock;
	}

	@Override
	public ArchiveResponse verify(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String targetLocationPath = archiveformatJob.getTargetLocationPath();

		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String filePathNameToBeRestored = storageJob.getArtifact().getName();
		
		int noOfTapeBlocksToBeRead = getNoOfTapeBlocksToBeReadForArtifact(selectedStorageJob.getArtifactStartVolumeBlock(), selectedStorageJob.getArtifactEndVolumeBlock());
		int skipByteCount = 0; // TODO Check if verify can be applied for non-artifacts. Restore and verify???
		
		logger.trace("Creating the directory " + targetLocationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(targetLocationPath));
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, false, 0, targetLocationPath, noOfTapeBlocksToBeRead);

		HashMap<String, byte[]> filePathNameToChecksumObj = selectedStorageJob.getFilePathNameToChecksum();
		boolean isSuccess = stream(deviceName, commandList, volumeBlocksize, skipByteCount, filePathNameToBeRestored, false, targetLocationPath, true, storageJob.getVolume().getChecksumtype(), filePathNameToChecksumObj);
		logger.debug("verification status " + isSuccess);
		return new ArchiveResponse();
	}
	
	private int getNoOfTapeBlocksToBeReadForArtifact(int artifactStartVolumeBlock, int artifactEndVolumeBlock) {
		return artifactEndVolumeBlock + TarBlockCalculatorUtil.INCLUSIVE_BLOCK_ADJUSTER - artifactStartVolumeBlock;
	}

	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		archiveformatJob = getDecoratedArchiveformatJobForRestore(archiveformatJob);
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String targetLocationPath = archiveformatJob.getTargetLocationPath();
		
		int noOfTapeBlocksToBeRead = archiveformatJob.getNoOfBlocksToBeRead();
		
		int skipByteCount = archiveformatJob.getSkipByteCount();
		
		logger.trace("Creating the directory " + targetLocationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(targetLocationPath));
		
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
		long fileSize = file.getSize();
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, selectedStorageJob.isUseBuffering(), fileSize, targetLocationPath, noOfTapeBlocksToBeRead);
		
		String filePathNameToBeRestored = archiveformatJob.getFilePathNameToBeRestored();
		
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		HashMap<String, byte[]> filePathNameToChecksumObj = selectedStorageJob.getFilePathNameToChecksum();
		
		// if checksumtype supports streaming verification then set it to whatever needed... if not
		boolean streamVerify = false;
		if(storageJob.isRestoreVerify()) { // if verification needed
			if(configuration.checksumTypeSupportsStreamingVerification()) // if stream verify supported by checksumtype, then set the streamverify = true
				streamVerify = true;
		}
		stream(deviceName, commandList, volumeBlocksize, skipByteCount, filePathNameToBeRestored, true, targetLocationPath, streamVerify, storageJob.getVolume().getChecksumtype(), filePathNameToChecksumObj);

		if(storageJob.isRestoreVerify() && !streamVerify) { // TO be verified using standard approach but not the on the fly streaming and verifying
			boolean success = ChecksumUtil.compareChecksum(filePathNameToChecksumObj, targetLocationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
			if(!success)
				throw new Exception("Restored file's verification failed");
		}
		return new ArchiveResponse();
	}
	
	private List<String> frameRestoreCommand(int volumeBlocksize, String deviceName, boolean useBuffering, long fileSize, String destinationPath, int noOfTapeBlocksToBeRead) {
		String restoreCommand = "dd if=" + deviceName + " bs=" + volumeBlocksize	+ " count=" + noOfTapeBlocksToBeRead;
		

		if(useBuffering) {
			/*
				/usr/bin/mbuffer -i /dev/tape/by-id/scsi-35000e11167f7d001-nst -s 1M -m 2G -p 10 -e | dd bs=512k count=200
				mbuffer switches: 
					-e (to make mbuffer exit on error - needed when bru/tar exits after finishing reading a file - mbuffer doesn't know when to stop reading the tape, so we rely on the "broken pipe" error after bru/tar terminates
					-m 2G - provides a buffer of 2G. for bigger files a bigger buffer is better, e.g. 100 GB file, 8GB is good - formula to arrive m value =  max(1G, round_to_nearest_gb(filesize/16))
					-s 1M - basically has to match the tape volume block size
					-p 10 - means start reading from the tape when the buffer is less than 10% full. This is good because mechanically stopping/starting the tape drive is an overhead, so once the buffer becomes 100% full, this means that bru/tar will read data from the buffer until it is almost empty, and only then the tape drive will start filling up the buffer again

			 */
			
			// TODO : Should there by any max cap for the buffer ???
			float fileSizeInGiB = (float) (fileSize/1073741824.0);  // 1 GiB = 1073741824 bytes...
			int m = (int) Math.max(1, Math.round(fileSizeInGiB/16.0));
			String mValue = m + "G";
			
			restoreCommand = "/usr/bin/mbuffer -i " + deviceName + " -s " + volumeBlocksize + " -m " + mValue + " -p 10 -e  | dd bs=" + volumeBlocksize	+ " count=" + noOfTapeBlocksToBeRead;
		}
		logger.debug("Tar restoration - " +  restoreCommand);
		logger.debug("Will be restoring to - " + destinationPath);
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + restoreCommand);
		
		return commandList;
	}

	protected boolean stream(String dataTransferElementName, List<String> commandList, int volumeBlocksize, int skipByteCount,
			String filePathNameWeNeed, boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
			HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {
		return streamWithRetriesOnDeviceBusyError(dataTransferElementName, commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj, DwaraConstants.DRIVE_BUSY_ERROR, 0);
	}
	
	private boolean streamWithRetriesOnDeviceBusyError(String dataTransferElementName, List<String> commandList, int volumeBlocksize, int skipByteCount,
				String filePathNameWeNeed, boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
				HashMap<String, byte[]> filePathNameToChecksumObj, String errorMsg, int nthRetryAttempt) throws Exception {
	
		boolean success = false;
		try {
			success = TapeStreamer.stream(commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj);
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			logger.error("Tar streaming failed " + e.getMessage());
			
			if(errorMessage.contains(errorMsg) && nthRetryAttempt <= 2){
				logger.debug("Must be a parallel mt status call... Retrying again");
				success = streamWithRetriesOnDeviceBusyError(dataTransferElementName, commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj, errorMsg, nthRetryAttempt);
			}
			else
				throw e;
		}
		
		return success;
	}
	
	private ArchiveformatJob getDecoratedArchiveformatJobForRestore(ArchiveformatJob archiveformatJob) throws Exception {
		// for tar
		int noOfBlocksToBeRead = 0;
		int skipByteCount = 0;

		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
    	Domain domain = storageJob.getDomain();
    	int fileIdToBeRestored = storageJob.getFileId();
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
		Artifact artifact = storageJob.getArtifact();
		Volume volume = storageJob.getVolume();

		archiveformatJob.setFilePathNameToBeRestored(file.getPathname());
    	
		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
		ArtifactVolume artifactVolume = domainUtil.getDomainSpecificArtifactVolume(domain, artifact.getId(), volume.getId());
		
		
		Long fileSize = file.getSize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, archiveformatJob.getVolumeBlocksize()); 
		String filePathname = file.getPathname();
		int seekedVolumeBlock = storageJob.getVolumeBlock();
		if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) { //if file is folder
			if(artifact.getName().equals(filePathname)) {// if file is entire artifact
				noOfBlocksToBeRead = getNoOfTapeBlocksToBeReadForArtifact(artifactVolume.getDetails().getStartVolumeBlock(), artifactVolume.getDetails().getEndVolumeBlock());
				skipByteCount = 0; // bang on artifact
			}else {
				int firstFileVolumeBlock = 0;
				int lastFileArchiveBlock = 0;
				int lastFileEndVolumeBlock = 0;
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = selectedStorageJob.getArtifactFileList();
				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : fileList) {
					String nthArtifactFilePathname = nthFile.getPathname();//FilenameUtils.separatorsToUnix(nthArtifactFile.getPathname());
					if(nthArtifactFilePathname.startsWith(filePathname)) {
						FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
						Integer filevolumeBlock = fileVolume.getVolumeBlock();
						Integer filearchiveBlock = fileVolume.getArchiveBlock();
						if(nthArtifactFilePathname.equals(filePathname)) { // first file
							firstFileVolumeBlock = filevolumeBlock;
							skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filePathname, filearchiveBlock, archiveformatBlocksize, blockingFactor);
						}
						else if(filearchiveBlock > lastFileArchiveBlock) {
							lastFileArchiveBlock = filearchiveBlock;
							lastFileEndVolumeBlock = TarBlockCalculatorUtil.getFileVolumeEndBlock(nthArtifactFilePathname, filearchiveBlock, nthFile.getSize(), archiveformatBlocksize, blockingFactor); 
						}
					}
				}
				//noOfBlocksToBeRead = (lastFileVolumeBlock + lastFileEndVolumeBlock) - firstFileVolumeBlock;
				noOfBlocksToBeRead = artifactVolume.getDetails().getStartVolumeBlock() + lastFileEndVolumeBlock - firstFileVolumeBlock;
			}
			 
		}else {
//			int fileEndArchiveBlock = TarBlockCalculatorUtil.getFileArchiveBlocksCount(fileSize, archiveBlocksize);
//			noOfBlocksToBeRead = TarBlockCalculatorUtil.getFileVolumeBlocksCount(fileEndArchiveBlock, blockingFactor);

			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, fileIdToBeRestored, volume.getId());// lets just let users use the util consistently
			Integer filearchiveBlock = fileVolume.getArchiveBlock();
			noOfBlocksToBeRead = artifactVolume.getDetails().getStartVolumeBlock() + TarBlockCalculatorUtil.getFileVolumeEndBlock(filePathname, filearchiveBlock, fileSize, archiveformatBlocksize, blockingFactor) - seekedVolumeBlock;
			skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filePathname, filearchiveBlock, archiveformatBlocksize, blockingFactor); 		
		}
		
		archiveformatJob.setNoOfBlocksToBeRead(noOfBlocksToBeRead);
		archiveformatJob.setSkipByteCount(skipByteCount);
		return archiveformatJob;
	}
}
