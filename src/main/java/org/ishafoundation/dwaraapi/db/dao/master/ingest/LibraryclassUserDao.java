package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.ingest.LibraryclassUser;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassUserDao extends CrudRepository<LibraryclassUser,Integer> {

	List<LibraryclassUser> findAllByUserId(int userId);
	
	List<LibraryclassUser> findAllByLibraryclassId(int libraryclassId);
}