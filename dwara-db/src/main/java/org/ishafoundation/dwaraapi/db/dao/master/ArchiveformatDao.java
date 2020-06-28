package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.springframework.data.repository.CrudRepository;

public interface ArchiveformatDao extends CrudRepository<Archiveformat, String> {
	

}