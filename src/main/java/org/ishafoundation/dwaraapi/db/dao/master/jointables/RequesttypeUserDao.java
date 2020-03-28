package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import org.ishafoundation.dwaraapi.db.keys.RequesttypeUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RequesttypeUser;
import org.springframework.data.repository.CrudRepository;

public interface RequesttypeUserDao extends CrudRepository<RequesttypeUser,RequesttypeUserKey> {

	RequesttypeUser findByRequesttypeIdAndUserId(int requesttypeId, int userId);
	
	//RequesttypeUser findByRequesttypeRequesttypeIdAndUserUserId(int requesttypeId, int userId);
}