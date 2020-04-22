package org.ishafoundation.dwaraapi.test_impl.db.dao;

import java.util.List;

import org.ishafoundation.dwaraapi.test_impl.db.model.Test_DataTransferElement;
import org.springframework.data.repository.CrudRepository;

public interface Test_DataTransferElementDao extends CrudRepository<Test_DataTransferElement,Integer> {

	Test_DataTransferElement findBySNo(int sNo);
	
	List<Test_DataTransferElement> findAllByTapelibraryId(int tapelibraryId);
	
}