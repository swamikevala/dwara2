package org.ishafoundation.dwaraapi.db.dao.mocks;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.mocks.MockStorageElement;
import org.springframework.data.repository.CrudRepository;

public interface MockStorageElementDao extends CrudRepository<MockStorageElement,Integer> {

	MockStorageElement findBysNo(int sNo);
	
	List<MockStorageElement> findAllByTapelibraryUid(String tapelibraryName);

}