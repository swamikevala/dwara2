package org.ishafoundation.dwaraapi.test_impl.tape.drive;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.ishafoundation.dwaraapi.tape.drive.AbstractTapeDriveManagerImpl;
import org.ishafoundation.dwaraapi.tape.drive.status.DriveStatusDetails;
import org.ishafoundation.dwaraapi.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.test_impl.db.dao.Test_DataTransferElementDao;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_DataTransferElement;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_MtStatus;
import org.ishafoundation.dwaraapi.test_impl.entrypoint.resource.mapper.Test_TapeObjectsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | test" })
public class Test_TapeDriveManagerImpl extends AbstractTapeDriveManagerImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(Test_TapeDriveManagerImpl.class);

	@Autowired
	private TapedriveDao tapedriveDao;
	
	@Autowired
	private Test_DataTransferElementDao test_DataTransferElementDao;
		
	@Autowired
	private Test_TapeObjectsMapper test_TapeObjectsMapper;
		
	// To write Nth medialibrary the tape head should be pointing at file Number N
	// For e.g., if 5 medialibrary already in volume and to write the 6th mediaLibrary on tape, we need to position tapeHead on FileNumber = 5 - Remember Tape fileNumbers starts with 0
	// Reference - http://etutorials.org/Linux+systems/how+linux+works/Chapter+13+Backups/13.6+Tape+Drive+Devices/
	@Override
	public DriveStatusDetails setTapeHeadPositionForWriting(int tapelibraryId, int dataTransferElementNo) {
		
		return null;
	}

	// if blockNo is not requested to be seeked...
	@Override
	public DriveStatusDetails setTapeHeadPositionForReading(int tapelibraryId, int dataTransferElementNo, int blockNumberToSeek) {
		return null;
	}

	// drivename has to be unique even on different libraries... so need to pass tapelibraryid???
	@Override
	protected MtStatus getMtStatus(int tapelibraryId, String driveName){
		MtStatus mtStatus = null;
		
		Tapedrive tapedrive = tapedriveDao.findByTapelibraryIdAndDeviceWwidContaining(tapelibraryId, driveName);
		int driveElementAddress = tapedrive.getElementAddress();
		
		Test_DataTransferElement test_DataTransferElement = test_DataTransferElementDao.findBySNo(driveElementAddress);
		Test_MtStatus test_MtStatus = test_DataTransferElement.getTest_MtStatus();
		
		mtStatus = test_TapeObjectsMapper.getMtStatus(test_MtStatus);
		return mtStatus;
	}

}
