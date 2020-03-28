package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.springframework.data.repository.CrudRepository;

public interface TapeDao extends CrudRepository<Tape,Integer> {


}