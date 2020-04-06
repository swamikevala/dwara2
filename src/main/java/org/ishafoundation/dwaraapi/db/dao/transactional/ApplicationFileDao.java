package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ApplicationFile;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationFileDao extends CrudRepository<ApplicationFile,Integer> {


}