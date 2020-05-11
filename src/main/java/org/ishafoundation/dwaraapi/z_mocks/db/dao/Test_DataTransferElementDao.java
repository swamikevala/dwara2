package org.ishafoundation.dwaraapi.z_mocks.db.dao;

import java.util.List;

import org.ishafoundation.dwaraapi.z_mocks.db.model.Test_DataTransferElement;
import org.springframework.data.repository.CrudRepository;

public interface Test_DataTransferElementDao extends CrudRepository<Test_DataTransferElement,Integer> {

	Test_DataTransferElement findBySNo(int sNo);
	
	Test_DataTransferElement findByTapedriveId(int tapedriveId);
	
	List<Test_DataTransferElement> findAllByTapelibraryName(String tapelibraryName);
	
}