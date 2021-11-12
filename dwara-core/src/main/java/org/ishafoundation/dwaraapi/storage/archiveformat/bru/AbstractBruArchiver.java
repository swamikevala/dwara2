package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchivedFile;
import org.ishafoundation.dwaraapi.storage.archiveformat.IArchiveformatter;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponse;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.BruResponseParser;
import org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components.File;
import org.ishafoundation.dwaraapi.storage.model.ArchiveformatJob;
import org.ishafoundation.dwaraapi.storage.model.SelectedStorageJob;
import org.ishafoundation.dwaraapi.storage.model.StorageJob;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractBruArchiver implements IArchiveformatter {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBruArchiver.class);

	@Autowired
	private DomainUtil domainUtil;

	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		
		int bruBufferSize = volumeBlocksize; // in bytes...
		
		String bruCopyCommand = "bru -B -cvvvvvvvvv -QX -s 0 -b " + bruBufferSize + " -f " + deviceName + " " + artifactNameToBeWritten;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + bruCopyCommand);
		
		String commandOutput = null;
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			logger.info("Bru write " +  bruCopyCommand);
			commandOutput = executeWriteCommand(commandList, artifactNameToBeWritten, volumeBlocksize);
			logger.info("Bru write complete");
//		}
		logger.trace("Before parsing bru response - " + commandOutput);
		BruResponseParser bruResponseParser = new BruResponseParser();
		BruResponse bruResponse = bruResponseParser.parseBruResponse(commandOutput);
		logger.trace("Parsed bru response object - " + bruResponse);
		logger.info("Bru archive id - " +  bruResponse.getArchiveId());
		return convertBruResponseToArchiveResponse(bruResponse, artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}

	protected ArchiveResponse convertBruResponseToArchiveResponse(BruResponse bruResponse, String artifactName, int volumeBlocksize, double archiveformatBlocksize){
		ArchiveResponse ar = new ArchiveResponse();
		ar.setArchiveId(bruResponse.getArchiveId());
		ar.setArtifactName(artifactName);
		
		/*
		1 tape block = N archive blocks
		lets say
		1 tape block = 262144 bytes
		1 archive block = 2048 bytes
		
		so N = 262144/2048 = 128
		
		lets call N - the blocking factor here...
		
		so,
			128 archive blocks = one tape block
			bruResponse.getArchiveBlocks() = how many tape blocks?
			
			// 1 tape block * bruResponse.getArchiveBlocks() = how many tape blocks? * 128
			bruResponse.getArchiveBlocks()/128 = how many tape blocks?
		*/		
		double blockingFactor = volumeBlocksize/archiveformatBlocksize; 
		int artifactTotalVolumeBlocks = (int) Math.ceil(bruResponse.getArchiveBlocks()/blockingFactor);
//		if(artifactTotalVolumeBlocks > 0)
//			artifactTotalVolumeBlocks = artifactTotalVolumeBlocks - 1;
		
		
		int artifactStartVolumeBlock = 0;
		List<ArchivedFile> archivedFileList = new ArrayList<ArchivedFile>();
		List<File> bruedFileList = bruResponse.getFileList();
		
		for (Iterator<File> iterator = bruedFileList.iterator(); iterator.hasNext();) {
			File bruedFile = (File) iterator.next();
			ArchivedFile af = new ArchivedFile();
		
			String filePathname = bruedFile.getFilePathName();
			af.setFilePathName(filePathname);
			af.setLinkName(bruedFile.getLinkName());
			
			// volumeBlockOffset starts with 0 and not -1
			int volumeBlockOffset = bruedFile.getVolumeBlockOffset() + 1; // +1, because bru "copy" responds with -1 block for BOT, while bru "t - table of contents"/"x - extraction" shows the block as 0 for same. Also while seek +1 followed by t/x returns faster results...
			if(filePathname.equals(artifactName)) {
				artifactStartVolumeBlock = volumeBlockOffset;
			}
			
			af.setVolumeBlock(volumeBlockOffset); 
			Long archiveRunningTotalDataInKB =  bruedFile.getArchiveRunningTotalDataInKB();
			//int archiveBlockOffset = archiveRunningTotalDataInKB/2;
			
			Long archiveRunningTotalDataInBytes = archiveRunningTotalDataInKB * 1024; // KB to bytes...
			Long archiveBlockOffset = (long) Math.ceil(archiveRunningTotalDataInBytes/archiveformatBlocksize);
			if(archiveBlockOffset > 0)
				archiveBlockOffset = archiveBlockOffset - 1; // - 1 because the first block starts with 0...
			
			af.setArchiveBlock(archiveBlockOffset);
			archivedFileList.add(af);
		}
		ar.setArchivedFileList(archivedFileList);

		ar.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		// -1 because artifactTotalVolumeBlocks is inclusive of the startVolumeBlock too...
		// for eg., - say 
		// svb = 2
		// tvb written = 5
		// so it would be 2,3,4,5,6
		// and so evb = (2-1) + 5 = 6
		ar.setArtifactEndVolumeBlock((artifactStartVolumeBlock-1) + artifactTotalVolumeBlocks);  
		return ar;
	}

	@Override
	public ArchiveResponse verify(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String targetLocationPath = archiveformatJob.getTargetLocationPath();
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String filePathNameToBeRestored = storageJob.getArtifact().getName();
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, false, 0, targetLocationPath, filePathNameToBeRestored);
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			executeRestoreCommand(commandList);
//		}
		boolean success = ChecksumUtil.compareChecksum(selectedStorageJob.getFilePathNameToChecksum(), targetLocationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype(),true);
		if(!success)
			throw new Exception("Verification failed");
		
		logger.info("Verification success");
		return new ArchiveResponse();
	}
	
	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String targetLocationPath = archiveformatJob.getTargetLocationPath();
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
		StorageJob storageJob = selectedStorageJob.getStorageJob();			
		String filePathNameToBeRestored = selectedStorageJob.getFilePathNameToBeRestored();
		long filesize = file.getSize();

		logger.trace("Creating the directory " + targetLocationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(targetLocationPath));
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, selectedStorageJob.isUseBuffering(), filesize, targetLocationPath, filePathNameToBeRestored);
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			executeRestoreCommand(commandList);
//		}
		
		if(storageJob.isRestoreVerify()) {
			boolean success = ChecksumUtil.compareChecksum(selectedStorageJob.getFilePathNameToChecksum(), targetLocationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
			if(!success)
				throw new Exception("Restored file's verification failed");
		}
		logger.info("Restoration complete");
		return new ArchiveResponse();
	}

	private List<String> frameRestoreCommand(int volumeBlocksize, String deviceName, boolean useBuffering, long fileSize, String destinationPath, String filePathNameToBeRestored) {
		String bruRestoreCommand = "bru -xvvvvvvvvv -ua -B -QV -C -b " + volumeBlocksize + " -f " + deviceName + " " + filePathNameToBeRestored;

		if(useBuffering) {
			/*
				/usr/bin/mbuffer -i /dev/tape/by-id/scsi-35000e11167f7d001-nst -s 1M -m 2G -p 10 -e | bru -xvvvvvvvvv -b1024k -QV -C -Pf -B -f /dev/stdin $foldername

				-f /dev/stdin (to read from stdin), 
				-Pf (to get file listing order from stdin)
				
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
			
			bruRestoreCommand = "/usr/bin/mbuffer -i " + deviceName + " -s " + volumeBlocksize + " -m " + mValue + " -p 10 -e -q | bru -xvvvvvvvvv -ua -B -QV -C -Pf -b " + volumeBlocksize + " -f /dev/stdin " + filePathNameToBeRestored;
		}
		logger.info("Bru restoring to " + destinationPath + " - " +  bruRestoreCommand);
	
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + bruRestoreCommand);
		
		return commandList;
	}
	
	protected abstract String executeWriteCommand(List<String> commandList, String artifactNameToBeWritten,
			int volumeBlocksize) throws Exception;

	protected abstract String executeRestoreCommand(List<String> commandList) throws Exception;	
}
