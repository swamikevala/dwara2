package org.ishafoundation.dwaraapi.storage.archiveformat.tar;

import java.util.HashMap;
import java.util.List;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("tar"+DwaraConstants.ARCHIVER_SUFFIX)
@Profile({ "!dev & !stage" })
public class TarArchiver extends AbstractTarArchiver {
	
	private static final Logger logger = LoggerFactory.getLogger(TarArchiver.class);
	
	@Override
	protected String executeCommand(List<String> tarCommandParamsList, String artifactName, int volumeBlocksize)
			throws Exception {

		String commandOutput = null;
//		CommandLineExecutionResponse tarCopyCommandLineExecutionResponse = commandLineExecuter.executeCommand(tarCommandParamsList, commandlineExecutorErrorResponseTemporaryFilename + ".err"); // TODO Fix this output file...
//		if(tarCopyCommandLineExecutionResponse.isComplete()) {
//			commandOutput = tarCopyCommandLineExecutionResponse.getStdOutResponse();
//		}else {
//			logger.error("tar command execution failed " + tarCopyCommandLineExecutionResponse.getFailureReason());
//			throw new Exception("Unable to execute tar command successfully");
//		}
		return commandOutput;
	}

	@Override
	protected boolean stream(List<String> commandList, int volumeBlocksize, int skipByteCount,
			String filePathNameWeNeed, String destinationPath, Checksumtype checksumtype,
			HashMap<String, byte[]> filePathNameToChecksumObj) throws Exception {
		
		return TapeStreamer.stream(commandList, volumeBlocksize, skipByteCount, filePathNameWeNeed, destinationPath, checksumtype, filePathNameToChecksumObj);
	}

	
//	@Override
//	protected int getArtifactStartVolumeBlock(StoragetypeJob storagetypeJob) {
//		Domain domain = storagetypeJob.getStorageJob().getDomain();
//		Volume volume = storagetypeJob.getStorageJob().getVolume();
//		ArtifactVolumeRepository<ArtifactVolume> domainSpecificArtifactVolumeRepository = domainUtil.getDomainSpecificArtifactVolumeRepository(domain);
//		ArtifactVolume artifactVolume = domainSpecificArtifactVolumeRepository.findTopByVolumeIdOrderByIdDesc(volume.getId());
//	
//		int artifactStartVolumeBlock = 0; // Get the last/most recent artifact_volume records' end volume block and + 1(+1 because next archive starts a fresh)
//		if(artifactVolume != null)
//			artifactStartVolumeBlock =  getArtifactStartVolumeBlock(artifactVolume);
//		
//		return artifactStartVolumeBlock;
//	}
//	
//	private int	getArtifactStartVolumeBlock(ArtifactVolume artifactVolume) {
//		return artifactVolume.getDetails().getStart_volume_block() + artifactVolume.getDetails().getTotal_volume_blocks();// + 1; not needed as we start on 0 // TODO: verify this === + 1, because the next file starts in a fresh block...
//	}
}
