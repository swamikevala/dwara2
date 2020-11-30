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
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;
import org.ishafoundation.dwaraapi.commandline.local.RetriableCommandLineExecutorImpl;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileEntityUtil;
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
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.TapeDriveManager;
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
public class TarArchiver extends AbstractTarArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(TarArchiver.class);
	
	@Autowired
	private RetriableCommandLineExecutorImpl retriableCommandLineExecutorImpl;
	
	@Override
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
}
