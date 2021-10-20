package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.transactional.custom.JobCustom;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface JobDao extends CrudRepository<Job,Integer>,JobCustom {
	
	public static Map<String, JobDaoQueryProps> executorName_queryProps_map = new HashMap<String, JobDaoQueryProps>();
	
	// used for toLoad api - get all jobs with volume id and storagetask is not null and status is queued
//	List<Job> findAllByVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status status);
//	List<Job> findAllByGroupVolumeIdIsNotNullAndStoragetaskActionIdIsNotNullAndStatus(Status status);
	List<Job> findAllByStoragetaskActionIdIsNotNullAndStatusOrderById(Status status);
	
	List<Job> findTop2ByStoragetaskActionIdIsNotNullAndRequestActionIdAndStatusOrderByRequestId(Action action, Status status);
	
	List<Job> findAllByStoragetaskActionIdIsNotNullAndRequestActionIdIsNotAndStatusOrderById(Action action, Status status);
	
	List<Job> findAllByStatusOrderById(Status status);
	
	List<Job> findAllByStatusAndProcessingtaskIdInOrderById(Status status, Collection<String> processingTaskList, Pageable pageable);
	
	List<Job> findAllByStatusAndProcessingtaskIdIsNotNullOrderById(Status status); 
	
	long countByStoragetaskActionIdInAndStatus(Collection<Action> tapedrivemappingOrInitialize, Status status);
	
	// volume_id only for storagetasks, so storagetasks as a filter not needed - applicable for any storagetask like write/verify/restore
	// completed at gets updated only when status=completed, but is Null and gets picked up by the query...
	Job findTopByStoragetaskActionIdIsNotNullAndVolumeIdAndStatusAndCompletedAtIsNotNullOrderByCompletedAtDesc(String volumeId, Status status);
	
	Job findTopByStoragetaskActionIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Action action);
	
	Job findByStoragetaskActionIdIsNotNullAndVolumeIdAndStatus(String volumeId, Status status); // we need to pass only inprogress status here...
	
	// Used in determining tapeusageStatus
	long countByVolumeIdAndStatusAndStoragetaskActionIdIsNotNull(String volumeId, Status status);

	long countByGroupVolumeIdAndStatusAndStoragetaskActionIdIsNotNull(String volumeId, Status status);
	
	long countByVolumeIdAndStatusAndStoragetaskActionIdIsNotNullAndRequestActionId(String volumeId, Status status, Action action);
	
	long countByGroupVolumeIdAndStatusAndStoragetaskActionIdIsNotNullAndRequestActionId(String groupVolumeId, Status status, Action action);
	
//	List<Job> findAllBySubrequestIdOrderById(int subrequestId);
//	
//	List<Job> findAllBySubrequestRequestActionAndStatus(Action action, Status status);
//	
//	Job findTopByTapeBarcodeOrderByIdDesc(String tapeBarcode);
	
	//Job findById(int jobId);
	
	Job findByRequestIdAndProcessingtaskId(int requestId, String processingtaskId); // TODO : Note this could be a list too if there are failed jobs...
	
	Job findByRequestIdAndInputArtifactIdAndStoragetaskActionIdAndGroupVolumeCopyId(int requestId, int artifactId, Action action, Integer copyNumber);
	
	List<Job> findAllByRequestId(int requestId);
	
	Job findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(int requestId, Integer inputArtifactId,  String flowelementId, String groupVolumeId);
	
	List<Job> findAllByRequestIdAndInputArtifactIdAndFlowelementId(int requestId, Integer inputArtifactId,  String flowelementId);
	
	List<Job> findAllByRequestIdAndInputArtifactId(int requestId, Integer inputArtifactId);
	
	// Used for LtoWala
	List<Job> findAllByCompletedAtBetweenAndStoragetaskActionIdAndGroupVolumeCopyId(LocalDateTime startDateTime, LocalDateTime endDateTime, Action action, Integer copyNumber);
	
	Job findByFlowelementIdAndStatus(int id, Status status); // RequestId could be different if we are rerunning...
	
	Job findByRequestIdAndFlowelementId(int requestId, String flowelementId);
	
	// SELECT * FROM dwara.job where started_at > '2021-01-17 01:00:00.000000' and status = 'failed' and processingtask_id in('video-mkv-pfr-metadata-extract', 'video-proxy-low-gen');
	List<Job> findAllByStartedAtBetweenAndStatusAndProcessingtaskIdInOrderById(LocalDateTime startDateTime, LocalDateTime endDateTime, Status status, Collection<String> processingTaskList);
	
	List<Job> findAllByRequestIdAndInputArtifactIdAndStoragetaskActionIdAndStatus(int requestId, int artifactId, Action action, Status status);
}