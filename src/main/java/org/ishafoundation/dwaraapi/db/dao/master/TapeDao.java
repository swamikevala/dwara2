package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.springframework.data.repository.CrudRepository;

public interface TapeDao extends CrudRepository<Tape,Integer> {

	Tape findTopByTapesetIdAndFinalizedIsFalseOrderByIdAsc(int tapesetId);
	
	List<Tape> findAllByTapesetIdAndFinalizedIsFalse(int tapesetId);
//	@Query(value="SELECT * FROM tape where tapesetId=?1 and finalized=false", nativeQuery=true)
//	List<Tape> getWritableTapes(int tapesetId);
	
	List<Tape> findAllByTapesetId(int tapesetId);
	
	Tape findTopByOrderByIdDesc(); // when a format_tape action is triggered we need to add the formatted tape to our system with the most last tape's Id + 1...
}