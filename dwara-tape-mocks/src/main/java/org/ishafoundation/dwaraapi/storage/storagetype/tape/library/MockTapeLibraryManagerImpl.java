package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.mocks.MockDataTransferElementDao;
import org.ishafoundation.dwaraapi.db.dao.mocks.MockMtStatusDao;
import org.ishafoundation.dwaraapi.db.dao.mocks.MockStorageElementDao;
import org.ishafoundation.dwaraapi.db.model.mocks.MockDataTransferElement;
import org.ishafoundation.dwaraapi.db.model.mocks.MockMtStatus;
import org.ishafoundation.dwaraapi.db.model.mocks.MockStorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.TapeDriveStatusCode;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.storage.storagetype.tape.mapper.MockZmockEntityObjectsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
//@Profile("!default") works as well
@Profile({ "dev | stage" })
public class MockTapeLibraryManagerImpl extends AbstractTapeLibraryManagerImpl {

	@Autowired
	private MockDataTransferElementDao mockDataTransferElementDao;
	
	@Autowired
	private MockMtStatusDao mockMtStatusDao;
	
	@Autowired
	private MockStorageElementDao mockStorageElementDao;
	
	@Autowired
	private MockZmockEntityObjectsMapper mockTapeObjectsMapper;
	
	@Override
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo) {
		MockStorageElement mockStorageElement= mockStorageElementDao.findBySNo(seSNo);
		String volumeTag = mockStorageElement.getVolumeTag();
		mockStorageElement.setEmpty(true);
		mockStorageElement.setVolumeTag(null);
		mockStorageElementDao.save(mockStorageElement);
		
		MockDataTransferElement mockDataTransferElement = mockDataTransferElementDao.findBySNo(driveSNo);
		mockDataTransferElement.setStorageElementNo(seSNo);
		mockDataTransferElement.setEmpty(false);
		mockDataTransferElement.setVolumeTag(volumeTag);
		
		MockMtStatus mockMtStatus = mockDataTransferElement.getMockMtStatus();
		mockMtStatus.setReady(true);
		mockMtStatus.setStatusCode(TapeDriveStatusCode.EOF);
		mockMtStatus.setFileNumber(1);
		mockMtStatus.setBlockNumber(0);
		
		// TODO: This is not good. We shouldn't explicitly saving this. The association is supposed to have MtStatus saved as well on saving DataTransferElement. 
		// For some reason works when running on a container but not working on running as tests...
		mockMtStatusDao.save(mockMtStatus); 
		mockDataTransferElementDao.save(mockDataTransferElement);
		return true;
	}

	@Override
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo) {
		MockDataTransferElement mockDataTransferElement = mockDataTransferElementDao.findBySNo(driveSNo);
		String volumeTag = mockDataTransferElement.getVolumeTag();
		
		mockDataTransferElement.setStorageElementNo(null);
		mockDataTransferElement.setEmpty(true);
		mockDataTransferElement.setVolumeTag(null);

		
		MockMtStatus mockMtStatus = mockDataTransferElement.getMockMtStatus();
		mockMtStatus.setReady(false);
		mockMtStatus.setStatusCode(TapeDriveStatusCode.DR_OPEN);
		mockMtStatus.setFileNumber(-1);
		mockMtStatus.setBlockNumber(-1);
		
		// TODO: This is not good. We shouldn't explicitly saving this. The association is supposed to have MtStatus saved as well on saving DataTransferElement. 
		// For some reason works when running on a container but not working on running as tests...		
		mockMtStatusDao.save(mockMtStatus); 
		mockDataTransferElementDao.save(mockDataTransferElement);


		MockStorageElement mockStorageElement= mockStorageElementDao.findBySNo(seSNo);
		mockStorageElement.setEmpty(false);
		mockStorageElement.setVolumeTag(volumeTag);
		mockStorageElementDao.save(mockStorageElement);
		return true;
	}
	
	@Override
	public MtxStatus getMtxStatus(String tapeLibraryName){
		MtxStatus mtxStatus = new MtxStatus();
		mtxStatus.setStorageChangerName(tapeLibraryName);
		
		List<DataTransferElement> dteList = new ArrayList<DataTransferElement>();
		List<MockDataTransferElement> mockDataTransferElementList = mockDataTransferElementDao.findAllByTapelibraryUid(tapeLibraryName);
		for (MockDataTransferElement mockDataTransferElement : mockDataTransferElementList) {
			dteList.add(mockTapeObjectsMapper.getDataTransferElement(mockDataTransferElement));
		}
		mtxStatus.setDteList(dteList);
		
		List<StorageElement> seList = new ArrayList<StorageElement>();
		List<MockStorageElement> mockStorageElementList = mockStorageElementDao.findAllByTapelibraryUid(tapeLibraryName);
		for (MockStorageElement mockStorageElement : mockStorageElementList) {
			seList.add(mockTapeObjectsMapper.getStorageElement(mockStorageElement));
		}
		mtxStatus.setSeList(seList);
//		mtxStatus.setNoOfIESlots(noOfIESlots);
		mtxStatus.setNoOfDrives(mtxStatus.getDteList().size());
		mtxStatus.setNoOfSlots(mtxStatus.getSeList().size());
		return mtxStatus;
	}

	@Override
	public boolean unload(String tapeLibraryName, int dataTransferElementSNo) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
