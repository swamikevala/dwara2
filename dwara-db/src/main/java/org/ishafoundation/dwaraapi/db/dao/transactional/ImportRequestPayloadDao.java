package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.ImportRequestPayload;
import org.springframework.data.repository.CrudRepository;

public interface ImportRequestPayloadDao extends CrudRepository<ImportRequestPayload, Integer> {

}