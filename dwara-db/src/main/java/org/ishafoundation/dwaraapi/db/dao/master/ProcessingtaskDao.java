package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.springframework.data.repository.CrudRepository;

public interface ProcessingtaskDao extends CrudRepository<Processingtask,String> {

}