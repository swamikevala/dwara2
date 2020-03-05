package org.ishafoundation.dwaraapi.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.restore.FileParams;
import org.ishafoundation.dwaraapi.api.resp.restore.SubrequestResp;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class RestoreContoller {
	
	Logger logger = LoggerFactory.getLogger(RestoreContoller.class);

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private SubrequestDao subrequestDao;	
	
	@Autowired
	private RequesttypeDao requesttypeDao;	
	
	@Autowired
	private JobDao jobDao;	

	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private ApplicationContext applicationContext;

	
//	@ApiOperation(value = "Lists all the mount points", response = List.class)
//	@GetMapping("/getAllMountPoints")
//	public ResponseEntity<List<String>> getAllMountPoints() {
//		List<String> mountPointsList = nodeumConfiguration.getRestoreMounts();
//		if (mountPointsList.size() > 0) {
//			return ResponseEntity.ok(mountPointsList);
//		} else {
//			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
//		}
//	}

//	@ApiOperation(value = "Gets the details of all the files requested in fileIdsListAsCSV")
//	@GetMapping("/getFileDetails")
//	public ResponseEntity<List<MediaLibraryFile>> getFileDetails(@RequestParam String fileIdsListAsCSV) {
//		List<MediaLibraryFile> mediaLibraryFileList = new ArrayList<MediaLibraryFile>();
//		List<String> fileIdsList = Arrays.asList(fileIdsListAsCSV.split(","));
//		for (Iterator<String> iterator = fileIdsList.iterator(); iterator.hasNext();) {
//			String fileId = (String) iterator.next();
//			MediaLibraryFile mediaLibraryFile = new MediaLibraryFile();
//			mediaLibraryFile.setFileId(Integer.parseInt(fileId));
//
//			try {
//				org.ishafoundation.dwaraapi.db.model.File file = fileDao.findById(Integer.parseInt(fileId)).get();
//				MediaLibrary mediaLibrary = mediaLibDao.findById(file.getMediaLibraryId()).get();
//				
//				mediaLibraryFile.setMediaLibraryFolderName(mediaLibrary.getFolderName());
//				mediaLibraryFile.setMediaLibrarysize(mediaLibrary.getTotalSizeBytes());
//
//			} catch (Throwable e) {
//				mediaLibraryFile.setMediaLibraryFolderName("Invalid file Id");
//				logger.error("Unable to load file for file Id " + fileId + " : " + e.getMessage());
//			}
//			mediaLibraryFileList.add(mediaLibraryFile);					
//		}
//
//		return ResponseEntity.ok(mediaLibraryFileList);
//	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @param mountPoint
	 * @param folderNameToGroupRestoredFiles
	 * @param fileIdsListAsCSV
	 * @param copyNumber - We can choose one of the three copies 1/2/3. By default we restore it from 1. 
	 * If 1 tape is corrupted, we dont have to wait for 1 to be written first to have it used for restoration. 
	 * @return
	 */
	@ApiOperation(value = "Restores the list of files requested in folderIdsListAsCSV into the mountPoint grouped under the dir folderNameToGroupRestoredFiles")
	@PostMapping("/restore")
	public org.ishafoundation.dwaraapi.api.resp.restore.Response restore(@RequestBody org.ishafoundation.dwaraapi.api.req.restore.UserRequest userRequest){
    	org.ishafoundation.dwaraapi.api.resp.restore.Response response = new org.ishafoundation.dwaraapi.api.resp.restore.Response();
		try {
	    	Requesttype requesttype = requesttypeDao.findByName("RESTORE");
	    	int requesttypeId = requesttype.getRequesttypeId();
	    	
	    	long requestedAt = System.currentTimeMillis();
	    	String requestedBy = getUserFromContext();
	    	
			Request request = new Request();
			request.setRequesttypeId(requesttypeId);
	    	request.setCopyNumber(userRequest.getCopyNumber());
	    	request.setTargetvolumeId(userRequest.getTargetvolumeId());
	    	request.setOutputFolder(userRequest.getOutputFolder());
			request.setRequestedAt(requestedAt);
	    	request.setRequestedBy(requestedBy);
	    	logger.debug("DB Request Creation");
	    	request = requestDao.save(request);
	    	int requestId = request.getRequestId();
	    	logger.debug("DB Request Creation - Success " + requestId);

	    	List<SubrequestResp> subrequestRespList = new ArrayList<SubrequestResp>();
	    	
	    	List<FileParams> fileParamsList = userRequest.getFileParams();
	    	for (Iterator<FileParams> iterator2 = fileParamsList.iterator(); iterator2.hasNext();) {
				FileParams nthFileParams = (FileParams) iterator2.next();
				
				int fileId = nthFileParams.getFileId();
				
		    	int statusId = Status.QUEUED.getStatusId();

		    	Subrequest subrequest = new Subrequest();
		    	subrequest.setRequestId(requestId);
		    	subrequest.setFileId(fileId);
		    	subrequest.setOptimizeTapeAccess(nthFileParams.isOptimizeTapeAccess());
		    	subrequest.setPriority(nthFileParams.getPriority());
		    	subrequest.setStatusId(statusId);

		    	logger.debug("DB Subrequest Creation");
		    	subrequest = subrequestDao.save(subrequest);
		    	logger.debug("DB Subrequest Creation - Success " + subrequest.getRequestId());

				createJobTableRows(subrequest, requesttypeId);
				
				
		    	org.ishafoundation.dwaraapi.api.resp.restore.Subrequest systemGeneratedSubRequestForResponse = new org.ishafoundation.dwaraapi.api.resp.restore.Subrequest();
		    	systemGeneratedSubRequestForResponse.setFileId(fileId);
		    	systemGeneratedSubRequestForResponse.setSubrequestId(subrequest.getSubrequestId());
		    	systemGeneratedSubRequestForResponse.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
		    	systemGeneratedSubRequestForResponse.setPriority(subrequest.getPriority());
		    	systemGeneratedSubRequestForResponse.setRequestId(requestId);
		    	systemGeneratedSubRequestForResponse.setStatusId(Status.QUEUED.getStatusId());
		    	
		    	org.ishafoundation.dwaraapi.api.resp.restore.SubrequestResp systemGeneratedSubRequestRespForResponse = new org.ishafoundation.dwaraapi.api.resp.restore.SubrequestResp();
		    	
		    	systemGeneratedSubRequestRespForResponse.setResponseCode(200);
		    	systemGeneratedSubRequestRespForResponse.setResponseMessage("Resp message");
		    	systemGeneratedSubRequestRespForResponse.setResponseType("Resp type");
		    	systemGeneratedSubRequestRespForResponse.setSubrequest(systemGeneratedSubRequestForResponse);
				
				
		    	subrequestRespList.add(systemGeneratedSubRequestRespForResponse);
			}
	    	
	    	org.ishafoundation.dwaraapi.api.resp.restore.Request requestForResponse = new org.ishafoundation.dwaraapi.api.resp.restore.Request();
	    	requestForResponse.setCopyNumber(userRequest.getCopyNumber());
	    	requestForResponse.setOutputFolder(userRequest.getOutputFolder());
	    	requestForResponse.setRequestedAt(requestedAt);
	    	requestForResponse.setRequestedBy(requestedBy);
	    	requestForResponse.setRequestId(requestId);
	    	requestForResponse.setRequesttypeId(requesttypeId);
	    	requestForResponse.setSubrequestResp(subrequestRespList);
	    	requestForResponse.setTargetvolumeId(userRequest.getTargetvolumeId());
	    	
	    	response.setRequest(requestForResponse);
	    	
	    	response.setResponseCode(200);
			response.setResponseType("Success");
			response.setResponseMessage("UserRequest(s) Submitted Successfully");
					

		} catch (Throwable e) {
			String errorMsg = "Unable to trigger the restore job - " + e.getMessage();
			logger.error(errorMsg, e);
			response.setResponseCode(111);
			response.setResponseType("Failed");
			response.setResponseMessage(errorMsg);
		}
		return response;		
	}
	
	private String getUserFromContext() {
		return "";//SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
    private void createJobTableRows(Subrequest subrequest, int requesttypeId) {
    	List<Job> jobList = jobManager.createJobs(requesttypeId, 0, subrequest, null);
    	
    	logger.debug("DB Job rows Creation");   
    	jobDao.saveAll(jobList);
    	logger.debug("DB Job rows Creation - Success");
    }	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@ApiOperation(value = "Get List of all Restore Requests filtered by its status or context")
//    @PostMapping("/getAllRestoreRequests")
//    public RestoreRequestListResp getAllRestoreRequests(@RequestParam String status){
//    	List<RestoreRequest> restoreRequestList = null;
//    	if(status.equals(MediaLibraryStatusFilter.PROCESSING.toString()))
//    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findProcessing();
//    	else if(status.equals(MediaLibraryStatusFilter.COMPLETED.toString())) 
//    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findCompleted();
//    	else if(status.equals(MediaLibraryStatusFilter.ALL.toString()))
//    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findAll();
//    	else
//    		return null;
//    	
//    	RestoreRequestListResp restoreRequestListResp = new RestoreRequestListResp();
//    	restoreRequestListResp.setResponseCode(200);
//    	restoreRequestListResp.setResponseType("Success");
//    	restoreRequestListResp.setResponseMessage("Retrieved Restore Jobs List Successfully");
//    	
//    	restoreRequestListResp.setRestoreRequestList(restoreRequestList);
//    	restoreRequestListResp.setTotalNoOfRecords(restoreRequestList.size());
//    	restoreRequestListResp.setStartRecord(0);
//    	restoreRequestListResp.setEndRecord(restoreRequestList.size());
//        return restoreRequestListResp;
//    }

//	@ApiOperation(value = "Gets a specific Restore Requests details")
//    @GetMapping("/{restoreRequestId}/getRestoreRequestDetails")
//    public RestoreRequestDetailsResp getRestoreRequestDetails(@PathVariable("restoreRequestId") int restoreRequestId) {
//    	RestoreRequestDetailsResp restoreRequestDetailsResp = new RestoreRequestDetailsResp();
//    	RestoreRequest restoreRequest = restoreRequestDao.findById(restoreRequestId).get();
//    	List<RestoreJob> restoreJobList = restoreJobDao.findAllByRestoreRequestId(restoreRequestId);
//    	restoreRequestDetailsResp.setRestoreJobList(restoreJobList);
//    	restoreRequestDetailsResp.setRestoreRequest(restoreRequest);
//		return restoreRequestDetailsResp;
//    }
	
//	@ApiOperation(value = "Cancels a specific restore request and all the folders/job requested in it")
//	@PostMapping("/{restoreRequestId}/cancelRestoreRequest") 
//	//public ResponseEntity<RestoreRequestDetailsResp> cancelRestoreRequest(@PathVariable("restoreRequestId") int restoreRequestId){
//	public RestoreRequestDetailsResp cancelRestoreRequest(@PathVariable("restoreRequestId") int toBeCancelledRestoreRequestId){
//		
//		// create a cancel request for auditing purpose
//		RestoreRequest cancelRestoreRequest = new RestoreRequest();
//		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_REQUEST call");
//		cancelRestoreRequest.setType(RestoreType.CANCEL.toString());
//		cancelRestoreRequest.setTypeId(RestoreType.CANCEL.getRestoreTypeId());
//		cancelRestoreRequest.setRestoreRequestIdRef(toBeCancelledRestoreRequestId);
//		cancelRestoreRequest.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
//		cancelRestoreRequest.setDate(Calendar.getInstance().getTimeInMillis());
//		cancelRestoreRequest.setStatus(Status.IN_PROGRESS.toString());
//		cancelRestoreRequest.setStatusId(Status.IN_PROGRESS.getStatusId());
//		cancelRestoreRequest = restoreRequestDao.save(cancelRestoreRequest);
//		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_REQUEST call - " + cancelRestoreRequest.getRestoreRequestId());		
//		
//    	RestoreRequestDetailsResp restoreRequestDetailsResp = new RestoreRequestDetailsResp();
//    	
//    	List<RestoreJob> restoreJobLatestList = new ArrayList<RestoreJob>();
//		List<RestoreJob> restoreJobList = restoreJobDao.findAllByRestoreRequestId(toBeCancelledRestoreRequestId);
//		boolean isCancelled = false;
//		boolean isAborted = false;
//
//		for (Iterator<RestoreJob> iterator = restoreJobList.iterator(); iterator.hasNext();) {
//			RestoreJob nthRestoreJob = (RestoreJob) iterator.next();
//			
//			nthRestoreJob = cancelRestoreJob_Internal(nthRestoreJob.getRestoreJobId());
//			String nthRestoreJobStatus = nthRestoreJob.getStatus();
//			if (nthRestoreJobStatus.equals(Status.ABORTED.toString())) {
//				isCancelled = true;
//			}else if (nthRestoreJobStatus.equals(Status.ABORTED.toString())) {
//				isAborted = true;
//			}
//			
//			restoreJobLatestList.add(nthRestoreJob);
//		}
//		restoreRequestDetailsResp.setRestoreJobList(restoreJobLatestList);
//		
//		// set the status to cancelled/aborted for the original request 
//		Status originalRestoreRequestNewStatus = null;
//		if (isAborted) {
//			originalRestoreRequestNewStatus = Status.ABORTED;
//		}else if(isCancelled) {
//			originalRestoreRequestNewStatus = Status.CANCELLED;
//		}
//		
//		RestoreRequest originalRestoreRequest = restoreRequestDao.findById(toBeCancelledRestoreRequestId).get();
//		originalRestoreRequest.setStatus(originalRestoreRequestNewStatus.toString());
//		originalRestoreRequest.setStatusId(originalRestoreRequestNewStatus.getStatusId());
//		logger.debug("DB RestoreRequest - Status Updation");
//		restoreRequestDao.save(originalRestoreRequest);
//		logger.debug("DB RestoreRequest - Status Updation - " + toBeCancelledRestoreRequestId);
//
//		restoreRequestDetailsResp.setRestoreRequest(originalRestoreRequest);
//
//		// set the status to complete for the cancellation request
//		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_REQUEST call");
//		cancelRestoreRequest.setStatus(Status.COMPLETED.toString());
//		cancelRestoreRequest.setStatusId(Status.COMPLETED.getStatusId());
//		cancelRestoreRequest = restoreRequestDao.save(cancelRestoreRequest);
//		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_REQUEST call - " + cancelRestoreRequest.getRestoreRequestId());	
//		//return ResponseEntity.ok(restoreRequestDetailsResp);
//		return restoreRequestDetailsResp;
//	}
	
//	@ApiOperation(value = "Get Details of a specific restore Job")
//	@GetMapping("/{restoreJobId}/getRestoreJobDetails")
//	public RestoreJobDetailsResp getRestoreJobDetails(@PathVariable("restoreJobId") int restoreJobId) {
//		RestoreJobDetailsResp restoreJobDetails = new RestoreJobDetailsResp();
//		
//		RestoreJob restoreJob = restoreJobDao.findById(restoreJobId).get();
//		restoreJobDetails.setRestoreJob(restoreJob);
//
//		RestoreTapeJob restoreTapeJob = restoreTapeJobDao.findByRestoreJobId(restoreJobId);
//		restoreJobDetails.setRestoreTapeJob(restoreTapeJob);
//		return restoreJobDetails;
//	}
//	
//
//	@ApiOperation(value = "Cancels a restore Job")
//	@PostMapping("/{restoreJobId}/cancelRestoreJob") 
//	public RestoreJob cancelRestoreJob(@PathVariable("restoreJobId") int restoreJobId){
//		// create a cancel job request for auditing purpose
//		RestoreRequest cancelRestoreJob_Request = new RestoreRequest();
//		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_JOB call");
//		cancelRestoreJob_Request.setType(RestoreType.CANCEL.toString());
//		cancelRestoreJob_Request.setTypeId(RestoreType.CANCEL.getRestoreTypeId());
//		cancelRestoreJob_Request.setRestoreJobIdRef(restoreJobId);
//		cancelRestoreJob_Request.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
//		cancelRestoreJob_Request.setDate(Calendar.getInstance().getTimeInMillis());
//		cancelRestoreJob_Request.setStatus(Status.IN_PROGRESS.toString());
//		cancelRestoreJob_Request.setStatusId(Status.IN_PROGRESS.getStatusId());
//		cancelRestoreJob_Request = restoreRequestDao.save(cancelRestoreJob_Request);
//		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_JOB call - " + cancelRestoreJob_Request.getRestoreRequestId());		
//		
//		RestoreJob restoreJob = cancelRestoreJob_Internal(restoreJobId);
//		
//		// set the status to complete for the cancellation request
//		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_JOB call");
//		cancelRestoreJob_Request.setStatus(Status.COMPLETED.toString());
//		cancelRestoreJob_Request.setStatusId(Status.COMPLETED.getStatusId());
//		cancelRestoreJob_Request = restoreRequestDao.save(cancelRestoreJob_Request);
//		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_JOB call - " + cancelRestoreJob_Request.getRestoreRequestId());
//		
//		// TODO - What should be the status of the original restore request??
//		return restoreJob;
//	}
//	
//	private RestoreJob cancelRestoreJob_Internal(int restoreJobId){
//		RestoreJob restoreJob = restoreJobDao.findById(restoreJobId).get();
//		String currentStatus = restoreJob.getStatus();
//		Status newStatus = null;
//		// if its queued status
//		if(currentStatus.equals(Status.QUEUED.toString())) { 
//			newStatus = Status.CANCELLED;
//		}
//		else if(currentStatus.equals(Status.IN_PROGRESS.toString())) {
//			newStatus = nodeumStorageManager.cancel(restoreJob, 1);
//		}
//		
//		restoreJob.setStatus(newStatus.toString());
//		restoreJob.setStatusId(newStatus.getStatusId());		
//		restoreJob = restoreJobDao.save(restoreJob);
//
//		return restoreJob;
//	}
//	
//    @PostMapping("/diagnosticsAndRepairRestoreRequests")
//    public void diagnosticsAndRepairRestoreRequests(){
//    	List<RestoreRequest> rrl = restoreRequestDao.findByStatusId(Status.IN_PROGRESS.getStatusId());
//    	
//    }
	
}
