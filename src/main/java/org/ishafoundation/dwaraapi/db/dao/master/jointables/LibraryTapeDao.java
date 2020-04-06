package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.springframework.data.repository.CrudRepository;

public interface LibraryTapeDao extends CrudRepository<LibraryTape,Integer> {
	
	//LibraryTape findByLibraryIdAndCopyNumber(int libraryId, int copyNumber);

}