package org.ishafoundation.dwaraapi.job;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassFlowDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassTaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassFlow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassTask;
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
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
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
	private ArtifactclassTaskDao artifactclassTaskDao;

	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private DomainUtil domainUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Restore restoreStorageTask;

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
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc(nthFlowId);
		for (Flowelement nthFlowelement : flowelementList) {
			logger.trace("Flowelement " + nthFlowelement.getId());
			
			List<Integer> refFlowelementDepsList = nthFlowelement.getDependencies();
			if(refFlowelementDepsList == null) {
				jobsCreated.addAll(createJobs(nthFlowelement, null, request, artifactclassId, artifact));
			}
		}
		return jobsCreated;
	}
	
	/**
	 * Naming convention - 
	 * Dependencies - For checksum-verify flowelement/job, checksum-gen and restore are dependencies/prerequisites
	 * Depende(a)nts - For a restore job, checksum-verify is a dependent.
	 * 
	 * Method called by the job that needs its dependent Jobs created
	 * 
	 * @param job - source job for which dependent jobs need to be created
	 * @return
	 */
	
	public List<Job> createDependentJobs(Job job){
		
		Flowelement flowelement = job.getFlowelement();
		Flow flow = flowelement.getFlow();
		Request request = job.getRequest();
		
		logger.trace("Creating dependent job(s) for sourceJob " + job.getId() + " with flowelement " + flowelement);
		List<Job> jobsCreated = new ArrayList<Job>();
		iterateDependentFlowelements(job, request, flow, flowelement, jobsCreated);
		logger.trace("Dependent job(s) created for " + job.getId() + " - " + jobsCreated);
		return jobsCreated;
	}

	private void iterateDependentFlowelements(Job job, Request request, Flow flow, Flowelement flowelement, List<Job> jobList) {
		List<Flowelement> dependentFlowelementList = getDependentFlowelements(flow, flowelement);
		logger.trace("Dependent Flowelements of flowelement " + flowelement.getId() + " - " + dependentFlowelementList);
		
		int dependentJobInputArtifactId = job.getOutputArtifactId() != null ? job.getOutputArtifactId() : job.getInputArtifactId();
		
		Artifact dependentJobInputArtifact = domainUtil.getDomainSpecificArtifact(dependentJobInputArtifactId);
		
		for (Flowelement nthDependentFlowelement : dependentFlowelementList) {
			logger.trace("Now processing - " + nthDependentFlowelement);
			Flow flowRef = nthDependentFlowelement.getFlowRef();
			
			if(flowRef != null) { // If the flowelement has a flowref, that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input
				logger.trace("References a flow again " + flowRef.getId());
				iterateDependentFlowelements(job, request, flowRef, nthDependentFlowelement, jobList);
			} else {
				jobList.addAll(createJobs(nthDependentFlowelement, job, request, dependentJobInputArtifact.getArtifactclass().getId(), dependentJobInputArtifact));
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
	 * dependent flow element passed here results in
	 * Either 
	 * As many jobs configured in artifactclassVolume (hence the method name createJobs - Note the Jobs plural)
	 * 		if Action = Storage or (Action = Processing but one of the dependency is storage)
	 * OR
	 * 1 job for a processing tasks 
	 */
	private synchronized List<Job> createJobs(Flowelement flowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {
		List<Job> jobsCreated = new ArrayList<Job>();
		
		Action storagetaskAction = flowelement.getStoragetaskActionId();
		String processingtaskId = flowelement.getProcessingtaskId();
		boolean processingtaskWithDependencyStoragetask = false;
		if(processingtaskId != null) {
			List<Integer> flowelementDependenciesList = flowelement.getDependencies();
			// Now check if any of the dependency is a storage task
			if(flowelementDependenciesList != null) {
				for (Integer nthFlowelementDependencyId : flowelementDependenciesList) {
					Flowelement prereqFlowelement = flowelementDao.findById(nthFlowelementDependencyId).get();
					Action storagetaskDependency = prereqFlowelement.getStoragetaskActionId();  
					if(storagetaskDependency != null) { // Is the dependency a Storage task?
						processingtaskWithDependencyStoragetask = true;
						break;
					}
				}
			}
		}
		// if the sourceJob for which the dependants are to be created is a processing task and the flowelement is a storagetask we may have to create as many jobs as copies needed...
		// But we should verify if they are not already created and create here... - 
		// We might have race conditions with write completion thread creating one and just at the same time this one does too... Need to ensure that doesnt happen...
		// TODO Synchronize this...
		if(storagetaskAction != null || processingtaskWithDependencyStoragetask) {
			if(sourceJob == null || (sourceJob != null && sourceJob.getProcessingtaskId() != null)) { // only srcjob = processing task is from scheduler which needs multiple job creation 
				logger.trace("Creating Job(s) for " + flowelement.getId() + ":" + storagetaskAction);
				List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(artifactclassId);
				logger.trace("No. of copies for artifactclass " + artifactclassId + "-" + artifactclassVolumeList.size());
				for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
					Volume grpVolume = artifactclassVolume.getVolume();

					List<Integer> dependentJobIds = new ArrayList<Integer>();
					if(sourceJob != null) {
						dependentJobIds.add(sourceJob.getId());
					}
					// validating if all its other dependencies are complete
					boolean isJobGoodToBeCreated = isJobGoodToBeCreated(flowelement, sourceJob, request, artifactclassId, artifact, grpVolume.getId(), dependentJobIds);
					if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
						Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
						if(dependentJobIds.size() > 0) {
							Collections.sort(dependentJobIds);
							job.setDependencies(dependentJobIds);
						}
						job.setStoragetaskActionId(storagetaskAction);
						job.setProcessingtaskId(processingtaskId);
						job.setGroupVolume(grpVolume); // we dont know the physical volume yet... How about provisioned volumes?

						job = saveJob(job);
						jobsCreated.add(job);
					}
				}
			}
			else { // sourceJob != null - called from a storage dependency job 
				logger.trace("Creating Job for flowelement " + flowelement.getId() + " from source Job " + sourceJob.getId());
				List<Integer> dependentJobIds = new ArrayList<Integer>();
				if(sourceJob != null) {
					dependentJobIds.add(sourceJob.getId());
				}
				
				// validating if all its other dependencies are complete
				boolean isJobGoodToBeCreated = isJobGoodToBeCreated(flowelement, sourceJob, request, artifactclassId, artifact, null, dependentJobIds);

				if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
					Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
					job.setStoragetaskActionId(storagetaskAction);
					job.setProcessingtaskId(processingtaskId);
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
			logger.trace("Creating Job for " + flowelement.getId() + ":" + processingtaskId);
			String inputArtifactName = artifact.getName();
			Artifactclass inputArtifactclass = artifact.getArtifactclass();
			String inputArtifactPath = inputArtifactclass.getPath() + File.separator + inputArtifactName;
			Volume groupVolume = null;
			String groupVolumeId = null;
			// If sourceJob of this processing flowelement is restore then the processing f/w should pick up the file from the restored tmp location instead of the artifactclass.pathprefix 
			if(sourceJob != null && sourceJob.getStoragetaskActionId() == Action.restore) {
				inputArtifactPath = restoreStorageTask.getRestoreTempLocation(sourceJob.getId()) + File.separator + inputArtifactName;
				groupVolume = sourceJob.getGroupVolume();
				//groupVolumeId = groupVolume != null ? groupVolume.getId() : null;
			}
			// TODO : What if the first parent job is a processing that has no files to process and hence no job created. So DependentJobs wont be created. Fine. So there is no Job in the request. Is it ok to create a request with no Jobs
			ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
			if(!processingJobManager.isJobToBeCreated(processingtaskId, inputArtifactPath, inputArtifactclass)) { // Dont create jobs for something we know would eventually fail...
				logger.trace("Job not to be created - " + flowelement.getId());
				return jobsCreated;
			}
			
			List<Integer> dependentJobIds = new ArrayList<Integer>();
			if(sourceJob != null) {
				dependentJobIds.add(sourceJob.getId());
			}
			
			// validating if all its other dependencies are complete
			boolean isJobGoodToBeCreated = isJobGoodToBeCreated(flowelement, sourceJob, request, artifactclassId, artifact, groupVolumeId, dependentJobIds);
			if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
				logger.trace("Creating Job for " + processingtaskId + ":" + flowelement.getId());
				Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
				
				if(dependentJobIds.size() > 0) {
					Collections.sort(dependentJobIds);
					job.setDependencies(dependentJobIds);
				}
				
				job.setProcessingtaskId(processingtaskId);
				if(groupVolume != null)
					job.setGroupVolume(groupVolume);
				job = saveJob(job);
				jobsCreated.add(job);
			}
		}
		return jobsCreated;
	}

	private boolean isJobGoodToBeCreated(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact, String groupVolumeId, List<Integer> dependentJobIds) {
		boolean isJobGoodToBeCreated = true;
		logger.trace("Validating if all dependencies Jobs of flowelement " + nthFlowelement + " are created and completed");
		List<Integer> preRequesiteFlowelements = nthFlowelement.getDependencies();
		if(preRequesiteFlowelements != null) {
			for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
				if(sourceJob != null && sourceJob.getFlowelement().getId() == nthPreRequesiteFlowelementId)
					continue;
				
				logger.trace("Now verifying if flowelement " + nthFlowelement + "'s dependency flowelement " + nthPreRequesiteFlowelementId + " has job created and completed");
				if(groupVolumeId == null) {
					Flowelement prereqFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get();
					if(prereqFlowelement.getStoragetaskActionId() != null && sourceJob.getGroupVolume() != null)
						groupVolumeId = sourceJob.getGroupVolume().getId();
					logger.trace("Group Volume Id " + groupVolumeId);
				}				
				Job jobInQuestion = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifact.getId(), nthPreRequesiteFlowelementId, groupVolumeId);
				
				if(jobInQuestion == null || jobInQuestion.getStatus() != Status.completed) {
					isJobGoodToBeCreated = false;
					if(jobInQuestion == null)
						logger.trace("Job for requestId " + request.getId() + " artifactId " + artifact.getId() +  " flowelement " + nthPreRequesiteFlowelementId + " groupVolumeId " + groupVolumeId + " is not created yet");
					else
						logger.trace(jobInQuestion + " is not completed yet");
					break;
				}
				dependentJobIds.add(jobInQuestion.getId());
			}	
		}
		logger.debug("Is all dependencies Job for flowelement " + nthFlowelement + " are complete and Job good to be created - " + isJobGoodToBeCreated);
		return isJobGoodToBeCreated;
	}
	
	private Job createJob(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {
		Job job = new Job();
		job.setInputArtifactId(artifact.getId());
		job.setFlowelement(nthFlowelement);
		job.setRequest(request);				
		job.setCreatedAt(LocalDateTime.now());
		
		Status status = Status.queued;
//		if(sourceJob == null) {
			ArtifactclassTask artifactclassTask = null;
			if(nthFlowelement.getProcessingtaskId() != null)
				artifactclassTask = artifactclassTaskDao.findByArtifactclassIdAndProcessingtaskId(artifactclassId, nthFlowelement.getProcessingtaskId());
			else if(nthFlowelement.getStoragetaskActionId() != null)
				artifactclassTask = artifactclassTaskDao.findByArtifactclassIdAndStoragetaskActionId(artifactclassId, nthFlowelement.getStoragetaskActionId());

			if(artifactclassTask != null && artifactclassTask.getConfig().isCreateHeldJobs())
				status = Status.on_hold;
//		}
		job.setStatus(status);
		return job;
	}
	
	// public so can be used in unit testing
	public List<Flowelement> getDependentFlowelements(Flow flow, Flowelement flowelement) {
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc(flow.getId());
		List<Flowelement> dependentFlowelementList = new ArrayList<Flowelement>();
		for (Flowelement nthFlowelement : flowelementList) {
			if(nthFlowelement.getId() == flowelement.getId())
				continue;
			
			List<Integer> preReqs = nthFlowelement.getDependencies();
			if(flowelement.getFlowRef() != null) {
				if(preReqs == null)
					dependentFlowelementList.add(nthFlowelement);
			}
			else if(preReqs != null && preReqs.contains(flowelement.getId())) {
				dependentFlowelementList.add(nthFlowelement);
			}
		}
		return dependentFlowelementList;
	}
	
	private Job saveJob(Job job) {
		//logger.debug("DB Job row Creation");   
		job = jobDao.save(job);
		logger.info(DwaraConstants.JOB + job.getId());
		return job;
	}
}

