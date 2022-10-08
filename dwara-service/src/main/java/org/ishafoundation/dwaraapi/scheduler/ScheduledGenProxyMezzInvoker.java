package org.ishafoundation.dwaraapi.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.api.req.GenerateMezzanineProxiesRequest;
import org.ishafoundation.dwaraapi.api.resp.request.RequestResponse;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
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
		tapeList.add("R20036L7");
		tapeList.add("R20038L7");
		tapeList.add("R20039L7");
		tapeList.add("R20040L7");
		tapeList.add("R20041L7");
		tapeList.add("R20042L7");
		tapeList.add("R20043L7");
		tapeList.add("R20044L7");
		tapeList.add("R20045L7");
		tapeList.add("R20046L7");
		tapeList.add("R20047L7");
		tapeList.add("R20048L7");
		tapeList.add("R20049L7");
		tapeList.add("R20050L7");
		tapeList.add("R20051L7");
		tapeList.add("R20052L7");
		tapeList.add("R20053L7");
		tapeList.add("R20054L7");
		tapeList.add("R20055L7");
		tapeList.add("R20056L7");
		tapeList.add("R20057L7");
//		tapeList.add("R20058L7");
//		tapeList.add("R20059L7");
//		tapeList.add("R20060L7");
//		tapeList.add("R20061L7");
//		tapeList.add("R20062L7");
//		tapeList.add("R20063L7");
//		tapeList.add("R20064L7");
//		tapeList.add("R20065L7");
//		tapeList.add("R20066L7");
//		tapeList.add("R20067L7");
//		tapeList.add("R20068L7");
//		tapeList.add("R20069L7");
//		tapeList.add("R20070L7");
//		tapeList.add("R20071L7");
//		tapeList.add("R20072L7");
//		tapeList.add("R20073L7");
//		tapeList.add("R20075L7");
//		tapeList.add("X20009L7");
//		tapeList.add("X20010L7");
//		tapeList.add("X20011L7");
		
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
			User user = userDao.findById(1).get();
			int cnt = 1;
			int currentCount = inProgressGeneratedMezzProxiesUserRequestList.size();
			int toBeDoneCount = 2 - currentCount;
			for (String nthTape : tapeList) {
				if(cnt > toBeDoneCount)
					continue;
				
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
				cnt = cnt + 1;
			}
			
		}
	}

}