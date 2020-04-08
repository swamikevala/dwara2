package org.ishafoundation.dwaraapi.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.constants.Requesttype;
import org.ishafoundation.dwaraapi.db.dao.master.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TargetvolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FileTapeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.FileDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.LibraryDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.SubrequestDao;
import org.ishafoundation.dwaraapi.db.dao.view.V_RestoreFileDao;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.view.V_RestoreFile;
import org.ishafoundation.dwaraapi.job.JobManager;
import org.ishafoundation.dwaraapi.tape.library.MtxStatus;
import org.ishafoundation.dwaraapi.tape.library.TapeLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class RestoreContoller {
	
	Logger logger = LoggerFactory.getLogger(RestoreContoller.class);

	@Autowired
	private FileDao fileDao;

	@Autowired
	private LibraryDao libraryDao;
	
	@Autowired
	private V_RestoreFileDao v_RestoreFileDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TapeDao tapeDao;
	
	@Autowired
	private FileTapeDao fileTapeDao;
	
	@Autowired
	private RequestDao requestDao;	
	
	@Autowired
	private SubrequestDao subrequestDao;	
	
	@Autowired
	private RequesttypeDao requesttypeDao;	
	
	@Autowired
	private JobDao jobDao;	
	
	@Autowired
	private TargetvolumeDao targetvolumeDao;	
	
	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private TapeLibraryManager tapeLibraryManager;	
	
	@ApiOperation(value = "Lists all the target volumes", response = List.class)
	@GetMapping("/targetvolume")
	public ResponseEntity<List<Targetvolume>> getAllTargetVolumes() {
		List<Targetvolume> targetvolumeList = (List<Targetvolume>) targetvolumeDao.findAll();
		if (targetvolumeList.size() > 0) {
			return ResponseEntity.ok(targetvolumeList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@ApiOperation(value = "Gets the details of all the files requested in fileIdsListAsCSV")
	@GetMapping("/file")
	public List<org.ishafoundation.dwaraapi.api.resp.restore.File> getFiles(@RequestParam String ids, @RequestParam(required=false, defaultValue="1") int copyNumber, @RequestParam(defaultValue="false") boolean showDeleted) {
		List<org.ishafoundation.dwaraapi.api.resp.restore.File> fileListForResponse = new ArrayList<org.ishafoundation.dwaraapi.api.resp.restore.File>();
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
	public org.ishafoundation.dwaraapi.api.resp.restore.File getFile(@PathVariable int fileId, @RequestParam(required=false, defaultValue="1") int copyNumber, @RequestParam(defaultValue="false") boolean showDeleted) {

		
		MtxStatus mtxStatus = tapeLibraryManager.getMtxStatus();
		List<String> loadedTapeList = mtxStatus.getAllLoadedTapesInTheLibrary();

		List<Requesttype> requesttypeList = new ArrayList<Requesttype>();
		requesttypeList.add(Requesttype.list);
		requesttypeList.add(Requesttype.restore);
		
		String requestedBy = getUserFromContext();
		int userId = userDao.findByName(requestedBy).getId();
		
		List<V_RestoreFile> v_RestoreFileList = v_RestoreFileDao.findAllByTapesetCopyNumberAndFileIdAndRequesttypeInAndUserId(copyNumber, fileId, requesttypeList, userId);
		

		int id = 0;
		String filePathname = null;
		double fileSize = 0;
		int libraryclassId = 0;
		String barcode = null;
		boolean isDeleted = false;
		boolean listPermitted = false;
		boolean restorePermitted = false;
		boolean targetVolumePermitted = false;
		boolean online = false;
		
		for (V_RestoreFile v_RestoreFile : v_RestoreFileList) {
			barcode = StringUtils.isBlank(barcode) ? v_RestoreFile.getTapeBarcode() : barcode;
			id = v_RestoreFile.getFileId();
			filePathname = StringUtils.isBlank(filePathname) ? v_RestoreFile.getFilePathname() : filePathname;
			fileSize = v_RestoreFile.getFileSize();
			libraryclassId = v_RestoreFile.getLibraryLibraryclassId();
			isDeleted = v_RestoreFile.isFileTapeDeleted();
			if(v_RestoreFile.getRequesttype() == Requesttype.list)
				listPermitted = true;
			if(v_RestoreFile.getRequesttype() == Requesttype.restore)
				restorePermitted = true;
			// TODO - how to arrive at targetVolumePermitted
			if(loadedTapeList.contains(barcode))
				online = true;				
		}
		org.ishafoundation.dwaraapi.api.resp.restore.File nthFile = null;
		if(!isDeleted || showDeleted) {
			nthFile = new org.ishafoundation.dwaraapi.api.resp.restore.File();
			nthFile.setListPermitted(listPermitted);
			if(listPermitted) {
				nthFile.setId(id);
				nthFile.setName(filePathname);
				nthFile.setSize(fileSize);
				nthFile.setLibraryclassId(libraryclassId);
				nthFile.setBarcode(barcode);
				if(restorePermitted)
					nthFile.setRestorePermitted(restorePermitted);
				if(restorePermitted && targetVolumePermitted)
					nthFile.setTargetVolumePermitted(targetVolumePermitted);
				nthFile.setOnline(online);
			}
		}

		return nthFile;
	}
	
//	@ApiOperation(value = "Restores the list of files requested from copy 1 into the target volume location grouped under the output dir")
//	@PostMapping("/restore")
//	public org.ishafoundation.dwaraapi.api.resp.restore.ResponseHeaderWrappedRequest restore(@RequestBody org.ishafoundation.dwaraapi.api.req.restore.UserRequest userRequest){
//    	org.ishafoundation.dwaraapi.api.resp.restore.ResponseHeaderWrappedRequest response = new org.ishafoundation.dwaraapi.api.resp.restore.ResponseHeaderWrappedRequest();
//		try {
//	    	Requesttype requesttype = requesttypeDao.findByName("RESTORE");
//	    	int requesttypeId = requesttype.getRequesttypeId();
//	    	
//	    	long requestedAt = System.currentTimeMillis();
//	    	String requestedBy = getUserFromContext();
//	    	
//			Request request = new Request();
//			request.setRequesttypeId(requesttypeId);
//	    	request.setCopyNumber(userRequest.getCopyNumber());
//	    	request.setTargetvolumeId(userRequest.getTargetvolumeId());
//	    	request.setOutputFolder(userRequest.getOutputFolder());
//			request.setRequestedAt(requestedAt);
//	    	request.setRequestedBy(requestedBy);
//	    	logger.debug("DB Request Creation");
//	    	request = requestDao.save(request);
//	    	int requestId = request.getRequestId();
//	    	logger.debug("DB Request Creation - Success " + requestId);
//
//	    	List<ResponseHeaderWrappedSubrequest> responseHeaderWrappedSubrequestList = new ArrayList<ResponseHeaderWrappedSubrequest>();
//	    	
//	    	List<FileParams> fileParamsList = userRequest.getFileParams();
//	    	for (Iterator<FileParams> iterator2 = fileParamsList.iterator(); iterator2.hasNext();) {
//				FileParams nthFileParams = (FileParams) iterator2.next();
//				
//				int fileId = nthFileParams.getFileId();
//				
//		    	int statusId = Status.QUEUED.getStatusId();
//
//		    	Subrequest subrequest = new Subrequest();
//		    	subrequest.setRequestId(requestId);
//		    	subrequest.setFileId(fileId);
//		    	subrequest.setOptimizeTapeAccess(nthFileParams.isOptimizeTapeAccess());
//		    	subrequest.setPriority(nthFileParams.getPriority());
//		    	subrequest.setStatusId(statusId);
//
//		    	logger.debug("DB Subrequest Creation");
//		    	subrequest = subrequestDao.save(subrequest);
//		    	logger.debug("DB Subrequest Creation - Success " + subrequest.getRequestId());
//
//				createJobTableRows(subrequest, requesttypeId);
//				
//				
//		    	org.ishafoundation.dwaraapi.api.resp.restore.Subrequest systemGeneratedSubRequestForResponse = new org.ishafoundation.dwaraapi.api.resp.restore.Subrequest();
//		    	systemGeneratedSubRequestForResponse.setFileId(fileId);
//		    	systemGeneratedSubRequestForResponse.setSubrequestId(subrequest.getSubrequestId());
//		    	systemGeneratedSubRequestForResponse.setOptimizeTapeAccess(subrequest.isOptimizeTapeAccess());
//		    	systemGeneratedSubRequestForResponse.setPriority(subrequest.getPriority());
//		    	systemGeneratedSubRequestForResponse.setRequestId(requestId);
//		    	systemGeneratedSubRequestForResponse.setStatusId(Status.QUEUED.getStatusId());
//		    	
//		    	org.ishafoundation.dwaraapi.api.resp.restore.ResponseHeaderWrappedSubrequest responseHeaderWrappedSubrequest = new org.ishafoundation.dwaraapi.api.resp.restore.ResponseHeaderWrappedSubrequest();
//		    	
//		    	responseHeaderWrappedSubrequest.setResponseCode(200);
//		    	responseHeaderWrappedSubrequest.setResponseMessage("Resp message");
//		    	responseHeaderWrappedSubrequest.setResponseType("Resp type");
//		    	responseHeaderWrappedSubrequest.setSubrequest(systemGeneratedSubRequestForResponse);
//				
//				
//		    	responseHeaderWrappedSubrequestList.add(responseHeaderWrappedSubrequest);
//			}
//	    	
//	    	org.ishafoundation.dwaraapi.api.resp.restore.RequestWithWrappedSubrequest requestForResponse = new org.ishafoundation.dwaraapi.api.resp.restore.RequestWithWrappedSubrequest();
//	    	requestForResponse.setCopyNumber(userRequest.getCopyNumber());
//	    	requestForResponse.setOutputFolder(userRequest.getOutputFolder());
//	    	requestForResponse.setRequestedAt(requestedAt);
//	    	requestForResponse.setRequestedBy(requestedBy);
//	    	requestForResponse.setRequestId(requestId);
//	    	requestForResponse.setRequesttypeId(requesttypeId);
//	    	requestForResponse.setResponseHeaderWrappedSubrequestList(responseHeaderWrappedSubrequestList);
//	    	requestForResponse.setTargetvolumeId(userRequest.getTargetvolumeId());
//	    	
//	    	response.setRequest(requestForResponse);
//	    	
//	    	response.setResponseCode(200);
//			response.setResponseType("Success");
//			response.setResponseMessage("UserRequest(s) Submitted Successfully");
//					
//
//		} catch (Throwable e) {
//			String errorMsg = "Unable to trigger the restore job - " + e.getMessage();
//			logger.error(errorMsg, e);
//			response.setResponseCode(111);
//			response.setResponseType("Failed");
//			response.setResponseMessage(errorMsg);
//		}
//		return response;		
//	}
//	
	private String getUserFromContext() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
//	
//    private void createJobTableRows(Subrequest subrequest, int requesttypeId) {
//    	List<Job> jobList = jobManager.createJobs(requesttypeId, 0, subrequest, null);
//    	
//    	logger.debug("DB Job rows Creation");   
//    	jobDao.saveAll(jobList);
//    	logger.debug("DB Job rows Creation - Success");
//    }	
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
