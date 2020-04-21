package org.ishafoundation.dwaraapi.test_impl.tape.library;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.tape.drive.status.TapeDriveStatusCode;
import org.ishafoundation.dwaraapi.tape.library.AbstractTapeLibraryManagerImpl;
import org.ishafoundation.dwaraapi.tape.library.components.DataTransferElement;
import org.ishafoundation.dwaraapi.tape.library.components.StorageElement;
import org.ishafoundation.dwaraapi.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.test_impl.db.dao.Test_DataTransferElementDao;
import org.ishafoundation.dwaraapi.test_impl.db.dao.Test_StorageElementDao;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_DataTransferElement;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_MtStatus;
import org.ishafoundation.dwaraapi.test_impl.db.model.Test_StorageElement;
import org.ishafoundation.dwaraapi.test_impl.entrypoint.resource.mapper.Test_TapeObjectsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
//@Profile("!default") works as well
@Profile({ "dev | test" })
public class Test_TapeLibraryManagerImpl extends AbstractTapeLibraryManagerImpl {

	@Autowired
	private Test_DataTransferElementDao test_DataTransferElementDao;
	
	@Autowired
	private Test_StorageElementDao test_StorageElementDao;
	
	@Autowired
	private Test_TapeObjectsMapper test_TapeObjectsMapper;
	
	@Override
	public boolean load(String tapeLibraryName, int seSNo, int driveSNo) {
		Test_StorageElement test_StorageElement= test_StorageElementDao.findBySNo(seSNo);
		String volumeTag = test_StorageElement.getVolumeTag();
		test_StorageElement.setEmpty(true);
		test_StorageElement.setVolumeTag(null);
		test_StorageElementDao.save(test_StorageElement);
		
		Test_DataTransferElement test_DataTransferElement = test_DataTransferElementDao.findBySNo(driveSNo);
		test_DataTransferElement.setStorageElementNo(seSNo);
		test_DataTransferElement.setEmpty(false);
		test_DataTransferElement.setVolumeTag(volumeTag);
		
		Test_MtStatus test_MtStatus = test_DataTransferElement.getTest_MtStatus();
		test_MtStatus.setReady(true);
		test_MtStatus.setStatusCode(TapeDriveStatusCode.EOF);
		test_MtStatus.setFileNumber(1);
		test_MtStatus.setBlockNumber(0);
		
		//test_DataTransferElement.setTest_MtStatus(test_MtStatus);
		test_DataTransferElementDao.save(test_DataTransferElement);
		
		
		// update Test_StorageElement and Test_DataTransferElement tables...
		return false;
	}

	@Override
	public boolean unload(String tapeLibraryName, int seSNo, int driveSNo) {
		Test_DataTransferElement test_DataTransferElement = test_DataTransferElementDao.findBySNo(driveSNo);
		String volumeTag = test_DataTransferElement.getVolumeTag();
		
		test_DataTransferElement.setStorageElementNo(null);
		test_DataTransferElement.setEmpty(true);
		test_DataTransferElement.setVolumeTag(null);

		
		Test_MtStatus test_MtStatus = test_DataTransferElement.getTest_MtStatus();
		test_MtStatus.setReady(false);
		test_MtStatus.setStatusCode(TapeDriveStatusCode.DR_OPEN);
		test_MtStatus.setFileNumber(-1);
		test_MtStatus.setBlockNumber(-1);
		
		//test_DataTransferElement.setTest_MtStatus(test_MtStatus);
		test_DataTransferElementDao.save(test_DataTransferElement);


		Test_StorageElement test_StorageElement= test_StorageElementDao.findBySNo(seSNo);
		test_StorageElement.setEmpty(false);
		test_StorageElement.setVolumeTag(volumeTag);
		test_StorageElementDao.save(test_StorageElement);
		return false;
	}
	
	@Override
	protected MtxStatus getMtxStatus(String tapeLibraryName){
		int tapelibraryId = getTapeLibraryId(tapeLibraryName);
		MtxStatus mtxStatus = new MtxStatus();
		mtxStatus.setStorageChangerName(tapeLibraryName);
		
		List<DataTransferElement> dteList = new ArrayList<DataTransferElement>();
		List<Test_DataTransferElement> test_DataTransferElementList = test_DataTransferElementDao.findAllByTapelibraryId(tapelibraryId);
		for (Test_DataTransferElement test_DataTransferElement : test_DataTransferElementList) {
			dteList.add(test_TapeObjectsMapper.getDataTransferElement(test_DataTransferElement));
		}
		mtxStatus.setDteList(dteList);
		
		List<StorageElement> seList = new ArrayList<StorageElement>();
		List<Test_StorageElement> test_StorageElementList = test_StorageElementDao.findAllByTapelibraryId(tapelibraryId);
		for (Test_StorageElement test_StorageElement : test_StorageElementList) {
			seList.add(test_TapeObjectsMapper.getStorageElement(test_StorageElement));
		}
		mtxStatus.setSeList(seList);
//		mtxStatus.setNoOfSlots(noOfSlots);
//		mtxStatus.setNoOfIESlots(noOfIESlots);
//		mtxStatus.setNoOfDrives(noOfDrives);
		return mtxStatus;
	}
}
