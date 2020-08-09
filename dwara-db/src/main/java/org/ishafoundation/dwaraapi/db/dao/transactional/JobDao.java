package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.Collection;
import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface JobDao extends CrudRepository<Job,Integer> {
	
	List<Job> findAllByStatusOrderById(Status status);
	
	List<Job> findAllByStatusAndProcessingtaskIdIsNotNullOrderById(Status status); 
	
	long countByStoragetaskActionIdInAndStatus(Collection<Action> tapedrivemappingOrFormat, Status status);
	
	// volume_id only for storagetasks so not needed as filter
	// completed at gets updated only when status=completed...
	Job findTopByVolumeIdOrderByCompletedAtDesc(String volumeId);
	
//	List<Job> findAllBySubrequestIdOrderById(int subrequestId);
//	
//	List<Job> findAllBySubrequestRequestActionAndStatus(Action action, Status status);
//	
//	Job findTopByTapeBarcodeOrderByIdDesc(String tapeBarcode);
	
	//Job findById(int jobId);
	
	Job findByRequestIdAndProcessingtaskId(int requestId, String processingtaskId);
}