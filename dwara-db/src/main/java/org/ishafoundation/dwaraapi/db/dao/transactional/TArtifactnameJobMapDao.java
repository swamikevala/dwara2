package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.keys.VolumeArtifactServerNameKey;
import org.ishafoundation.dwaraapi.db.model.transactional.TArtifactnameJobMap;
import org.springframework.data.repository.CrudRepository;

public interface TArtifactnameJobMapDao extends CrudRepository<TArtifactnameJobMap,VolumeArtifactServerNameKey> {
	
	TArtifactnameJobMap findByJobId(Integer jobId);
	
}