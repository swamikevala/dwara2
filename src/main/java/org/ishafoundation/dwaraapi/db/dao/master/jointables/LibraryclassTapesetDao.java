package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassTapesetKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassTapesetDao extends CrudRepository<LibraryclassTapeset,LibraryclassTapesetKey> {
	
	LibraryclassTapeset findByTaskId(int taskId);
	
	LibraryclassTapeset findByLibraryclassIdAndCopyNumber(int libraryclassId, int copyNumber);
}
