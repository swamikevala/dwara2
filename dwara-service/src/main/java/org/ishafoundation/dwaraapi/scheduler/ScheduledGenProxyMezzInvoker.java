package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.GenerateMezzanineProxiesRequest;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.service.VolumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ScheduledGenProxyMezzInvoker {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledGenProxyMezzInvoker.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private RequestDao requestDao;
	
	@Autowired
	private VolumeService volumeService;	

	@Value("${scheduler.genProxyMezzInvoker.enabled:true}")
	private boolean isEnabled;

	@Scheduled(fixedDelayString = "${scheduler.genProxyMezzInvoker.fixedDelay}")
	@PostMapping("/genProxyMezzInvoker")
	public ResponseEntity<String> genProxyMezzInvoker(){
		if(isEnabled) {
			logger.info("***** genProxyMezzInvoker *****");
			try {
				invokeRestoreAndGenerateMezzanineProxiesApi();
			}
			catch (Exception e) {
				logger.error("Unable to update status " + e.getMessage(), e);
			}
			return ResponseEntity.status(HttpStatus.OK).body("Done");
		}
		else
			return null; 
	}

	public void invokeRestoreAndGenerateMezzanineProxiesApi() {
		
		List<String> tapeList = new ArrayList<String>();
		tapeList.add("R20073L7");
		tapeList.add("R20074L7");
		tapeList.add("R20075L7");
		tapeList.add("R20076L7");
		tapeList.add("R20077L7");
		tapeList.add("R20078L7");
		tapeList.add("R20079L7");
		tapeList.add("R20080L7");
		
		// exclude cancelled list
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.completed);
		statusList.add(Status.completed_failures);
		statusList.add(Status.marked_completed);
		statusList.add(Status.failed);
		statusList.add(Status.marked_failed);
		statusList.add(Status.queued);
		statusList.add(Status.in_progress);
		statusList.add(Status.on_hold);
		
		List<Request> alreadyGeneratedMezzProxiesUserRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.generate_mezzanine_proxies, statusList, RequestType.user);
		for (Request nthUserRequest : alreadyGeneratedMezzProxiesUserRequestList) {			
			JsonNode jsonNode = nthUserRequest.getDetails().getBody();
			String nthVolumeId = jsonNode.get("volumeId").asText();
			logger.trace("Skipping " + nthVolumeId + " as its already done ");
			tapeList.remove(nthVolumeId);
		}
		
		logger.trace(tapeList.toString());
		List<Status> statusList2 = new ArrayList<Status>();
		statusList2.add(Status.queued);
		statusList2.add(Status.in_progress);
		
		List<Request> inProgressGeneratedMezzProxiesUserRequestList = requestDao.findAllByActionIdAndStatusInAndType(Action.generate_mezzanine_proxies, statusList2, RequestType.user);
		if(inProgressGeneratedMezzProxiesUserRequestList.size() < 2) {
			int cnt = 1;
			int currentCount = inProgressGeneratedMezzProxiesUserRequestList.size();
			int toBeDoneCount = 2 - currentCount;
			for (String nthTape : tapeList) {
				if(cnt > toBeDoneCount)
					continue;
				
				logger.info("Invoking GenerateMezzanineProxiesApi as there are not enough requests");
				invokeRestoreAndGenerateMezzanineProxiesApi(nthTape);
				cnt = cnt + 1;
			}			
		} else if(inProgressGeneratedMezzProxiesUserRequestList.size() == 2) {
			for (Request nthInProgressGenerateMezzProxyUserRequest : inProgressGeneratedMezzProxiesUserRequestList) {
				List<Job> queuedRestoreJobList = jobDao.findTop3ByStoragetaskActionIdAndRequestRequestRefIdAndStatusOrderByRequestId(Action.restore, nthInProgressGenerateMezzProxyUserRequest.getId(), Status.queued);
				
				if(queuedRestoreJobList.size() == 0) {
					logger.info("Invoking GenerateMezzanineProxiesApi as no queued restore job for " + nthInProgressGenerateMezzProxyUserRequest.getId());
					invokeRestoreAndGenerateMezzanineProxiesApi(tapeList.get(0));
					tapeList.remove(0);
				}
			}
		}
	}

	private void invokeRestoreAndGenerateMezzanineProxiesApi(String nthTape) {
		User user = userDao.findById(1).get();
		logger.trace("Taking up tape " + nthTape);
		GenerateMezzanineProxiesRequest generateMezzanineProxiesRequest = new GenerateMezzanineProxiesRequest();
		generateMezzanineProxiesRequest.setArtifactclassRegex("video-p.*");
		generateMezzanineProxiesRequest.setArtifactRegex(".*Conscious.*");
		try {
			RequestResponse generateMezzanineProxiesResponse = volumeService.restoreAndGenerateMezzanineProxies(nthTape, generateMezzanineProxiesRequest, user);
			logger.info("restoreAndGenerateMezzanineProxies - userReqId : " + generateMezzanineProxiesResponse.getId());					
		}catch (Exception e) {
			String errorMsg = "Unable to invoke restoreAndGenerateMezzanineProxies successfully - " + e.getMessage();
			logger.error(errorMsg, e);
		}
	}

}