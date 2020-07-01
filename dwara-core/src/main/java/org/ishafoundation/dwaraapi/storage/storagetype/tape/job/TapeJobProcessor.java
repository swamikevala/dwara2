package org.ishafoundation.dwaraapi.storage.storagetype.tape.job;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.storage.archiveformat.ArchiveResponse;
import org.ishafoundation.dwaraapi.storage.model.StoragetypeJob;
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
	protected void beforeWrite(StoragetypeJob storageJob) {
		logger.debug("before write hook on TapeJP");
//		// TODO Auto-generated method stub
//		String tapeLibraryName = storageJob.getTapeLibraryName();
//		int driveElementAddress = storageJob.getDriveNo();
//		
//		logger.trace("Now positioning tape head for writing");
//		tapeDriveManager.setTapeHeadPositionForWriting(tapeLibraryName, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
//		logger.trace("Tape Head positioned for writing");
		
	}

//	@Override
//	protected void afterWrite(StoragetypeJob storageJob, ArchiveResponse ar) {
//		// TODO Auto-generated method stub
//		logger.debug("after write hook on TapeJP");
//	}

	@Override
	protected void beforeRestore(StoragetypeJob storageJob) {
		// TODO Auto-generated method stub
		
//		tapeDriveManager.setTapeHeadPositionForReading(tapeLibraryName, driveElementAddress); // FIXME - check on this, using eod, bsf 1 and fsf 1
	}


//	@Override
//	protected void afterRestore(StorageTypeJob storageJob) {
//		// TODO Auto-generated method stub
//		
//	}
//

}
