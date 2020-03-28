package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassRequesttypeUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassRequesttypeUserDao extends CrudRepository<LibraryclassRequesttypeUser,LibraryclassRequesttypeUserKey> {
	
	List<LibraryclassRequesttypeUser> findAllByLibraryclassIdAndRequesttypeId(int libraryclassId, int requesttypeId);
	
	List<LibraryclassRequesttypeUser> findAllByRequesttypeIdAndUserId(int requesttypeId, int userId);
}
