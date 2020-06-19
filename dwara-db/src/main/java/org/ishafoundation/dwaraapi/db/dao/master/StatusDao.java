package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.reference.Status;
import org.springframework.data.repository.CrudRepository;

public interface StatusDao extends CrudRepository<Status,Integer> {


}