package org.ishafoundation.dwaraapi.db.dao.master;

import org.ishafoundation.dwaraapi.db.model.master.reference.Version;
import org.springframework.data.repository.CrudRepository;

public interface VersionDao extends CrudRepository<Version,String> {

	Version findTopByVersion();
}
