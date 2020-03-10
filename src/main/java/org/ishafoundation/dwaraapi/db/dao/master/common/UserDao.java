package org.ishafoundation.dwaraapi.db.dao.master.common;

import org.ishafoundation.dwaraapi.db.model.master.common.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User,Integer> {

	User findByName(String name);
}