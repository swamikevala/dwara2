package org.ishafoundation.dwaraapi.db.dao.master.storage;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.storage.Tape;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TapeDao extends CrudRepository<Tape,Integer> {
	
	@Query(value="SELECT * FROM tape where tapeset_id=?1 and finalized=false", nativeQuery=true)
	List<Tape> getWritableTapes(int tapeset_id);
	
	List<Tape> findAllByTapesetId(int tapeset_id);
	
}