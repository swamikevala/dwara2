package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface JobDao extends CrudRepository<Job,Integer> {
	
//	@Query(value="SELECT * FROM job where status_id = 1 order by job_id", nativeQuery=true)
//	List<Job> getAllJobsToBeProcessed();

	List<Job> findAllByStatusAndJobRefIsNullOrderById(Status status);
	
	List<Job> findAllBySubrequestIdOrderById(int subrequestId);
	
	Job findByTaskIdAndSubrequestId(int taskId, int subrequestId);
	
	List<Job> findAllBySubrequestRequestActionAndStatus(Action action, Status status);
	
	Job findTopByTapeBarcodeOrderByIdDesc(String tapeBarcode);
	
	//Job findById(int jobId);
}