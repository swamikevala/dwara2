package org.ishafoundation.dwaraapi.db.dao.master.ingest;

import org.ishafoundation.dwaraapi.db.model.master.ingest.Scanfolder;
import org.springframework.data.repository.CrudRepository;

public interface ScanfolderDao extends CrudRepository<Scanfolder,Integer> {
	

}