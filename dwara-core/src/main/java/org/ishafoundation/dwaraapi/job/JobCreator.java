package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionelementDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionelementMapDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobMapDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Actionelement;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionelementMap;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.JobMap;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
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
	private JobMapDao jobMapDao;
	
	@Autowired
	private ActionelementDao actionelementDao;

	@Autowired
	private ActionelementMapDao actionelementMapDao;

	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	
	// only if action is async create job should be called...
	public List<Job> createJobs(Request request, Artifact artifact) throws Exception{
		Action requestedBusinessAction = request.getActionId();
		List<Integer>  actionelementsToBeSkipped = request.getDetails().getSkipActionelements();
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);
		
		if(action == null)
			throw new Exception("Action for " + requestedBusinessAction.name() + " not configured in DB properly. Please set it first");
		
		List<Job> jobList = new ArrayList<Job>();
		
		if(Actiontype.complex == action.getType()) {
			Map<Integer, Job> actionelementId_Job_Map = new HashMap<>();// used for getting referenced jobs
			Map<String, Job> actionelementId_CopyNumber_Job_Map = new HashMap<>();
			List<Actionelement> actionelementList = actionelementDao.findAllByComplexActionIdAndArtifactclassIdAndActiveTrueOrderByDisplayOrderAsc(requestedBusinessAction, artifact.getArtifactclass().getId());
			for (Iterator<Actionelement> iterator = actionelementList.iterator(); iterator.hasNext();) {
				Actionelement actionelement = (Actionelement) iterator.next();
				
				if(!actionelementsToBeSkipped.contains(actionelement.getId())){ // if the current actionelement needs to be skipped...
					Action storagetaskAction = actionelement.getStoragetaskActionId();
					if(storagetaskAction == Action.write || storagetaskAction == Action.verify) {
						Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(actionelement.getArtifactclassId());
						Artifactclass artifactclassGroup = artifactclass.getGroupRef();
						
						List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclass(artifactclassGroup);
						for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
							if(artifactclassVolume.isActive()) {
								Volume volume = artifactclassVolume.getVolume();
								Job job = new Job();
								job.setStoragetaskActionId(storagetaskAction);
								
								List<ActionelementMap> preRequesiteActionelementMaps = actionelementMapDao.findAllByIdActionelementId(actionelement.getId());
								job.setInputArtifactId(artifact.getId());
								job.setActionelement(actionelement);
								job.setRequest(request);				
								job.setCreatedAt(LocalDateTime.now());
								job.setStatus(Status.queued);
								job.setVolume(volume);
								job = saveJob(job);
								
								
								// saving all the pre requisite jobs needed for this job...
								if(preRequesiteActionelementMaps.size() > 0) {
									for (ActionelementMap nthPreRequesiteActionelementMap : preRequesiteActionelementMaps) {
										int nthPreRequesiteActionelementId = nthPreRequesiteActionelementMap.getId().getActionelementRefId();
										Actionelement nthPreRequesiteActionelement = actionelementDao.findById(nthPreRequesiteActionelementId).get();
										Job nthPreRequesiteJob = null;
										if(nthPreRequesiteActionelement.getStoragetaskActionId() == Action.write)
											nthPreRequesiteJob = actionelementId_CopyNumber_Job_Map.get(nthPreRequesiteActionelementMap.getId().getActionelementRefId() + "_" + volume.getCopyNumber());
										else
											nthPreRequesiteJob = actionelementId_Job_Map.get(nthPreRequesiteActionelementMap.getId().getActionelementRefId());
										JobMap jobMap = new JobMap(job, nthPreRequesiteJob);
										jobMapDao.save(jobMap);
									}
								}
								jobList.add(job);
								
								actionelementId_CopyNumber_Job_Map.put(actionelement.getId() + "_" + volume.getCopyNumber(), job);
								actionelementId_Job_Map.put(actionelement.getId(), job);		
							}
						}
					}
					else {
						String processingtaskId = actionelement.getProcessingtaskId();
						
						Job job = new Job();
						if(storagetaskAction != null)
							job.setStoragetaskActionId(storagetaskAction);
						if(processingtaskId != null)
							job.setProcessingtaskId(processingtaskId);
						
						List<ActionelementMap> preRequesiteActionelementMaps = actionelementMapDao.findAllByIdActionelementId(actionelement.getId());
						if(storagetaskAction != null || (processingtaskId != null && preRequesiteActionelementMaps.size() == 0)) {
							// If a processing task contains prerequisite task that means its a derived one, for which the input library id needs to set by the prerequisite/parent task's job at the time of its processing and not at the time of job creation...
							// for eg., Mezz copy wont have a input library id upfront, which will be generated by its prerequisite/parent Mezz Transcoding job...
							job.setInputArtifactId(artifact.getId());
						}
						job.setActionelement(actionelement);
						job.setRequest(request);				
						job.setCreatedAt(LocalDateTime.now());
						job.setStatus(Status.queued);
		
						job = saveJob(job);
						
						
						// saving all the pre requisite jobs needed for this job...
						if(preRequesiteActionelementMaps != null) {
							for (ActionelementMap nthPreRequesiteActionelementMap : preRequesiteActionelementMaps) {
								Job nthPreRequesiteJob = actionelementId_Job_Map.get(nthPreRequesiteActionelementMap.getId().getActionelementRefId());
								JobMap jobMap = new JobMap(job, nthPreRequesiteJob);
								jobMapDao.save(jobMap);
							}
						}
						jobList.add(job);
						actionelementId_Job_Map.put(actionelement.getId(), job);
					}
				}
			}
		}
		else if(Actiontype.storage_task == action.getType()){
			String actionName = requestedBusinessAction.name();
			logger.debug("Calling storage task impl " + actionName);
			AbstractStoragetaskAction actionImpl = storagetaskActionMap.get(actionName);
			
			jobList.addAll(actionImpl.createJobsForStoragetaskAction(request, requestedBusinessAction));
		}
		return jobList;
	}

	private Job saveJob(Job job) {
		//logger.debug("DB Job row Creation");   
		job = jobDao.save(job);
		logger.info("Job - " + job.getId());
		return job;
	}
}
	
