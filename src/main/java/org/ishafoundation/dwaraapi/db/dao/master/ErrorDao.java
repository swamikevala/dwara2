package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Error;
import org.springframework.data.repository.CrudRepository;

public interface ErrorDao extends CrudRepository<Error,Integer> {


}