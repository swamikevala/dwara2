package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
import org.ishafoundation.dwaraapi.enumreferences.VolumeLifecyclestage;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;
import org.springframework.data.repository.CrudRepository;

public interface VolumeDao extends CrudRepository<Volume,String> {

	List<Volume> findAllByGroupRefIdAndFinalizedIsFalseAndHealthstatusAndLifecyclestageOrderByIdAsc(String volumerefId, VolumeHealthStatus volumeHealthstatus, VolumeLifecyclestage volumeLifecyclestage);
		
	List<Volume> findAllByType(Volumetype volumetype);
	
	List<Volume> findAllByStoragetypeAndType(Storagetype storagetype, Volumetype volumetype);
}