package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationDao extends CrudRepository<Application,Integer> {


}