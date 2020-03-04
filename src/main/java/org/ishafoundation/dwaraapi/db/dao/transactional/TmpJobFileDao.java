
package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TmpJobFile;
import org.springframework.data.repository.CrudRepository;

public interface TmpJobFileDao extends CrudRepository<TmpJobFile,Integer> {
	
	List<TmpJobFile> findAllByJobId(int jobId);
	
}