package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.springframework.data.repository.CrudRepository;

public interface TapedriveDao extends CrudRepository<Tapedrive,Integer> {
	
	List<Tapedrive> findAllByStatus(String status);
	
	Tapedrive findByElementAddress(int elementAddress);
}