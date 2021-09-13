package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.RoleUserKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RoleUser;
import org.springframework.data.repository.CrudRepository;

public interface RoleUserDao extends CrudRepository<RoleUser,RoleUserKey> {

	List<RoleUser> findAllByIdUserId(int userId);
}