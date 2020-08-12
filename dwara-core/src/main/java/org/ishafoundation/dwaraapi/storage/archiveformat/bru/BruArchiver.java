package org.ishafoundation.dwaraapi.storage.archiveformat.bru;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuter;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.configuration.Configuration;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("bru"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class BruArchiver implements IArchiveformatter {

	private static final Logger logger = LoggerFactory.getLogger(BruArchiver.class);
	
	@Autowired
	private CommandLineExecuter commandLineExecuter;

	@Autowired
	private Configuration configuration;
	
	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		
		int bruBufferSize = volumeBlocksize; // in bytes...
		
		String bruCopyCommand = "bru -B -clOjvvvvvvvvv -QX -C -b " + bruBufferSize + " -f " + deviceName + " " + artifactNameToBeWritten;
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + bruCopyCommand);
		
		logger.debug(" Bru write " +  bruCopyCommand);
		String commandOutput = executeWriteCommand(commandList, artifactNameToBeWritten, volumeBlocksize);

		logger.trace("Before parsing bru response - " + commandOutput);
		BruResponseParser bruResponseParser = new BruResponseParser();
		BruResponse bruResponse = bruResponseParser.parseBruResponse(commandOutput);
		logger.trace("Parsed bru response object - " + bruResponse);		
		return convertBruResponseToArchiveResponse(bruResponse, artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected String executeWriteCommand(List<String> writeCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {
		return executeCommand(writeCommandParamsList);
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
			
			// volumeBlockOffset starts with 0 and not -1
			int volumeBlockOffset = bruedFile.getVolumeBlockOffset() + 1; // +1, because bru "copy" responds with -1 block for BOT, while bru "t - table of contents"/"x - extraction" shows the block as 0 for same. Also while seek +1 followed by t/x returns faster results...
			if(filePathname.equals(artifactName)) {
				artifactStartVolumeBlock = volumeBlockOffset;
			}
			
			af.setVolumeBlock(volumeBlockOffset); 
			Long archiveRunningTotalDataInKB =  bruedFile.getArchiveRunningTotalDataInKB();
			//int archiveBlockOffset = archiveRunningTotalDataInKB/2;
			
			Long archiveRunningTotalDataInBytes = archiveRunningTotalDataInKB * 1024; // KB to bytes...
			int archiveBlockOffset = (int) Math.ceil(archiveRunningTotalDataInBytes/archiveformatBlocksize);
			if(archiveBlockOffset > 0)
				archiveBlockOffset = archiveBlockOffset - 1; // - 1 because the first block starts with 0...
			
			af.setArchiveBlock(archiveBlockOffset);
			archivedFileList.add(af);
		}
		ar.setArchivedFileList(archivedFileList);

		ar.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		ar.setArtifactEndVolumeBlock(artifactStartVolumeBlock + artifactTotalVolumeBlocks);
		return ar;
	}

	@Override
	public ArchiveResponse verify(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String filePathNameToBeRestored = storageJob.getArtifact().getName();
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, destinationPath, filePathNameToBeRestored);
		
		executeRestoreCommand(commandList);
		
		boolean success = ChecksumUtil.compareChecksum(selectedStorageJob.getFilePathNameToChecksum(), destinationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
		if(!success)
			throw new Exception("Verification failed");
		
		
		return new ArchiveResponse();
	}
	
	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String destinationPath = archiveformatJob.getDestinationPath();
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
		String filePathNameToBeRestored = file.getPathname();

		logger.trace("Creating the directory " + destinationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(destinationPath));
		
		List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, destinationPath, filePathNameToBeRestored);

		executeRestoreCommand(commandList);

		StorageJob storageJob = selectedStorageJob.getStorageJob();
		
		if(storageJob.isRestoreVerify()) {
			boolean success = ChecksumUtil.compareChecksum(selectedStorageJob.getFilePathNameToChecksum(), destinationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
			if(!success)
				throw new Exception("Restored file's verification failed");
		}
		return new ArchiveResponse();
	}

	private List<String> frameRestoreCommand(int volumeBlocksize, String deviceName, String destinationPath, String filePathNameToBeRestored) {
		

		
		// FIXME: mbuffer -s 1M -m 2G -i /dev/st1 | bru -xvvvvvvvvv -b1024k -QV -C ***-f*** - 25079_Cauvery-Calling_Day15-Crowd-Shots-At-Closing-Event_CODISSIA-Cbe_17-Sep-2019_Z280V_B-Rolls
		String bruRestoreCommand = "bru -B -xvvvvvvvvv -QV -C -b " + volumeBlocksize + " -f " + deviceName + " " + filePathNameToBeRestored;
		if(configuration.useMbuffer()) {
			/*
				/usr/bin/mbuffer -i /dev/tape/by-id/scsi-35000e11167f7d001-nst -s 1M -m 2G -p 10 -e | bru -xvvvvvvvvv -b1024k -QV -C -Pf -B -f /dev/stdin $foldername

				-f /dev/stdin (to read from stdin), 
				-Pf (to get file listing order from stdin)
				mbuffer switches: 
					-e (to make mbuffer exit on error - needed when bru exits after finishing reading a file - mbuffer doesn't know when to stop reading the tape, so we rely on the "broken pipe" error afer bru terminates
					-m 2G provides a buffer of 2G. for bigger files a bigger buffer is better, e.g. 100 GB file, 8GB is good
					-s 1M basically has to match the tape volume block size
					-p 10 means start reading from the tape when the buffer is less than 10% full. This is good because mechanically stopping/starting the tape drive is an overhead, so once the buffer becomes 100% full, this means that bru/tar will read data from the buffer until it is almost empty, and only then the tape drive will start filling up the buffer again

			 */
			bruRestoreCommand = "/usr/bin/mbuffer -i " + deviceName + " -s " + volumeBlocksize + " -m 2G -p 10 -e  | bru -B -xvvvvvvvvv -QV -C -Pf -b " + volumeBlocksize + " -f /dev/stdin " + filePathNameToBeRestored;
		}
		logger.debug("Bru restoring to " + destinationPath + " - " +  bruRestoreCommand);
	
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + bruRestoreCommand);
		
		return commandList;
	}
	
	protected String executeRestoreCommand(List<String> restoreCommandParamsList) throws Exception {
		return executeCommand(restoreCommandParamsList);
	}
	
	private String executeCommand(List<String> bruCommandParamsList)
			throws Exception {
		String commandOutput = null;
		CommandLineExecutionResponse bruCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(bruCommandParamsList);
		if(bruCopyCommandLineExecutionResponse.isComplete()) {
			commandOutput = bruCopyCommandLineExecutionResponse.getStdOutResponse();
		}else {
			logger.error("Bru command execution failed " + bruCopyCommandLineExecutionResponse.getFailureReason());
			throw new Exception("Unable to execute bru command successfully");
		}
		return commandOutput;
	}
}
