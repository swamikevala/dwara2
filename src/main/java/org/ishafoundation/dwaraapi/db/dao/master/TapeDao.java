package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.springframework.data.repository.CrudRepository;

public interface TapeDao extends CrudRepository<Tape,Integer> {

	List<Tape> findAllByTapesetIdAndFinalizedIsFalse(int tapeset_id);
//	@Query(value="SELECT * FROM tape where tapeset_id=?1 and finalized=false", nativeQuery=true)
//	List<Tape> getWritableTapes(int tapeset_id);
	
	List<Tape> findAllByTapesetId(int tapeset_id);
}