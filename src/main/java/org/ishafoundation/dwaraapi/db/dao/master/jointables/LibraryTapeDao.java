package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface LibraryTapeDao extends CrudRepository<LibraryTape,Integer> {
	
	//SELECT sum(library.total_size) FROM library_tape join library on library_tape.library_id = library.id where tape_id=12002;
	@Query("select sum(l.totalSize) from LibraryTape lt join Library l on lt.library.id=l.id where lt.tape.id = ?1")
	long findUsedSpaceOnTape(int tapeId);
}