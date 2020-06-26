package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Actionelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.utils.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(JobCreator.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ActionelementDao actionelementDao;
		
	@Autowired
	private Map<String, AbstractStoragetaskAction> actionMap;

	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;

	
	// only if action is async create job should be called...
	public List<Job> createJobs(Request request, Artifact artifact) throws Exception{
		Action requestedBusinessAction = request.getAction();

		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = (org.ishafoundation.dwaraapi.db.model.master.reference.Action) dBMasterTablesCacheManager.getRecord(CacheableTablesList.action.name(), requestedBusinessAction.name());
		
		if(action == null)
			throw new Exception("Action for " + requestedBusinessAction.name() + " not configured in DB properly. Please set it first");
		
		Map<Integer, Job> actionelementId_Job_Map = new HashMap<>();
		
		List<Job> jobList = new ArrayList<Job>();
		
		if(Actiontype.complex == action.getType()) {
			
			List<Actionelement> actionelementList = actionelementDao.findAllByComplexActionAndArtifactclassIdOrderByDisplayOrderAsc(requestedBusinessAction, artifact.getArtifactclass().getId());
			for (Iterator<Actionelement> iterator = actionelementList.iterator(); iterator.hasNext();) {
				Actionelement actionelement = (Actionelement) iterator.next();
				
				int storagetaskActionId = actionelement.getStoragetaskActionId();
				int processingtaskId = actionelement.getProcessingtaskId();
			
				// TODO - if(skip_storagetasks)
				
				Job job = new Job();
				if(storagetaskActionId > 0)
					job.setStoragetaskActionId(storagetaskActionId);
				if(processingtaskId > 0)
					job.setProcessingtaskId(processingtaskId);
				
				if(actionelement.getActionelementRefId() != null) {
					job.setJobRef(actionelementId_Job_Map.get(actionelement.getActionelementRefId()));
					if(storagetaskActionId > 0)
						job.setInputArtifactId(artifact.getId());
				}
				else  {
					// If a task contains prerequisite task that means its a derived one, for which the input library id needs to set by the prerequisite/parent task's job at the time of its processing and not at the time of job creation...
					// for eg., Mezz copy wont have a input library id upfront, which will be generated by its prerequisite/parent Mezz Transcoding job...
					job.setInputArtifactId(artifact.getId());
				}
				job.setActionelement(actionelement);
				job.setRequest(request);				
				job.setCreatedAt(LocalDateTime.now());
				job.setStatus(Status.queued);
//				
//				JobDetails jobDetails = new JobDetails();
//				jobDetails.setDevice_id(device_id);
//				jobDetails.setVolume_id(volume_id);
//				job.setDetails(jobDetails);
				job = saveJob(job);
				jobList.add(job);
				actionelementId_Job_Map.put(actionelement.getId(), job);
			}
		}
		else if(Actiontype.storage_task == action.getType()){
			String actionName = action.getName();
			logger.debug("\t\tcalling storage task impl " + actionName);
			AbstractStoragetaskAction actionImpl = actionMap.get(actionName);
			
			try {
				jobList.addAll(actionImpl.createJobsForStoragetaskAction(request, Action.valueOf(actionName)));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jobList;
	}

	private Job saveJob(Job job) {
		//logger.debug("DB Job row Creation");   
		job = jobDao.save(job);
		logger.info("DB Job " + job.getId() + " - Created");
		return job;
	}
}
	
