package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.api.exception.DwaraException;
import org.ishafoundation.dwaraapi.api.req.restore.FileParams;
import org.ishafoundation.dwaraapi.constants.Action;
import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.dao.master.TargetvolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.dao.view.V_RestoreFileDao;
import org.ishafoundation.dwaraapi.db.keys.V_RestoreFileKey;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.db.model.view.V_RestoreFile;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.ishafoundation.dwaraapi.tape.library.status.MtxStatus;
import org.ishafoundation.dwaraapi.utils.ObjectMappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class FileController {
	
	Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private V_RestoreFileDao v_RestoreFileDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private SubrequestDao subrequestDao;	
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private TargetvolumeDao targetvolumeDao;	
	
	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;	
	
	@Autowired
	private ObjectMappingUtil objectMappingUtil;
	

	@ApiOperation(value = "Gets the details of all the files requested in fileIdsListAsCSV")
	@GetMapping("/file")
	public List<org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File> getFiles(@RequestParam String ids, @RequestParam(required=false, defaultValue="1") int copyNumber, @RequestParam(defaultValue="false") boolean showDeleted) {
		List<org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File> fileListForResponse = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File>();
		List<String> fileIdsList = Arrays.asList(ids.split(","));
		for (Iterator<String> iterator = fileIdsList.iterator(); iterator.hasNext();) {
			String fileIdAsString = (String) iterator.next();
			int fileId = Integer.parseInt(fileIdAsString);
		
			fileListForResponse.add(getFile(fileId, copyNumber, showDeleted));
		}
		return fileListForResponse;
	}
	
	
	@ApiOperation(value = "Gets the details of the particular file")
	@GetMapping("/file/{fileId}")
	public org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File getFile(@PathVariable int fileId, @RequestParam(required=false, defaultValue="1") int copyNumber, @RequestParam(defaultValue="false") boolean showDeleted) {

		List<String> loadedTapeList = tapeLibraryManager.getAllLoadedTapesInTheLibraries();

		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.list);
		actionList.add(Action.restore);
		
		String requestedBy = getUserFromContext();
		int userId = userDao.findByName(requestedBy).getId();
		
		List<V_RestoreFile> v_RestoreFileList = v_RestoreFileDao.findAllByTapesetCopyNumberAndIdFileIdAndIdActionInAndIdUserId(copyNumber, fileId, actionList, userId);
		

		int id = 0;
		String filePathname = null;
		double fileSize = 0;
		String libraryclass = null;
		String barcode = null;
		boolean isDeleted = false;
		boolean listPermitted = false;
		boolean restorePermitted = false;
		boolean online = false;
		
		for (V_RestoreFile v_RestoreFile : v_RestoreFileList) {
			barcode = StringUtils.isBlank(barcode) ? v_RestoreFile.getTapeBarcode() : barcode;
			V_RestoreFileKey v_RestoreFileKey = v_RestoreFile.getId();
			id = v_RestoreFileKey.getFileId();
			filePathname = StringUtils.isBlank(filePathname) ? v_RestoreFile.getFilePathname() : filePathname;
			fileSize = v_RestoreFile.getFileSize();
			libraryclass = v_RestoreFile.getLibraryclassName();
			isDeleted = v_RestoreFile.isFileTapeDeleted();
			if(v_RestoreFileKey.getAction() == Action.list)
				listPermitted = true;
			if(v_RestoreFileKey.getAction() == Action.restore)
				restorePermitted = true;
			// TODO - how to arrive at targetVolumePermitted
			if(loadedTapeList.contains(barcode))
				online = true;				
		}
		org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File nthFile = null;
		if(!isDeleted || showDeleted) {
			nthFile = new org.ishafoundation.dwaraapi.entrypoint.resource.restore.list.File();
			nthFile.setListPermitted(listPermitted);
			if(listPermitted) {
				nthFile.setId(id);
				nthFile.setName(filePathname);
				nthFile.setSize(fileSize);
				nthFile.setLibraryclass(libraryclass);
				nthFile.setBarcode(barcode);
				if(restorePermitted)
					nthFile.setRestorePermitted(restorePermitted);
				nthFile.setOnline(online);
			}
		}

		return nthFile;
	}
	
	@ApiOperation(value = "Restores the list of files requested from copy 1 into the target volume location grouped under the output dir")
	@PostMapping("/file/restore")
	public ResponseEntity<org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails> restore(@RequestBody org.ishafoundation.dwaraapi.api.req.restore.UserRequest userRequest){
		org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails requestForResponse = null;
		
		try {

	    	Action action = Action.restore;
	    	
	    	String targetvolumeName = userRequest.getTargetvolumeName();
	    	Targetvolume targetvolume = targetvolumeDao.findByName(targetvolumeName);
	    	
	    	String requestedBy = getUserFromContext();
	    	User user = userDao.findByName(requestedBy);
	    	
			Request request = new Request();
			request.setAction(action);
	    	request.setCopyNumber(userRequest.getCopyNumber());
	    	request.setTargetvolume(targetvolume);//request.setTargetvolumeId(userRequest.getTargetvolumeId());
	    	request.setOutputFolder(userRequest.getOutputFolder());
			request.setRequestedAt(LocalDateTime.now());
			request.setUser(user);
	    	logger.debug("DB Request Creation");
	    	request = requestDao.save(request);
	    	logger.debug("DB Request Creation - Success " + request.getId());
	    	
	    	List<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest> subrequestList = new ArrayList<org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest>();	
	    	List<FileParams> fileParamsList = userRequest.getFileParams();
	    	for (Iterator<FileParams> iterator2 = fileParamsList.iterator(); iterator2.hasNext();) {
				FileParams nthFileParams = (FileParams) iterator2.next();
		    	
				int fileId = nthFileParams.getFileId();
				
		    	Subrequest subrequest = new Subrequest();
		    	subrequest.setRequest(request);
		    	subrequest.setFileId(fileId);
		    	subrequest.setPriority(nthFileParams.getPriority());
		    	subrequest.setStatus(Status.queued);

		    	logger.debug("DB Subrequest Creation");
		    	subrequest = subrequestDao.save(subrequest);
		    	logger.debug("DB Subrequest Creation - Success " + subrequest.getId());

		    	createRestoreJob(request, subrequest);
				
		    	org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest systemGeneratedSubRequestForResponse = objectMappingUtil.frameSubrequestObjectForResponse(subrequest);

		    	subrequestList.add(systemGeneratedSubRequestForResponse);
			}
	    	requestForResponse = objectMappingUtil.frameRequestObjectForResponse(request);
	    	requestForResponse.setSubrequestList(subrequestList);
		} catch (Throwable e) {
			String errorMsg = "Unable to trigger the restore job - " + e.getMessage();
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(requestForResponse);
	}
	
	private String getUserFromContext() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
    private void createRestoreJob(Request request, Subrequest subrequest) {
    	Job job = jobManager.createJobForRestore(request, subrequest);
    	
    	logger.debug("DB Job for restore Creation");   
    	jobDao.save(job);
    	logger.debug("DB Job for restore Creation - Success");
    }	

	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
////	@ApiOperation(value = "Get List of all Restore Requests filtered by its status or context")
////    @PostMapping("/getAllRestoreRequests")
////    public RestoreRequestListResp getAllRestoreRequests(@RequestParam String status){
////    	List<RestoreRequest> restoreRequestList = null;
////    	if(status.equals(MediaLibraryStatusFilter.PROCESSING.toString()))
////    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findProcessing();
////    	else if(status.equals(MediaLibraryStatusFilter.COMPLETED.toString())) 
////    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findCompleted();
////    	else if(status.equals(MediaLibraryStatusFilter.ALL.toString()))
////    		restoreRequestList = (List<RestoreRequest>) restoreRequestDao.findAll();
////    	else
////    		return null;
////    	
////    	RestoreRequestListResp restoreRequestListResp = new RestoreRequestListResp();
////    	restoreRequestListResp.setResponseCode(200);
////    	restoreRequestListResp.setResponseType("Success");
////    	restoreRequestListResp.setResponseMessage("Retrieved Restore Jobs List Successfully");
////    	
////    	restoreRequestListResp.setRestoreRequestList(restoreRequestList);
////    	restoreRequestListResp.setTotalNoOfRecords(restoreRequestList.size());
////    	restoreRequestListResp.setStartRecord(0);
////    	restoreRequestListResp.setEndRecord(restoreRequestList.size());
////        return restoreRequestListResp;
////    }
//
////	@ApiOperation(value = "Gets a specific Restore Requests details")
////    @GetMapping("/{restoreRequestId}/getRestoreRequestDetails")
////    public RestoreRequestDetailsResp getRestoreRequestDetails(@PathVariable("restoreRequestId") int restoreRequestId) {
////    	RestoreRequestDetailsResp restoreRequestDetailsResp = new RestoreRequestDetailsResp();
////    	RestoreRequest restoreRequest = restoreRequestDao.findById(restoreRequestId).get();
////    	List<RestoreJob> restoreJobList = restoreJobDao.findAllByRestoreRequestId(restoreRequestId);
////    	restoreRequestDetailsResp.setRestoreJobList(restoreJobList);
////    	restoreRequestDetailsResp.setRestoreRequest(restoreRequest);
////		return restoreRequestDetailsResp;
////    }
//	
////	@ApiOperation(value = "Cancels a specific restore request and all the folders/job requested in it")
////	@PostMapping("/{restoreRequestId}/cancelRestoreRequest") 
////	//public ResponseEntity<RestoreRequestDetailsResp> cancelRestoreRequest(@PathVariable("restoreRequestId") int restoreRequestId){
////	public RestoreRequestDetailsResp cancelRestoreRequest(@PathVariable("restoreRequestId") int toBeCancelledRestoreRequestId){
////		
////		// create a cancel request for auditing purpose
////		RestoreRequest cancelRestoreRequest = new RestoreRequest();
////		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_REQUEST call");
////		cancelRestoreRequest.setType(RestoreType.CANCEL.toString());
////		cancelRestoreRequest.setTypeId(RestoreType.CANCEL.getRestoreTypeId());
////		cancelRestoreRequest.setRestoreRequestIdRef(toBeCancelledRestoreRequestId);
////		cancelRestoreRequest.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
////		cancelRestoreRequest.setDate(Calendar.getInstance().getTimeInMillis());
////		cancelRestoreRequest.setStatus(Status.IN_PROGRESS.toString());
////		cancelRestoreRequest.setStatusId(Status.IN_PROGRESS.getStatusId());
////		cancelRestoreRequest = restoreRequestDao.save(cancelRestoreRequest);
////		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_REQUEST call - " + cancelRestoreRequest.getRestoreRequestId());		
////		
////    	RestoreRequestDetailsResp restoreRequestDetailsResp = new RestoreRequestDetailsResp();
////    	
////    	List<RestoreJob> restoreJobLatestList = new ArrayList<RestoreJob>();
////		List<RestoreJob> restoreJobList = restoreJobDao.findAllByRestoreRequestId(toBeCancelledRestoreRequestId);
////		boolean isCancelled = false;
////		boolean isAborted = false;
////
////		for (Iterator<RestoreJob> iterator = restoreJobList.iterator(); iterator.hasNext();) {
////			RestoreJob nthRestoreJob = (RestoreJob) iterator.next();
////			
////			nthRestoreJob = cancelRestoreJob_Internal(nthRestoreJob.getRestoreJobId());
////			String nthRestoreJobStatus = nthRestoreJob.getStatus();
////			if (nthRestoreJobStatus.equals(Status.ABORTED.toString())) {
////				isCancelled = true;
////			}else if (nthRestoreJobStatus.equals(Status.ABORTED.toString())) {
////				isAborted = true;
////			}
////			
////			restoreJobLatestList.add(nthRestoreJob);
////		}
////		restoreRequestDetailsResp.setRestoreJobList(restoreJobLatestList);
////		
////		// set the status to cancelled/aborted for the original request 
////		Status originalRestoreRequestNewStatus = null;
////		if (isAborted) {
////			originalRestoreRequestNewStatus = Status.ABORTED;
////		}else if(isCancelled) {
////			originalRestoreRequestNewStatus = Status.CANCELLED;
////		}
////		
////		RestoreRequest originalRestoreRequest = restoreRequestDao.findById(toBeCancelledRestoreRequestId).get();
////		originalRestoreRequest.setStatus(originalRestoreRequestNewStatus.toString());
////		originalRestoreRequest.setStatusId(originalRestoreRequestNewStatus.getStatusId());
////		logger.debug("DB RestoreRequest - Status Updation");
////		restoreRequestDao.save(originalRestoreRequest);
////		logger.debug("DB RestoreRequest - Status Updation - " + toBeCancelledRestoreRequestId);
////
////		restoreRequestDetailsResp.setRestoreRequest(originalRestoreRequest);
////
////		// set the status to complete for the cancellation request
////		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_REQUEST call");
////		cancelRestoreRequest.setStatus(Status.COMPLETED.toString());
////		cancelRestoreRequest.setStatusId(Status.COMPLETED.getStatusId());
////		cancelRestoreRequest = restoreRequestDao.save(cancelRestoreRequest);
////		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_REQUEST call - " + cancelRestoreRequest.getRestoreRequestId());	
////		//return ResponseEntity.ok(restoreRequestDetailsResp);
////		return restoreRequestDetailsResp;
////	}
//	
////	@ApiOperation(value = "Get Details of a specific restore Job")
////	@GetMapping("/{restoreJobId}/getRestoreJobDetails")
////	public RestoreJobDetailsResp getRestoreJobDetails(@PathVariable("restoreJobId") int restoreJobId) {
////		RestoreJobDetailsResp restoreJobDetails = new RestoreJobDetailsResp();
////		
////		RestoreJob restoreJob = restoreJobDao.findById(restoreJobId).get();
////		restoreJobDetails.setRestoreJob(restoreJob);
////
////		RestoreTapeJob restoreTapeJob = restoreTapeJobDao.findByRestoreJobId(restoreJobId);
////		restoreJobDetails.setRestoreTapeJob(restoreTapeJob);
////		return restoreJobDetails;
////	}
////	
////
////	@ApiOperation(value = "Cancels a restore Job")
////	@PostMapping("/{restoreJobId}/cancelRestoreJob") 
////	public RestoreJob cancelRestoreJob(@PathVariable("restoreJobId") int restoreJobId){
////		// create a cancel job request for auditing purpose
////		RestoreRequest cancelRestoreJob_Request = new RestoreRequest();
////		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_JOB call");
////		cancelRestoreJob_Request.setType(RestoreType.CANCEL.toString());
////		cancelRestoreJob_Request.setTypeId(RestoreType.CANCEL.getRestoreTypeId());
////		cancelRestoreJob_Request.setRestoreJobIdRef(restoreJobId);
////		cancelRestoreJob_Request.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
////		cancelRestoreJob_Request.setDate(Calendar.getInstance().getTimeInMillis());
////		cancelRestoreJob_Request.setStatus(Status.IN_PROGRESS.toString());
////		cancelRestoreJob_Request.setStatusId(Status.IN_PROGRESS.getStatusId());
////		cancelRestoreJob_Request = restoreRequestDao.save(cancelRestoreJob_Request);
////		logger.debug("DB RestoreRequest Creation for CANCEL_RESTORE_JOB call - " + cancelRestoreJob_Request.getRestoreRequestId());		
////		
////		RestoreJob restoreJob = cancelRestoreJob_Internal(restoreJobId);
////		
////		// set the status to complete for the cancellation request
////		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_JOB call");
////		cancelRestoreJob_Request.setStatus(Status.COMPLETED.toString());
////		cancelRestoreJob_Request.setStatusId(Status.COMPLETED.getStatusId());
////		cancelRestoreJob_Request = restoreRequestDao.save(cancelRestoreJob_Request);
////		logger.debug("DB RestoreRequest - Status Updation for CANCEL_RESTORE_JOB call - " + cancelRestoreJob_Request.getRestoreRequestId());
////		
////		// TODO - What should be the status of the original restore request??
////		return restoreJob;
////	}
////	
////	private RestoreJob cancelRestoreJob_Internal(int restoreJobId){
////		RestoreJob restoreJob = restoreJobDao.findById(restoreJobId).get();
////		String currentStatus = restoreJob.getStatus();
////		Status newStatus = null;
////		// if its queued status
////		if(currentStatus.equals(Status.QUEUED.toString())) { 
////			newStatus = Status.CANCELLED;
////		}
////		else if(currentStatus.equals(Status.IN_PROGRESS.toString())) {
////			newStatus = nodeumStorageManager.cancel(restoreJob, 1);
////		}
////		
////		restoreJob.setStatus(newStatus.toString());
////		restoreJob.setStatusId(newStatus.getStatusId());		
////		restoreJob = restoreJobDao.save(restoreJob);
////
////		return restoreJob;
////	}
////	
////    @PostMapping("/diagnosticsAndRepairRestoreRequests")
////    public void diagnosticsAndRepairRestoreRequests(){
////    	List<RestoreRequest> rrl = restoreRequestDao.findByStatusId(Status.IN_PROGRESS.getStatusId());
////    	
////    }
//	
}