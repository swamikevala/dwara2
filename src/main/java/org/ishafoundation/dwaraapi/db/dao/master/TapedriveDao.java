package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.springframework.data.repository.CrudRepository;

public interface TapedriveDao extends CrudRepository<Tapedrive,Integer> {
	
	List<Tapedrive> findAllByStatus(String status);
	
	Tapedrive findByTapelibraryNameAndElementAddress(String tapelibraryName, int elementAddress);
	
	Tapedrive findByDeviceWwidContaining(String wwid);
	
//	Tapedrive findByTapelibraryNameAndDeviceWwidContaining(String tapelibraryName, String wwid);
//	
//	Tapedrive findByTapelibraryIdAndElementAddress(int tapelibraryId, int elementAddress);
//	
//	Tapedrive findByTapelibraryIdAndDeviceWwidContaining(int tapelibraryId, String wwid);
	
}