package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.PfrConstants;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
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
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
public abstract class AbstractTarArchiver implements IArchiveformatter {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTarArchiver.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private FileEntityUtil fileEntityUtil;
	
	@Autowired
	private Configuration configuration;
	
	@Autowired
	private ArtifactVolumeRepositoryUtil artifactVolumeRepositoryUtil;
	
	@Autowired
	private TapeDriveManager tapeDriveManager;
	
	@Override
	public ArchiveResponse write(ArchiveformatJob archiveformatJob) throws Exception {
		String artifactSourcePath = archiveformatJob.getArtifactSourcePath();
		String artifactNameToBeWritten = archiveformatJob.getArtifactNameToBeWritten();
		
		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String junkFilesStagedDirName = archiveformatJob.getSelectedStorageJob().getJunkFilesStagedDirName();
		
		int tarBlockingFactor = volumeBlocksize/archiveformatBlocksize;
		String tarCopyCommand = "tar cvvv -R -b " + tarBlockingFactor + " -f " + deviceName + " " + artifactNameToBeWritten + " --exclude=" + junkFilesStagedDirName + " --format=posix";
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + artifactSourcePath + " ; " + tarCopyCommand);
		
		String commandOutput = null;
//		synchronized (deviceLockFactory.getDeviceLock(deviceName)) {
			logger.info("Tar write " +  tarCopyCommand);
			commandOutput = executeCommand(commandList, artifactNameToBeWritten, volumeBlocksize);
			logger.info("Tar write complete");
//		}
		
		logger.trace("Before parsing tar response - " + commandOutput);
		TarResponseParser tarResponseParser = new TarResponseParser();
		TarResponse tarResponse = tarResponseParser.parseTarResponse(commandOutput);
		logger.trace("Parsed tar response object - " + tarResponse);	
		
		return convertTarResponseToArchiveResponse(deviceName, tarResponse, archiveformatJob.getSelectedStorageJob(), artifactNameToBeWritten, volumeBlocksize, archiveformatBlocksize);
	}
	
	protected abstract String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception;
	
	private ArchiveResponse convertTarResponseToArchiveResponse(String deviceName, TarResponse tarResponse, SelectedStorageJob storagetypeJob, String artifactName, int volumeBlocksize, int archiveformatBlocksize) throws Exception{
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
			archivedFile.setLinkName(taredFile.getLinkName());
			
			Long archiveBlock = taredFile.getArchiveBlock();
			archivedFile.setArchiveBlock(archiveBlock);
			
			// running total of volume block start
			int volumeBlock =  TarBlockCalculatorUtil.getFileVolumeBlock(artifactStartVolumeBlock, archiveBlock, blockingFactor); 
			archivedFile.setVolumeBlock(volumeBlock); 
			
			archivedFileList.add(archivedFile);
			if(!iterator.hasNext())
				lastTaredFile = taredFile;
		}
		
		archiveResponse.setArtifactStartVolumeBlock(artifactStartVolumeBlock);
		artifactTotalVolumeBlocks = TarBlockCalculatorUtil.getFileVolumeEndBlock(lastTaredFile.getArchiveBlock(), 3, lastTaredFile.getFileSize(), archiveformatBlocksize, blockingFactor);
		int evbOldWays = artifactStartVolumeBlock + artifactTotalVolumeBlocks - TarBlockCalculatorUtil.INCLUSIVE_BLOCK_ADJUSTER;  // - 1 because svb inclusive
		logger.trace("evbOldWays " + evbOldWays);
//		archiveResponse.setArtifactEndVolumeBlock(evbOldWays);
		int evbNewWays = tapeDriveManager.getCurrentPositionBlockNumber(deviceName) - TarBlockCalculatorUtil.TAPEMARK_BLOCK - TarBlockCalculatorUtil.INCLUSIVE_BLOCK_ADJUSTER; // - TMB because tell always includes the tapemarkblock...
		logger.trace("evbNewWays " + evbNewWays);
		if(evbOldWays != evbNewWays) {
			String msg = "evb mismatch - evbOldWays" + evbOldWays + " evbNewWays " + evbNewWays;
			logger.error(msg);
			//throw new Exception(msg);
		}
		archiveResponse.setArtifactEndVolumeBlock(evbNewWays);
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
		TapeStreamerResponse tsr = stream(deviceName, commandList, volumeBlocksize, skipByteCount, filePathNameToBeRestored, true, false, targetLocationPath, true, storageJob.getVolume().getChecksumtype(), filePathNameToChecksumObj);
		if(tsr.isSuccess())
			logger.info("Verification success");
		else
			throw new Exception("Verification failed");
		
		ArchiveResponse archiveResponse = new ArchiveResponse();
		archiveResponse.setArchivedFilePathNameToHeaderBlockCnt(tsr.getFilePathNameToHeaderBlockCnt());
		
		return archiveResponse;
	}
	
	private int getNoOfTapeBlocksToBeReadForArtifact(int artifactStartVolumeBlock, int artifactEndVolumeBlock) {
		return artifactEndVolumeBlock + TarBlockCalculatorUtil.INCLUSIVE_BLOCK_ADJUSTER - artifactStartVolumeBlock;
	}

	@Override
	public ArchiveResponse restore(ArchiveformatJob archiveformatJob) throws Exception {
		SelectedStorageJob selectedStorageJob = archiveformatJob.getSelectedStorageJob();
		StorageJob storageJob = selectedStorageJob.getStorageJob();
		String timeCodeStart = storageJob.getTimecodeStart();
		boolean pfr = false;
		if(timeCodeStart != null)
			pfr = true;
		
		if(!pfr)
			archiveformatJob = getDecoratedArchiveformatJobForRestore(archiveformatJob);

		int volumeBlocksize = archiveformatJob.getVolumeBlocksize();
		String deviceName = archiveformatJob.getDeviceName();
		String targetLocationPath = archiveformatJob.getTargetLocationPath();
		
		int noOfTapeBlocksToBeRead = archiveformatJob.getNoOfBlocksToBeRead();
		
		int skipByteCount = archiveformatJob.getSkipByteCount();
		
		logger.trace("Creating the directory " + targetLocationPath + ", if not already present");
		FileUtils.forceMkdir(new java.io.File(targetLocationPath));
		
		org.ishafoundation.dwaraapi.db.model.transactional.domain.File file = selectedStorageJob.getFile();
		long fileSize = file.getSize();
		if(!pfr) {
			List<String> commandList = frameRestoreCommand(volumeBlocksize, deviceName, selectedStorageJob.isUseBuffering(), fileSize, targetLocationPath, noOfTapeBlocksToBeRead);
			
			String filePathNameToBeRestored = selectedStorageJob.getFilePathNameToBeRestored();
			
			HashMap<String, byte[]> filePathNameToChecksumObj = selectedStorageJob.getFilePathNameToChecksum();

		
			// if checksumtype supports streaming verification then set it to whatever needed... if not
			boolean streamVerify = false;
			if(storageJob.isRestoreVerify()) { // if verification needed
				if(configuration.checksumTypeSupportsStreamingVerification()) // if stream verify supported by checksumtype, then set the streamverify = true
					streamVerify = true;
			}
			TapeStreamerResponse tsr = stream(deviceName, commandList, volumeBlocksize, skipByteCount, filePathNameToBeRestored, file.isDirectory(), true, targetLocationPath, streamVerify, storageJob.getVolume().getChecksumtype(), filePathNameToChecksumObj);
			if(storageJob.isRestoreVerify()) {
				boolean success = true;
				if(streamVerify) { // TO be verified using standard approach but not the on the fly streaming and verifying
					success = tsr.isSuccess();
				}
				else {
					success = ChecksumUtil.compareChecksum(filePathNameToChecksumObj, targetLocationPath, filePathNameToBeRestored, storageJob.getVolume().getChecksumtype());
				}
				if(!success)
					throw new Exception("Restored file's verification failed");
			}

			ArchiveResponse archiveResponse = new ArchiveResponse();
			archiveResponse.setArchivedFilePathNameToHeaderBlockCnt(tsr.getFilePathNameToHeaderBlockCnt());
			
			java.io.File restoredfile = new java.io.File(targetLocationPath, file.getPathname());
			if(!restoredfile.exists())
				throw new Exception("Restore seems to be completed, but for some reason file doesnt exist in the destination location");
			
			logger.info("Restoration complete");
			return archiveResponse;
		}else {
			String timeCodeEnd = storageJob.getTimecodeEnd();
			String filePathNameToBeRestored = file.getPathname();
			logger.trace("filePathNameToBeRestored " + filePathNameToBeRestored);
			String parentDir = FilenameUtils.getFullPathNoEndSeparator(filePathNameToBeRestored);
			FileUtils.forceMkdir(new java.io.File(targetLocationPath + java.io.File.separator + parentDir));
			String partialFileFromTapeOutputFilePathName = filePathNameToBeRestored.replace(PfrConstants.MKV_EXTN, "_" + timeCodeStart.replace(":", "-") + "_" + timeCodeEnd.replace(":", "-") + PfrConstants.RESTORED_FROM_TAPE_BIN);
			
			Domain domain = storageJob.getDomain();
			String path = fileEntityUtil.getArtifact(file, domain).getArtifactclass().getPath();
			logger.trace("path " + path);
			String filePathname = path + java.io.File.separator + file.getPathname();
			logger.trace("filePathname " + filePathname);
			String cuesFileEntries = FileUtils.readFileToString(new java.io.File(filePathname.replace(PfrConstants.MKV_EXTN, PfrConstants.INDEX_EXTN)));
			
			long startClusterPosition = Long.parseLong(getClusterPosition(cuesFileEntries, timeCodeStart));
//			String inclusiveTimeCode = getInclusiveTimeCode(timeCodeEnd);
//			String endClusterPositionAsString = getClusterPosition(cuesFileEntries, inclusiveTimeCode);
//			if(endClusterPositionAsString == null)
//				endClusterPositionAsString = getClusterPosition(cuesFileEntries, timeCodeEnd);
//			long endClusterPosition = Long.parseLong(endClusterPositionAsString);
			long endClusterPosition = Long.parseLong(getClusterPosition(cuesFileEntries, timeCodeEnd));
			
			long bytesToRetrieve = endClusterPosition - startClusterPosition;
			logger.trace("bytesToRetrieve " + bytesToRetrieve);
			float volumeBlocksizeAsFloat = volumeBlocksize;
			noOfTapeBlocksToBeRead = (int) Math.ceil(bytesToRetrieve/volumeBlocksizeAsFloat);
			logger.trace("noOfTapeBlocksToBeRead " + noOfTapeBlocksToBeRead);
			

			
			String ddRestoreCommand = "dd if=" + deviceName + " bs=" + volumeBlocksize + " count=" + noOfTapeBlocksToBeRead + " of=" + partialFileFromTapeOutputFilePathName;
			logger.trace("ddRestoreFromTapeCommand " + ddRestoreCommand);
			List<String> commandList = new ArrayList<String>();
			commandList.add("sh");
			commandList.add("-c");
			commandList.add("cd " + targetLocationPath + " ; " + ddRestoreCommand);

			logger.trace(executeCommand(commandList, null, volumeBlocksize));

			int fileId = storageJob.getFileId();
			Volume volume = storageJob.getVolume();
			FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, fileId, volume.getId());// lets just let users use the util consistently
			Integer headerBlocks = fileVolume.getHeaderBlocks();

			if(headerBlocks == null)
				headerBlocks = 3;
			long noOfBytesToBeSkipped = ((storageJob.getArchiveBlock() + headerBlocks) * storageJob.getVolume().getArchiveformat().getBlocksize()) + startClusterPosition; // TODO Swami check not subtracting as it throws error - 1;
			int bytesConvertedAsBlocks =  (int) (noOfBytesToBeSkipped/storageJob.getVolume().getDetails().getBlocksize());
			
			logger.trace("bytesConvertedAsBlocks " + bytesConvertedAsBlocks);
			long bytesAlreadySeeked = bytesConvertedAsBlocks * storageJob.getVolume().getDetails().getBlocksize();
			logger.trace("bytesAlreadySeeked " + bytesAlreadySeeked);
			
			long bytesToBeSkipped = noOfBytesToBeSkipped - bytesAlreadySeeked;
			logger.trace("bytesToBeSkipped " + bytesToBeSkipped);
			
			//String trimmedOutputFilePathName = filePathNameToBeRestored.replace(".mkv", "_" + timeCodeStart.replace(":", "-") + "_" + timeCodeEnd.replace(":", "-") + ".pfr");
			String trimmedOutputFilePathName = partialFileFromTapeOutputFilePathName.replace(PfrConstants.RESTORED_FROM_TAPE_BIN, PfrConstants.TRIMMED_BIN);
			
			String trimCommand = "dd if=" + partialFileFromTapeOutputFilePathName + " skip=" + bytesToBeSkipped + " count=" + bytesToRetrieve + " iflag=skip_bytes,count_bytes of=" + trimmedOutputFilePathName;
			logger.trace("trimCommand " + trimCommand);
			List<String> trimCommandList = new ArrayList<String>();
			trimCommandList.add("sh");
			trimCommandList.add("-c");
			trimCommandList.add("cd " + targetLocationPath + " ; " + trimCommand);

			logger.trace("trimCommand response - "+ executeCommand(trimCommandList, null, volumeBlocksize));

			// cat P22250_sample.hdr sample_00-00-01_00-00-02.pfr > sample_stitched_00-00-01_00-00-02.mkv
			String headerFilePathName = filePathname.replace(PfrConstants.MKV_EXTN, PfrConstants.HDR_EXTN);
			String stitchedFilePathName = trimmedOutputFilePathName.replace(PfrConstants.TRIMMED_BIN, PfrConstants.STITCHED_MKV);
			String catCommand = "cat " + headerFilePathName + " " + trimmedOutputFilePathName + " > " + stitchedFilePathName;
			List<String> catCommandList = new ArrayList<String>();
			catCommandList.add("sh");
			catCommandList.add("-c");
			catCommandList.add("cd " + targetLocationPath + " ; " + catCommand);

			logger.trace("catCommand response - "+ executeCommand(catCommandList, null, volumeBlocksize));

			

			// mkvmerge -o sample_00-00-01_00-00-02.mkv sample_stitched_00-00-01_00-00-02.mkv
			String wrappedFileName = stitchedFilePathName.replace(PfrConstants.STITCHED_MKV, PfrConstants.MKV_EXTN);
			String mkvmergeCommand = "mkvmerge -o " + wrappedFileName + " " + stitchedFilePathName;
			List<String> mkvmergeCommandList = new ArrayList<String>();
			mkvmergeCommandList.add("sh");
			mkvmergeCommandList.add("-c");
			mkvmergeCommandList.add("cd " + targetLocationPath + " ; " + mkvmergeCommand);

			logger.trace("mkvmergeCommand response - "+ executeCommand(mkvmergeCommandList, null, volumeBlocksize));

			
			 
			 
			 // ffmpeg -i sample_00-00-01_00-00-02.mkv -target pal-dv50 sample_editingTeam_00-00-01_00-00-02.mxf
			String editingTeamFileName = wrappedFileName.replace(PfrConstants.MKV_EXTN, PfrConstants.MXF_EXTN);
			String mxfConversionCommand = "ffmpeg -i " + wrappedFileName + " -target pal-dv50 " + editingTeamFileName;
			List<String> mxfConversionCommandList = new ArrayList<String>();
			mxfConversionCommandList.add("sh");
			mxfConversionCommandList.add("-c");
			mxfConversionCommandList.add("cd " + targetLocationPath + " ; " + mxfConversionCommand);

			logger.trace("mxfConversionCommand response - "+ executeCommand(mxfConversionCommandList, null, volumeBlocksize));
		}
		logger.info("Restoration complete");
		return new ArchiveResponse();
	}
	
	private String getInclusiveTimeCode(String timeCode) {
		String inclusiveTimeCode = timeCode;
		Pattern timecodeRegexPattern = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})");
		Matcher timecodeRegexMatcher = timecodeRegexPattern.matcher(timeCode);
		if(timecodeRegexMatcher.matches()) {
			String seconds = timecodeRegexMatcher.group(3);
			// Zero padded seconds value...
			int inclusiveSecond = Integer.parseInt(seconds) + 1; // TODO : if second is 59, + 1 is next minute. need to handle that scenario.
			inclusiveTimeCode = timecodeRegexMatcher.group(1) + ":" + timecodeRegexMatcher.group(2) + ":" + String.format("%02d", inclusiveSecond);
		}
		
		return inclusiveTimeCode;
	}
	
	// TODO move this method to CuesFileParser
	private String getClusterPosition(String cuesFileEntries, String timestamp){
		String clusterPosition = null;
		Pattern timestampLineRegexPattern = Pattern.compile("timestamp=" + timestamp + ".000000000 duration=- cluster_position=([0-9]*) relative_position=([0-9]*)");
		
		
		Matcher timestampLineRegexMatcher = timestampLineRegexPattern.matcher(cuesFileEntries);
		if(timestampLineRegexMatcher.find()) {
			clusterPosition = timestampLineRegexMatcher.group(1);
		}

		return clusterPosition;
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
		logger.info("Tar restoring to " + destinationPath + " - " +  restoreCommand);
		
		List<String> commandList = new ArrayList<String>();
		commandList.add("sh");
		commandList.add("-c");
		commandList.add("cd " + destinationPath + " ; " + restoreCommand);
		
		return commandList;
	}

	protected TapeStreamerResponse stream(String dataTransferElementName, List<String> commandList, int volumeBlocksize, int skipByteCount,
			String filePathNameWeNeed, boolean isFilePathNameWeNeedIsDirectory, boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
			HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {
		return streamWithRetriesOnDeviceBusyError(dataTransferElementName, commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, isFilePathNameWeNeedIsDirectory, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj, DwaraConstants.DRIVE_BUSY_ERROR, 0);
	}
	
	private TapeStreamerResponse streamWithRetriesOnDeviceBusyError(String dataTransferElementName, List<String> commandList, int volumeBlocksize, int skipByteCount,
				String filePathNameWeNeed, boolean isFilePathNameWeNeedIsDirectory, boolean toBeRestored, String destinationPath, boolean toBeVerified, Checksumtype checksumtype,
				HashMap<String, byte[]> filePathNameToChecksumObj, String errorMsg, int nthRetryAttempt) throws Exception {
	
		TapeStreamerResponse tsr = null;
		try {
			tsr = TapeStreamer.stream(commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, isFilePathNameWeNeedIsDirectory, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj);
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			logger.error("Tar streaming failed " + e.getMessage());
			
			if(errorMessage.contains(errorMsg) && nthRetryAttempt <= 2){
				logger.debug("Must be a parallel mt status call... Retrying again");
				tsr = streamWithRetriesOnDeviceBusyError(dataTransferElementName, commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, isFilePathNameWeNeedIsDirectory, toBeRestored, destinationPath, toBeVerified, checksumtype, filePathNameToChecksumObj, errorMsg, nthRetryAttempt + 1);
			}
			else
				throw e;
		}
		
		return tsr;
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
    	
		ArtifactVolume artifactVolume = selectedStorageJob.getArtifactVolume();
		
		String filePathname = file.getPathname();

		Long fileSize = file.getSize();
		int archiveformatBlocksize = archiveformatJob.getArchiveformatBlocksize();
		int blockingFactor = TarBlockCalculatorUtil.getBlockingFactor(archiveformatBlocksize, archiveformatJob.getVolumeBlocksize()); 
		
		int seekedVolumeBlock = storageJob.getVolumeBlock();
		if(StringUtils.isBlank(FilenameUtils.getExtension(filePathname))) { //if file is folder
			if(artifact.getName().equals(filePathname)) {// if file is entire artifact
				noOfBlocksToBeRead = getNoOfTapeBlocksToBeReadForArtifact(artifactVolume.getDetails().getStartVolumeBlock(), artifactVolume.getDetails().getEndVolumeBlock());
				skipByteCount = 0; // bang on artifact
			}else {
				int firstFileVolumeBlock = 0;
				long lastFileArchiveBlock = 0;
				int lastFileEndVolumeBlock = 0;
				List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = selectedStorageJob.getArtifactFileList();
				for (org.ishafoundation.dwaraapi.db.model.transactional.domain.File nthFile : fileList) {
					String nthArtifactFilePathname = nthFile.getPathname();//FilenameUtils.separatorsToUnix(nthArtifactFile.getPathname());
					if(nthArtifactFilePathname.startsWith(filePathname)) {
						FileVolume fileVolume = domainUtil.getDomainSpecificFileVolume(domain, nthFile.getId(), volume.getId());// lets just let users use the util consistently
						Integer filevolumeBlock = fileVolume.getVolumeBlock();
						Long filearchiveBlock = fileVolume.getArchiveBlock();
						Integer headerBlocks = fileVolume.getHeaderBlocks();
						if(nthArtifactFilePathname.equals(filePathname)) { // first file
							firstFileVolumeBlock = filevolumeBlock;
							skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filearchiveBlock, archiveformatBlocksize, blockingFactor);
						}
						else if(filearchiveBlock > lastFileArchiveBlock) {
							lastFileArchiveBlock = filearchiveBlock;
							lastFileEndVolumeBlock = TarBlockCalculatorUtil.getFileVolumeEndBlock(filearchiveBlock, headerBlocks, nthFile.getSize(), archiveformatBlocksize, blockingFactor); 
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
			Long filearchiveBlock = fileVolume.getArchiveBlock();
			Integer headerBlocks = fileVolume.getHeaderBlocks();
			noOfBlocksToBeRead = artifactVolume.getDetails().getStartVolumeBlock() + TarBlockCalculatorUtil.getFileVolumeEndBlock(filearchiveBlock, headerBlocks, fileSize, archiveformatBlocksize, blockingFactor) - seekedVolumeBlock;
			skipByteCount = TarBlockCalculatorUtil.getSkipByteCount(filearchiveBlock, archiveformatBlocksize, blockingFactor); 		
		}
		
		archiveformatJob.setNoOfBlocksToBeRead(noOfBlocksToBeRead);
		archiveformatJob.setSkipByteCount(skipByteCount);
		return archiveformatJob;
	}
}
