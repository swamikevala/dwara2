package org.ishafoundation.dwaraapi.job;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassFlowDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassFlow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobCreator {

	private static final Logger logger = LoggerFactory.getLogger(JobCreator.class);

	@Autowired
	private JobDao jobDao;

	@Autowired
	private ActionArtifactclassFlowDao actionArtifactclassFlowDao;

	@Autowired
	private FlowelementDao flowelementDao;

	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;

	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private ApplicationContext applicationContext;
	
	// if only request.action is complex this gets called.
	public List<Job> createDependentJobs(Job job){
		Flowelement fe = job.getFlowelement();
		Flow flow = fe.getFlow();
		Request request = job.getRequest();

		List<Job> jobsCreated = new ArrayList<Job>();
		iterateFlow(job, request, flow, fe, jobsCreated);
		return jobsCreated;
	}

	private void iterateFlow(Job job, Request request, Flow flow, Flowelement fe, List<Job> jobList) {
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndActiveTrueOrderByDisplayOrderAsc(flow.getId());
		List<Flowelement> dependentFlowelementList = getDependentFlowelements(fe, flowelementList);
		
		int dependentJobInputArtifactId = job.getOutputArtifactId() != null ? job.getOutputArtifactId() : job.getInputArtifactId();
		
		Artifact dependentJobInputArtifact = domainUtil.getDomainSpecificArtifact(dependentJobInputArtifactId);
		
		for (Flowelement nthFlowelement : dependentFlowelementList) {
			Flow flowRef = nthFlowelement.getFlowRef();
			
			if(flowRef != null) { // If the flowelement has a flowref, that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input
				logger.trace("References a flow again " + flowRef.getId());
				iterateFlow(job, request, flowRef, nthFlowelement, jobList);
			} else {
				jobList.addAll(createJobs(nthFlowelement, job, request, dependentJobInputArtifact.getArtifactclass().getId(), dependentJobInputArtifact));
			}
		}
	}

	/**
	 * scenario 1 - verify both checksum and write jobs
	 * srcJob == null
	 * 
	 * srcJob == write
	 * 
	 * srcJob == checksum
	 * 
	 */

	/*
	 * flow element passed here should be either a storage or a processing task one.
	 * 1 flow element = As many storage jobs configured in artifactclassVolume (hence the method name createJobs - Note the Jobs plural)
	 * 1 flow element = 1 job for all processing tasks 
	 */
	private List<Job> createJobs(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {
		List<Job> jobsCreated = new ArrayList<Job>();
		
		Action storagetaskAction = nthFlowelement.getStoragetaskActionId();
		String processingtaskId = nthFlowelement.getProcessingtaskId();
		
		// if the sourceJob for which the dependants are to be created is a processing task and the flowelement is a storagetask we may have to create as many jobs as copies needed...
		// But we should verify if they are not already created and create here... - 
		// We might have race conditions with write completion thread creating one and just at the same time this one does too... Need to ensure that doesnt happen...
		// TODO Synchronize this...
		if(storagetaskAction != null) {
			if(sourceJob == null || (sourceJob != null && sourceJob.getProcessingtaskId() != null)) { // only srcjob = processing task is from scheduler which needs multiple job updates 
				logger.trace("Creating Job for " + storagetaskAction + ":" + nthFlowelement.getId());
				List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(artifactclassId);
				logger.trace("No. of copies " + artifactclassVolumeList.size());
				for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
//					if(artifactclassVolume.isActive()) {
						Volume grpVolume = artifactclassVolume.getVolume();

						List<Integer> dependentJobIds = new ArrayList<Integer>();
						if(sourceJob != null) {
							dependentJobIds.add(sourceJob.getId());
						}
						
						// validating if all its other dependencies other than sourceJob are complete[only on sourceJob's completion this is called...]
						boolean isJobGoodToBeCreated = true;
						List<Integer> preRequesiteFlowelements = nthFlowelement.getDependencies();
						if(preRequesiteFlowelements != null) {
							for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
								if(sourceJob != null && sourceJob.getFlowelement().getId() == nthPreRequesiteFlowelementId)
									continue;
								
								Job jobInQuestion = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifact.getId(), nthPreRequesiteFlowelementId, grpVolume.getId());
								
								if(jobInQuestion == null || jobInQuestion.getStatus() != Status.completed) {
									isJobGoodToBeCreated = false;
									break;
								}
								dependentJobIds.add(jobInQuestion.getId());
							}	
						}
						if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
							Job job = createJob(nthFlowelement, sourceJob, request, artifactclassId, artifact);
							if(dependentJobIds.size() > 0)
								job.setDependencies(dependentJobIds);
							
							job.setStoragetaskActionId(storagetaskAction);
							job.setGroupVolume(grpVolume); // we dont know the physical volume yet... How about provisioned volumes?
	
							job = saveJob(job);
							jobsCreated.add(job);
						}
//					}
				}
			}
			else { // sourceJob != null - called from a storage dependency job 
				List<Integer> dependentJobIds = new ArrayList<Integer>();
				if(sourceJob != null) {
					dependentJobIds.add(sourceJob.getId());
				}
				
				// validating if all its other dependencies other than sourceJob are complete[only on sourceJob's completion this is called...]
				boolean isJobGoodToBeCreated = true;
				List<Integer> preRequesiteFlowelements = nthFlowelement.getDependencies();
				if(preRequesiteFlowelements != null) {
					for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
						if(sourceJob != null && sourceJob.getFlowelement().getId() == nthPreRequesiteFlowelementId)
							continue;
						
						Flowelement prereqFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get();

						String gvId = null;
						if(prereqFlowelement.getStoragetaskActionId() != null)
							gvId = sourceJob.getGroupVolume().getId();
						
						Job jobInQuestion = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifact.getId(), nthPreRequesiteFlowelementId, gvId);
						dependentJobIds.add(jobInQuestion.getId());
						if(jobInQuestion.getStatus() != Status.completed) {
							isJobGoodToBeCreated = false;
						}
					}	
				}
				if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
					Job job = createJob(nthFlowelement, sourceJob, request, artifactclassId, artifact);
					job.setStoragetaskActionId(storagetaskAction);
					job.setGroupVolume(sourceJob.getGroupVolume()); // we already know the physical volume used by its parent job
					job.setVolume(sourceJob.getVolume());
					Collections.sort(dependentJobIds);
					job.setDependencies(dependentJobIds);
					job = saveJob(job);
					jobsCreated.add(job);
				}
			}
		}
		else {
			String inputArtifactName = artifact.getName();
			Artifactclass inputArtifactclass = artifact.getArtifactclass();
			String inputArtifactPath = inputArtifactclass.getPath() + File.separator + inputArtifactName;
			
			ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
			if(!processingJobManager.isJobToBeCreated(processingtaskId, inputArtifactPath, inputArtifactclass)) { // Dont create jobs for something we know would eventually fail...
				logger.trace("Job not to be created - " + nthFlowelement.getId());
				return jobsCreated;
			}
			logger.trace("Creating Job for " + processingtaskId + ":" + nthFlowelement.getId());
			
			Job job = createJob(nthFlowelement, sourceJob, request, artifactclassId, artifact);
			
			List<Integer> dependentJobIds = new ArrayList<Integer>();
			if(sourceJob != null) {
				dependentJobIds.add(sourceJob.getId());
				job.setDependencies(dependentJobIds);
			}
			
			job.setProcessingtaskId(processingtaskId);
			job = saveJob(job);
			jobsCreated.add(job);
		}
		return jobsCreated;
	}

	private Job createJob(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {

		Job job = new Job();
		job.setInputArtifactId(artifact.getId());
		job.setFlowelement(nthFlowelement);
		job.setRequest(request);				
		job.setCreatedAt(LocalDateTime.now());
		job.setStatus(Status.queued);

//		List<Integer> dependentJobIds = new ArrayList<Integer>();
//		List<Integer> preRequesiteFlowelements = nthFlowelement.getDependencies();
//		if(preRequesiteFlowelements != null && preRequesiteFlowelements.size() > 1) { // Do this only for multiple dependencies
//			for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
//				Flowelement prereqFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get();
//				Job nthPreRequesiteJob = null;
//				
//				String gvId = null;
//				if(prereqFlowelement.getStoragetaskActionId() != null)
//					gvId = job.getGroupVolume().getId();
//
//				nthPreRequesiteJob = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(job.getRequest().getId(), artifact.getId(), nthPreRequesiteFlowelementId, gvId);	
//				dependentJobIds.add(nthPreRequesiteJob.getId());
//			}
//		}
//		else if(sourceJob != null)
//			dependentJobIds.add(sourceJob.getId());
//		
//		job.setDependencies(dependentJobIds);
		return job;
	}
	
	private List<Flowelement> getDependentFlowelements(Flowelement fe, List<Flowelement> flowelementList) {
		List<Flowelement> dependentFlowelementList = new ArrayList<Flowelement>();
		for (Flowelement nthFlowelement : flowelementList) {
			if(nthFlowelement.getId() == fe.getId())
				continue;
			
			List<Integer> preReqs = nthFlowelement.getDependencies();
			if(fe.getFlowRef() != null) {
				if(preReqs == null)
					dependentFlowelementList.add(nthFlowelement);
			}
			else if(preReqs != null && preReqs.contains(fe.getId())) {
				dependentFlowelementList.add(nthFlowelement);
			}
		}
		return dependentFlowelementList;
	}
	
	// only if action is async create job should be called...
	public List<Job> createJobs(Request request, Artifact sourceArtifact){
		Action requestedBusinessAction = request.getActionId();
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);

		List<Job> jobList = new ArrayList<Job>();

		if(Actiontype.complex == action.getType()) {
			logger.trace("Complex action");
			String sourceArtifactclassId = sourceArtifact.getArtifactclass().getId();
			// get all the flows for the action on the artifactclass - Some could be global across artifactclasses and some specific to that artifactclass. so using "_all_" for global
			List<ActionArtifactclassFlow> actionArtifactclassFlowList = actionArtifactclassFlowDao.findAllByArtifactclassIdOrArtifactclassIdAndActionIdAndActiveTrue("_all_", sourceArtifactclassId, requestedBusinessAction.name()); //
			for (ActionArtifactclassFlow actionArtifactclassFlow : actionArtifactclassFlowList) {
				String nthFlowId = actionArtifactclassFlow.getFlow().getId();
				logger.trace("flow " + nthFlowId);
//				Map<String, Job> flowelementId_Job_Map = new HashMap<>();// used for getting referenced jobs
//				Map<String, Job> flowelementId_CopyNumber_Job_Map = new HashMap<>();
//
				jobList.addAll(iterateFlow(request, sourceArtifactclassId, sourceArtifact, nthFlowId));

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

	private List<Job> iterateFlow(Request request, String artifactclassId, Artifact artifact, String nthFlowId) {
		List<Job> jobsCreated = new ArrayList<Job>();
		
		logger.trace("Iterating flow " + nthFlowId);
		logger.trace("artifactclassId " + artifactclassId);
		logger.trace("artifactId " + artifact.getId());
		//  get all the flow elements for the flow
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndActiveTrueOrderByDisplayOrderAsc(nthFlowId);
		for (Flowelement nthFlowelement : flowelementList) {
			logger.trace("Flowelement " + nthFlowelement.getId());
			
			List<Integer> refFlowelementDepsList = nthFlowelement.getDependencies();
			if(refFlowelementDepsList == null) {
				jobsCreated.addAll(createJobs(nthFlowelement, null, request, artifactclassId, artifact));
			}
			// *** Dont create dependent jobs upfront. Create them as and when prerequisite jobs are complete
//			Flow flowRef = nthFlowelement.getFlowRef();
//			if(flowRef != null) { // If the flowelement has a flowref, that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input
//				logger.trace("References a flow again " + flowRef.getId());
//				List<Integer> refFlowelementDepsList = nthFlowelement.getDependencies();
//				if(refFlowelementDepsList == null) {
//					logger.warn("Are you sure you dont want any dependencies defined on - " + nthFlowelement.getId() + ". Whats the point of this flowelement with flowref then?");
//				}else {
//					logger.trace("Has dependencies " + refFlowelementDepsList);
//					// Now use one of the processing task that too generating an output
//					String outputArtifactclassSuffix = null;
//					for (Integer nthRefFlowelementDepId : refFlowelementDepsList) {
//						Flowelement prereqFlowelement = flowelementDao.findById(nthRefFlowelementDepId).get();
//						String processingtaskId = prereqFlowelement.getProcessingtaskId();  
//						if(processingtaskId == null) // Is the dependency a processing task?
//							continue;
//						logger.trace("A processing task " + processingtaskId);
//						Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
//						outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix(); // Does the dependent processing task generate an output?
//						if(outputArtifactclassSuffix != null)
//							break;
//					}
//					String outputArtifactclassId = request.getDetails().getArtifactclassId() + outputArtifactclassSuffix;
//					logger.trace("Output ArtifactclassId " + outputArtifactclassId);
//					iterateFlow(request, outputArtifactclassId, null, flowRef.getId(), nthFlowelement, jobList, flowelementId_Job_Map, flowelementId_CopyNumber_Job_Map);
//				}
//			} else {
//				createJob(request, artifactclassId, artifactId, flowRefFlowelement, nthFlowelement, jobList, flowelementId_Job_Map, flowelementId_CopyNumber_Job_Map);
//			}
		}
		return jobsCreated;
	}
	
//	private String getKeyPrefix(Flowelement flowRefFlowelement, String keyPrefix) {
//		String keyPrefixTmp = null;
//		Flowelement flowRefFlowelementParent = flowRefFlowelement.getFlowelementRef();
//		if(flowRefFlowelementParent != null) {
//			keyPrefixTmp = getKeyPrefix(flowRefFlowelementParent, keyPrefix + flowRefFlowelementParent.getId() + "_");
//		}
//		else
//			keyPrefixTmp = keyPrefix;
//		
//		logger.trace("keyPrefixTmp - " + keyPrefixTmp);
//		return keyPrefixTmp;
//	}
	
//	private void createJob(Request request, String artifactclassId, Artifact artifact, Flowelement flowRefFlowelement, Flowelement nthFlowelement, List<Job> jobList,
//			Map<String, Job> flowelementId_Job_Map, Map<String, Job> flowelementId_CopyNumber_Job_Map) {
//	
//		String putKeyPrefix="";
//		String getKeyPrefix = "";
//		List<Integer> preRequesiteFlowelements = null;
//		if(flowRefFlowelement == null) {
//			preRequesiteFlowelements = nthFlowelement.getDependencies();
//		}
//		else {
//			String keyPrefix = getKeyPrefix(flowRefFlowelement, "");
//			logger.trace("keyPrefix " + keyPrefix);
//			
//			putKeyPrefix = keyPrefix + flowRefFlowelement.getId() + "_";
//			getKeyPrefix = keyPrefix;
//			
//			if(nthFlowelement.getDependencies() == null)
//				preRequesiteFlowelements = flowRefFlowelement.getDependencies();
//			if(nthFlowelement.getDependencies() != null) {
//				preRequesiteFlowelements = nthFlowelement.getDependencies();
//				getKeyPrefix = getKeyPrefix + flowRefFlowelement.getId() + "_";
//			}
//			logger.trace("putKeyPrefix " + putKeyPrefix);
//			logger.trace("preRequesiteFlowelements " + preRequesiteFlowelements);
//			logger.trace("getKeyPrefix " + getKeyPrefix);
//		}
//		// TODO Skip individual flow elements - How? Need to be defined
//		Action storagetaskAction = nthFlowelement.getStoragetaskActionId();
//		if(storagetaskAction == Action.write || storagetaskAction == Action.verify) {
//			logger.trace("Creating Job for " + storagetaskAction + ":" + nthFlowelement.getId());
//			List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassId(artifactclassId);
//			logger.trace("No. of copies " + artifactclassVolumeList.size());
//			for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
//				if(artifactclassVolume.isActive()) {
//					Volume volume = artifactclassVolume.getVolume();
//					Job job = new Job();
//					job.setStoragetaskActionId(storagetaskAction);
//					job.setInputArtifactId(artifact.getId());
//					job.setFlowelement(nthFlowelement);
//					job.setRequest(request);				
//					job.setCreatedAt(LocalDateTime.now());
//					job.setStatus(Status.queued);
//					job.setGroupVolume(volume); // we dont know the physical volume yet... How about provisioned volumes?
//
//					// saving all the pre requisite jobs needed for this job...
//					List<Integer> dependentJobIds = new ArrayList<Integer>();
//					if(preRequesiteFlowelements != null) {
//						for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
//							Job nthPreRequesiteJob = null;
//							Flowelement nthPreRequesiteFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get(); // TODO Cache it
//							if(nthPreRequesiteFlowelement.getStoragetaskActionId() == Action.write) {
//								logger.trace("Getting write job from copy map with key " + getKeyPrefix + nthPreRequesiteFlowelement.getId() + "_" + volume.getCopy().getId());
//								nthPreRequesiteJob = flowelementId_CopyNumber_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelement.getId() + "_" + volume.getCopy().getId());
//							}
//							else {
//								logger.trace("Getting verify job with key " + getKeyPrefix + nthPreRequesiteFlowelement.getId());
//								nthPreRequesiteJob = flowelementId_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelement.getId());
//							}
//
//							dependentJobIds.add(nthPreRequesiteJob.getId());
//						}
//					}
//					if(dependentJobIds.size() > 0)
//						job.setDependencies(dependentJobIds);
//					job = saveJob(job);
//					jobList.add(job);
//					
//					logger.trace("Added job " + job.getId() + " to copy map with key " + putKeyPrefix + nthFlowelement.getId() + "_" + volume.getCopy().getId());
//					flowelementId_CopyNumber_Job_Map.put(putKeyPrefix + nthFlowelement.getId() + "_" + volume.getCopy().getId(), job);
//					logger.trace("Added job " + job.getId() + " to map with key " + putKeyPrefix + nthFlowelement.getId());
//					flowelementId_Job_Map.put(putKeyPrefix + nthFlowelement.getId(), job);		
//				}
//			}
//		}
//		else {
//			String processingtaskId = nthFlowelement.getProcessingtaskId();
//			
//			String inputArtifactName = artifact.getName();
//			Artifactclass inputArtifactclass = artifact.getArtifactclass();
//			String inputArtifactPath = inputArtifactclass.getPath() + File.separator + inputArtifactName;
//			
//			ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
//			if(!processingJobManager.isJobToBeCreated(processingtaskId, inputArtifactPath, inputArtifactclass)) // Dont create jobs for something we know would eventually fail...
//				return;
//			
//			logger.trace("Creating Job for " + processingtaskId + ":" + nthFlowelement.getId());
//			
//			Job job = new Job();
//			if(processingtaskId != null)
//				job.setProcessingtaskId(processingtaskId);
//			else
//				job.setStoragetaskActionId(storagetaskAction);
//
//			if(storagetaskAction != null || (processingtaskId != null && preRequesiteFlowelements == null)) {
//				// If a processing task contains prerequisite task that means its a derived one, for which the input library id needs to set by the prerequisite/parent task's job at the time of its processing and not at the time of job creation...
//				// for eg., Mezz copy wont have a input library id upfront, which will be generated by its prerequisite/parent Mezz Transcoding job...
//				job.setInputArtifactId(artifact.getId());
//			}
//			job.setFlowelement(nthFlowelement);
//			job.setRequest(request);				
//			job.setCreatedAt(LocalDateTime.now());
//			job.setStatus(Status.queued);
//			List<Integer> dependentJobIds = new ArrayList<Integer>();
//			if(preRequesiteFlowelements != null) {
//				for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
//					Job nthPreRequesiteJob = null;
//					//					Flowelement nthPreRequesiteFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get(); // TODO Cache it
//					//					nthPreRequesiteJob = flowelementId_Job_Map.get(nthPreRequesiteFlowelement.getId());
//					logger.trace("Getting job with key " + getKeyPrefix + nthPreRequesiteFlowelementId);
//					nthPreRequesiteJob = flowelementId_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelementId);
//					dependentJobIds.add(nthPreRequesiteJob.getId());
//				}
//				job.setDependencies(dependentJobIds);
//			}
//			
//			job = saveJob(job);
//			jobList.add(job);
//			logger.trace("Added job " + job.getId() + " to map with key " + putKeyPrefix + nthFlowelement.getId());
//			flowelementId_Job_Map.put(putKeyPrefix + nthFlowelement.getId(), job);
//		}
//	}
	
	private Job saveJob(Job job) {
		//logger.debug("DB Job row Creation");   
		job = jobDao.save(job);
		logger.info(DwaraConstants.JOB + job.getId());
		return job;
	}
}

