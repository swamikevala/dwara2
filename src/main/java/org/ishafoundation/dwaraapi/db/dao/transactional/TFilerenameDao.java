package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.keys.TFilerenameKey;
import org.ishafoundation.dwaraapi.db.model.transactional.TFilerename;
import org.springframework.data.repository.CrudRepository;

public interface TFilerenameDao extends CrudRepository<TFilerename,TFilerenameKey> {
	
	TFilerename findByTFilerenameKeySourcePathAndTFilerenameKeyOldFilename(String sourcePath, String oldFilename);
	
}
