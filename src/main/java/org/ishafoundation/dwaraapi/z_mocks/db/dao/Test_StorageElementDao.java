package org.ishafoundation.dwaraapi.z_mocks.db.dao;

import java.util.List;

import org.ishafoundation.dwaraapi.z_mocks.db.model.Test_StorageElement;
import org.springframework.data.repository.CrudRepository;

public interface Test_StorageElementDao extends CrudRepository<Test_StorageElement,Integer> {

	Test_StorageElement findBySNo(int sNo);
	
	List<Test_StorageElement> findAllByTapelibraryName(String tapelibraryName);

}