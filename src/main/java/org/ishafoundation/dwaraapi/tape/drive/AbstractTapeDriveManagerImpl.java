package org.ishafoundation.dwaraapi.tape.drive;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapelibraryDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.status.MtStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTapeDriveManagerImpl implements TapeDriveManager{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTapeDriveManagerImpl.class);

	@Autowired
	private TapedriveDao tapedriveDao;
	
//	@Autowired
//	private TapelibraryDao tapelibraryDao;
	
		
	// Swami said we can talk to the drive even more on a low level and get details like no.OfReads, writes, usage etc.,
	@Override
	public DriveStatusDetails getDriveDetails(int tapelibraryId, int dataTransferElementNo){
		String driveName = getDriveName(tapelibraryId, dataTransferElementNo);
		MtStatus mtStatus = getMtStatus(tapelibraryId, driveName);
		
		DriveStatusDetails dsd = new DriveStatusDetails();
//		Tapelibrary tapelibrary = tapelibraryDao.findById(tapelibraryId);
//		dsd.setTapeLibraryName(tapeLibraryName);
		dsd.setDriveSNo(dataTransferElementNo);
		dsd.setMtStatus(mtStatus);
		dsd.setDte(null);
		
		if(!mtStatus.isBusy()) {

			// TODO : callLowLevelCommandAndGetTheBelowDetails(); // SWAMI' Action point...
			dsd.setNoOfReads(5);
			dsd.setNoOfWrites(50);
			dsd.setHoursOfReads(6);
			dsd.setHoursOfWrites(544); 
			dsd.setTotalUsageInHours(550);
		}
		return dsd;
	}
	
	private String getDriveName(int tapelibraryId, int dataTransferElementNo) {
		Tapedrive tapedrive = tapedriveDao.findByTapelibraryIdAndElementAddress(tapelibraryId, dataTransferElementNo); // TODO Cache this...
		return tapedrive.getDeviceWwid();
	}

	// drivename has to be unique even on different libraries... so need to pass tapelibraryid??? 
	protected abstract MtStatus getMtStatus(int tapelibraryId, String driveName);
	

}
