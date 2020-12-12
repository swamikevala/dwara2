package org.ishafoundation.dwaraapi.db.dao.mocks;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.mocks.MockDataTransferElement;
import org.springframework.data.repository.CrudRepository;

public interface MockDataTransferElementDao extends CrudRepository<MockDataTransferElement,Integer> {

	MockDataTransferElement findBysNum(int sNo);
	
	MockDataTransferElement findByTapedriveUid(String tapedriveUid);
	
	List<MockDataTransferElement> findAllByTapelibraryUid(String tapelibraryUid);
	
}