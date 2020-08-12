package org.ishafoundation.dwaraapi.storage.storagetype.tape.drive;

import org.ishafoundation.dwaraapi.db.dao.mocks.MockDataTransferElementDao;
import org.ishafoundation.dwaraapi.db.model.mocks.MockDataTransferElement;
import org.ishafoundation.dwaraapi.db.model.mocks.MockMtStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.DriveDetails;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.MtStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.mapper.MockZmockEntityObjectsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev | stage" })
public class MockTapeDriveManagerImpl extends TapeDriveManagerImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(MockTapeDriveManagerImpl.class);
	
	@Autowired
	private MockDataTransferElementDao mockDataTransferElementDao;
		
	@Autowired
	private MockZmockEntityObjectsMapper mockTapeObjectsMapper;
	
	// drivename has to be unique even on different libraries... so need to pass tapelibraryid???
	@Override
	public MtStatus getMtStatus(String driveName){
		MtStatus mtStatus = null;
		
		MockDataTransferElement mockDataTransferElement = mockDataTransferElementDao.findByTapedriveUid(driveName);
		MockMtStatus mockMtStatus = mockDataTransferElement.getMockMtStatus();
		
		mtStatus = mockTapeObjectsMapper.getMtStatus(mockMtStatus);
		return mtStatus;
	}

	@Override
	public boolean isTapeBlank(String dataTransferElementName) throws Exception {
		return true;
	}

	@Override
	public DriveDetails setTapeHeadPositionForWriting(String dataTransferElementName, int fileNumberToBePositioned) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DriveDetails setTapeHeadPositionForReading(String dataTransferElementName, int blockNumberToSeek)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DriveDetails setTapeHeadPositionForFormatting(String dataTransferElementName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DriveDetails setTapeHeadPositionForFinalizing(String dataTransferElementName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}



}
