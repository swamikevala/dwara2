package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.repository.CrudRepository;

public interface JobDao extends CrudRepository<Job,Integer> {
	
	List<Job> findAllByStatusOrderById(Status status);
//	@Query(value="SELECT * FROM job where status_id = 1 order by job_id", nativeQuery=true)
//	List<Job> getAllJobsToBeProcessed();

	List<Job> findAllByJobRefId(int parentJobId); // Find all dependent jobs of a particular job
	
	List<Job> findAllByStatusAndProcessingtaskIdIsNotNullOrderById(Status status); // TODO: Explain why Jobref is null? 
	
//	List<Job> findAllBySubrequestIdOrderById(int subrequestId);
//	
//	List<Job> findAllBySubrequestRequestActionAndStatus(Action action, Status status);
//	
//	Job findTopByTapeBarcodeOrderByIdDesc(String tapeBarcode);
	
	//Job findById(int jobId);
}