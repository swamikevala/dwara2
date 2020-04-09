package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassActionUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassActionUser;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassActionUserDao extends CrudRepository<LibraryclassActionUser,LibraryclassActionUserKey> {
	
	List<LibraryclassActionUser> findAllByLibraryclassIdAndActionId(int libraryclassId, int actionId);
	
	List<LibraryclassActionUser> findAllByActionIdAndUserId(int actionId, int userId);
	
	List<LibraryclassActionUser> findAllByUserId(int userId);
}
