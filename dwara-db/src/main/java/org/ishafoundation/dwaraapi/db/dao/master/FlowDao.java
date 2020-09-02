package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.springframework.data.repository.CrudRepository;

public interface FlowDao extends CrudRepository<Flow,String> {

}