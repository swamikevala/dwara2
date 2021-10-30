package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.springframework.data.repository.CrudRepository;

public interface TRestoreBucketDao extends CrudRepository<TRestoreBucket,String> {
}
