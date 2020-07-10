package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
import org.ishafoundation.dwaraapi.storage.model.TapeJob;
import org.ishafoundation.dwaraapi.storage.storagetype.AbstractStoragetypeJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@Component("tapeJobProcessor")
@Component("tape" + DwaraConstants.STORAGETYPE_JOBPROCESSOR_SUFFIX)
//@Profile({ "!dev & !stage" })
public class TapeJobProcessor extends AbstractStoragetypeJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger(TapeJobProcessor.class);
//
//    static {
//    	StoragetypeJobProcessorFactory.register(Storagetype.tape.name(), TapeJobProcessor.class);
//    }
//    
//	@Autowired
//	private TapeDriveManager tapeDriveManager;
//	
//
//	
	@Override
	protected void beforeWrite(StoragetypeJob storagetypeJob) {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		
		logger.trace("Now positioning tape head for writing" + tapeLibraryName + ":" + driveElementAddress);
//		tapeDriveManager.setTapeHeadPositionForWriting(tapeLibraryName, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for writing");
		
	}
	
	@Override
	protected void beforeVerify(StoragetypeJob storagetypeJob) throws Exception {
		super.beforeVerify(storagetypeJob);
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getArtifactStartVolumeBlock();
		
//		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for verifying"+ tapeLibraryName + ":" + driveElementAddress  + ":" + blockNumberToSeek);
	}

//	@Override
//	protected void afterWrite(StoragetypeJob storageJob, ArchiveResponse ar) {
//		// TODO Auto-generated method stub
//		logger.debug("after write hook on TapeJP");
//	}

	@Override
	protected void beforeRestore(StoragetypeJob storagetypeJob) {
		TapeJob tapeJob = (TapeJob) storagetypeJob;
		String tapeLibraryName = tapeJob.getTapeLibraryName();
		int driveElementAddress = tapeJob.getTapedriveNo();
		int blockNumberToSeek = tapeJob.getStorageJob().getVolumeBlock();
		
//		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress, blockNumberToSeek); // FIXME - check on this, using eod, bsf 1 and fsf 1
		logger.trace("Tape Head positioned for reading"+ tapeLibraryName + ":" + driveElementAddress  + ":" + blockNumberToSeek);
	}


//	@Override
//	protected void afterRestore(StorageTypeJob storageJob) {
//		// TODO Auto-generated method stub
//		
//	}
//

}
