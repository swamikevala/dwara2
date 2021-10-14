package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.ImportPayload;
import org.springframework.data.repository.CrudRepository;

public interface ImportPayloadDao extends CrudRepository<ImportPayload, Integer> {

}