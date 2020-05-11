package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTapeDriveManagerImpl implements TapeDriveManager{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTapeDriveManagerImpl.class);

	@Autowired
	private TapedriveDao tapedriveDao;

	@Override
	public String getDriveWwid(String tapelibraryName, int dataTransferElementNo) {
		Tapedrive tapedrive = tapedriveDao.findByTapelibraryNameAndElementAddress(tapelibraryName, dataTransferElementNo); // TODO Cache this...
		return tapedrive.getDeviceWwid();
	}

	
	// Swami said we can talk to the drive even more on a low level and get details like no.OfReads, writes, usage etc.,
	@Override
	public DriveStatusDetails getDriveDetails(String tapelibraryName, int dataTransferElementNo){
		String driveName = getDriveWwid(tapelibraryName, dataTransferElementNo);
		MtStatus mtStatus = getMtStatus(driveName);
		
		DriveStatusDetails dsd = new DriveStatusDetails();
		dsd.setTapelibraryName(tapelibraryName);
		dsd.setDriveSNo(dataTransferElementNo);
		dsd.setDriveName(driveName);
		dsd.setMtStatus(mtStatus);
		dsd.setDte(null);
		
		if(!mtStatus.isBusy()) {

//			// TODO : callLowLevelCommandAndGetTheBelowDetails(); // SWAMI' Action point...
//			dsd.setNoOfReads(5);
//			dsd.setNoOfWrites(50);
//			dsd.setHoursOfReads(6);
//			dsd.setHoursOfWrites(544); 
//			dsd.setTotalUsageInHours(550);
		}
		return dsd;
	}
	

	// drivename has to be unique even on different libraries... so need to pass tapelibraryid??? 
//	protected abstract MtStatus getMtStatus(int tapelibraryId, String driveName);
	protected abstract MtStatus getMtStatus(String driveName);

}
