package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassPropertyKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassProperty;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassPropertyDao extends CrudRepository<LibraryclassProperty, LibraryclassPropertyKey> {
	
}