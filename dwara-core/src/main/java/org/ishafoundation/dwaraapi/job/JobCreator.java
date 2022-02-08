package org.ishafoundation.dwaraapi.job;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.TagDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassFlowDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.ArtifactDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassFlow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.master.jointables.json.Taskconfig;
import org.ishafoundation.dwaraapi.db.model.master.jointables.json.Taskconfig.IncludeExcludeProperties;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.utils.ArtifactclassUtil;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.db.utils.FlowelementUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlow;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.checksum.ChecksumVerifier;
import org.ishafoundation.dwaraapi.process.thread.ProcessingJobManager;
import org.ishafoundation.dwaraapi.service.JobServiceRequeueHelper;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.ishafoundation.dwaraapi.storage.storagetask.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Naming convention - 
 * Dependencies - For checksum-verify flowelement/job, checksum-gen and restore are dependencies/prerequisites
 * Depende(a)nts - For a restore job, checksum-verify is a dependent.
 */

@Component
public class JobCreator {

	private static final Logger logger = LoggerFactory.getLogger(JobCreator.class);

	@Autowired
	private JobDao jobDao;
	
	@Autowired
	private ArtifactDao artifactDao;

	@Autowired
	private ActionArtifactclassFlowDao actionArtifactclassFlowDao;

	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;
	
	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;
	
	@Autowired
	private FlowelementUtil flowelementUtil;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Restore restoreStorageTask;
	
	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private JobServiceRequeueHelper jobServiceRequeueHelper;
	
	@Autowired
	private ArtifactclassUtil artifactclassUtil;

	// only if action is async create job should be called...
	public List<Job> createJobs(Request request, Artifact sourceArtifact){
		Action requestedBusinessAction = request.getActionId();
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);

		List<Job> jobList = new ArrayList<Job>();

		if(Actiontype.complex == action.getType()) {
			logger.trace("Complex action");
			List<ActionArtifactclassFlow> actionArtifactclassFlowList = null;
			String sourceArtifactclassId = null;
			if(requestedBusinessAction == Action.ingest) {
				sourceArtifactclassId = sourceArtifact.getArtifactclass().getId();
				// get all the flows for the action on the artifactclass - Some could be global across artifactclasses and some specific to that artifactclass. so using "_all_" for global
				actionArtifactclassFlowList = actionArtifactclassFlowDao.findAllByIdArtifactclassIdAndActionIdAndActiveTrue(sourceArtifactclassId, requestedBusinessAction.name()); //
				
				if(actionArtifactclassFlowList == null || actionArtifactclassFlowList.size() == 0)
					throw new DwaraException("No flow configured for " + sourceArtifactclassId + " in action_artifactclass_flow");

			}else if(requestedBusinessAction == Action.restore_process) {
				jobList.addAll(iterateFlow(request, sourceArtifact, request.getDetails().getFlowId()));
			}
			else if(requestedBusinessAction == Action.rewrite) {
				jobList.addAll(iterateFlow(request, sourceArtifact, CoreFlow.core_rewrite_flow.getFlowName()));
			}
			
			if(actionArtifactclassFlowList != null) {
				for (ActionArtifactclassFlow actionArtifactclassFlow : actionArtifactclassFlowList) {
					String nthFlowId = actionArtifactclassFlow.getId().getFlowId();
					logger.trace("flow " + nthFlowId);
					jobList.addAll(iterateFlow(request, sourceArtifact, nthFlowId));
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

	private List<Job> iterateFlow(Request request, Artifact artifact, String flowId) {
		List<Job> jobsCreated = new ArrayList<Job>();
		
		logger.trace("Iterating flow " + flowId);
		String artifactclassId = artifact != null ? artifact.getArtifactclass().getId() : null; 
		logger.trace("artifactclassId " + artifactclassId);
		logger.trace("artifactId " + (artifact != null ? artifact.getId() : null));
		//  get all the flow elements for the flow
		List<Flowelement> flowelementList = flowelementUtil.getAllFlowElements(flowId);
		for (Flowelement nthFlowelement : flowelementList) {
			logger.trace("Flowelement " + nthFlowelement.getId());
			
			List<String> refFlowelementDepsList = nthFlowelement.getDependencies();
			if(refFlowelementDepsList == null) {
				jobsCreated.addAll(createJobs(nthFlowelement, null, request, artifactclassId, artifact, false));
			}
		}
		return jobsCreated;
	}

	public List<Job> hasDependentJobsToBeCreated(Job job){
		return createDependentJobs_internal(job, true);
	}
	
	public List<Job> createDependentJobs(Job job){
		return createDependentJobs_internal(job, false);
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
	private List<Job> createDependentJobs_internal(Job job, boolean dryRun){
		List<Job> jobsCreated = new ArrayList<Job>();
		String flowelementId = job.getFlowelementId();
		
		
		if(flowelementId != null) {
			Flowelement flowelement = flowelementUtil.findById(flowelementId);
			String flowId = flowelement.getFlowId();
			Request request = job.getRequest();
			
			logger.trace("Creating dependent job(s) for sourceJob " + job.getId() + " with flowelement " + flowelement);
			iterateDependentFlowelements(job, request, flowId, flowelement, jobsCreated, dryRun);
			logger.trace("Dependent job(s) created for " + job.getId() + " - " + jobsCreated);
		}else
			logger.trace(job.getId() + " not a complex task and doesnt involve a flow");
		
		return jobsCreated;
	}

	private void iterateDependentFlowelements(Job job, Request request, String flowId, Flowelement flowelement, List<Job> jobList, boolean dryRun) {
		List<Flowelement> dependentFlowelementList = getDependentFlowelements(flowId, flowelement);
		logger.trace("Dependent Flowelements of flowelement " + flowelement.getId() + " - " + dependentFlowelementList);
		
		Integer dependentJobInputArtifactId = job.getOutputArtifactId() != null ? job.getOutputArtifactId() : job.getInputArtifactId();
		Artifact dependentJobInputArtifact = null;
		if(dependentJobInputArtifactId != null)
			dependentJobInputArtifact = artifactDao.findById(dependentJobInputArtifactId).get();
		
		for (Flowelement nthDependentFlowelement : dependentFlowelementList) {
			logger.trace("Now processing - " + nthDependentFlowelement);
			String flowRefId = nthDependentFlowelement.getFlowRefId();
			
			if(flowRefId != null) { // If the flowelement has a flowref, that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input
				logger.trace("References a flow again " + flowRefId);
				iterateDependentFlowelements(job, request, flowRefId, nthDependentFlowelement, jobList, dryRun);
			} else {
				String artifactclassId = dependentJobInputArtifact!=null ? dependentJobInputArtifact.getArtifactclass().getId() : null;
				jobList.addAll(createJobs(nthDependentFlowelement, job, request, artifactclassId, dependentJobInputArtifact, dryRun));
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
	private synchronized List<Job> createJobs(Flowelement flowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact, boolean dryRun) {
		List<Job> jobsCreated = new ArrayList<Job>();
		
		Action storagetaskAction = flowelement.getStoragetaskActionId();
		String processingtaskId = flowelement.getProcessingtaskId();

		boolean continueWithJobCreation = dealWithInclusionExclusion(flowelement, artifactclassId, artifact);
		if(!continueWithJobCreation)
			return jobsCreated;
		
		if(request.getActionId() == Action.ingest) {
			
			if(sourceJob != null) { // avoid this for fresh ingest
				continueWithJobCreation = dealWithMarkedFailedJobs(flowelement, sourceJob, request, artifactclassId, artifact);
				if(!continueWithJobCreation)
					return jobsCreated;
			}
			
			boolean processingtaskWithDependencyStoragetask = false;
			if(processingtaskId != null) {
				List<String> flowelementDependenciesList = flowelement.getDependencies();
				// Now check if any of the dependency is a storage task
				if(flowelementDependenciesList != null) {
					for (String nthFlowelementDependencyId : flowelementDependenciesList) {
						Flowelement prereqFlowelement = flowelementUtil.findById(nthFlowelementDependencyId);
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
						boolean isJobGoodToBeCreated = isJobGoodToBeCreatedOrRequeued(flowelement, sourceJob, request, artifactclassId, artifact, grpVolume.getId(), dependentJobIds);
						if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
							Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
							if(dependentJobIds.size() > 0) {
								Collections.sort(dependentJobIds);
								job.setDependencies(dependentJobIds);
							}
							job.setStoragetaskActionId(storagetaskAction);
							job.setProcessingtaskId(processingtaskId);
							job.setGroupVolume(grpVolume); // we dont know the physical volume yet... How about provisioned volumes?
							Volume volume = null;
							for (Integer nthDependentJobId : dependentJobIds) {
								if(sourceJob.getId() == nthDependentJobId) {
									volume = sourceJob.getVolume();
								}
								else {
									Job nthDependentJob = jobDao.findById(nthDependentJobId).get();
									volume = nthDependentJob.getVolume();
								}
								if(volume != null)
									break;
							}
							if(volume != null)
								job.setVolume(volume);
							
							job = saveJob(job, dryRun);
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
					boolean isJobGoodToBeCreated = isJobGoodToBeCreatedOrRequeued(flowelement, sourceJob, request, artifactclassId, artifact, null, dependentJobIds);
	
					if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
						Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
						job.setStoragetaskActionId(storagetaskAction);
						job.setProcessingtaskId(processingtaskId);
						job.setGroupVolume(sourceJob.getGroupVolume()); // we already know the physical volume used by its parent job
						job.setVolume(sourceJob.getVolume());
						Collections.sort(dependentJobIds);
						job.setDependencies(dependentJobIds);
						job = saveJob(job, dryRun);
						jobsCreated.add(job);
					}
				}
			}
			else { // processing task with no storage dependency
				Job job = createProcessingJob(processingtaskId, flowelement, sourceJob, request, artifactclassId, artifact, dryRun);
				if(job != null)
					jobsCreated.add(job);
			}
		}
		else if(request.getActionId() == Action.restore_process){
			if(storagetaskAction != null) {
				Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
				job.setStoragetaskActionId(storagetaskAction);
				job = saveJob(job, dryRun);
				jobsCreated.add(job);
			} else {
				Job job = createProcessingJob(processingtaskId, flowelement, sourceJob, request, artifactclassId, artifact, dryRun);
				if(job != null)
					jobsCreated.add(job);
			}
		}
		else if(request.getActionId() == Action.rewrite) {
			if(storagetaskAction != null) {
				Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
				job.setStoragetaskActionId(storagetaskAction);
				List<Integer> dependentJobIds = new ArrayList<Integer>();
				if(sourceJob != null) {
					dependentJobIds.add(sourceJob.getId());
				}
				if(dependentJobIds.size() > 0) {
					Collections.sort(dependentJobIds);
					job.setDependencies(dependentJobIds);
				}
				job = saveJob(job, dryRun);
				jobsCreated.add(job);
			}else {
				Job job = createProcessingJob(processingtaskId, flowelement, sourceJob, request, artifactclassId, artifact, dryRun);
				if(job != null)
					jobsCreated.add(job);
			}
		}

		return jobsCreated;
	}
 
	/**
	 * deals With Inclusion Exclusion
	 * @param flowelement
	 * @param artifactclassId
	 * @param artifact
	 * @returns if job should be created or not for the configured flowelement
	 */
	private boolean dealWithInclusionExclusion(Flowelement flowelement, String artifactclassId, Artifact artifact){
		boolean isJobToBeCreated = true;
		logger.trace("Dealing with Inc/Exc for " + flowelement.getId() + " : " + artifactclassId + " : " + (artifact != null ? artifact.getId() : null));
		Taskconfig taskconfig =	flowelement.getTaskconfig();
		logger.trace("taskconfig " + taskconfig);
		if(taskconfig != null) {
			logger.trace("taskconfig configured for "+ flowelement.getId());
			IncludeExcludeProperties excludeProperties = taskconfig.getExcludeIf();
			if(excludeProperties != null) {
				logger.trace("Has exclude props" + excludeProperties);
				boolean isExcludeJob = doIncludeExcludePropertiesMatch(excludeProperties, artifactclassId, artifact);
				if(isExcludeJob) {
					logger.info("Excluding Flowelement " + flowelement.getId());
					isJobToBeCreated = false;
					return isJobToBeCreated;
				}
			}
			
			IncludeExcludeProperties includeProperties = taskconfig.getIncludeIf();
			if(includeProperties != null) {
				logger.trace("Has include props" + includeProperties);
				boolean isIncludeJob = doIncludeExcludePropertiesMatch(includeProperties, artifactclassId, artifact);
				if(!isIncludeJob) { // only proceed creating job if condition is met else return
					logger.info("Flowelement " + flowelement.getId() + "doesnt match include condition");
					isJobToBeCreated = false;
					return isJobToBeCreated;
				}
			}
		}
		return isJobToBeCreated;
	}
	
	private boolean doIncludeExcludePropertiesMatch(IncludeExcludeProperties includeExcludeProperties, String artifactclassId, Artifact artifact){	
		boolean isMatch = false;
		
		String tag = includeExcludeProperties.getTag();
		String artifactclassRegex = includeExcludeProperties.getArtifactclassRegex();
		if(tag != null) {
			boolean isSource = artifact.getArtifactclass().isSource();
			Set<Tag> tags = null;
			
			if(isSource) {
				tags = artifact.getTags(); 
				
				if(tags == null){
					// During ingest(StagedService) tags isnt set properly in artifact object - hence had to hit DB ...
					List<Tag> tagList = tagDao.findByArtifacts_Id(artifact.getId());
					if(tagList != null)
						tags = new HashSet<Tag>(tagList);
				}
			}
			else { // if artifact is a derived artifact - tags are only saved with the source artifacts - so get the tag collection from source artifact 
				try {
					tags = artifactDao.findByArtifactRef(artifact).getTags();
				} catch (Exception e) {
					logger.error("Unable to get tags info from source artifact for " + artifact.getId() + " : " + e.getMessage());
				}
			}
			if(tags != null) {
				for (Tag nthTag : tags) {
					if(nthTag.getTag().equals(tag)) {
						isMatch = true;
						break;
					}
				}
			}
		}
		
		if(artifactclassRegex != null) {
			Pattern artifactclassRegexPattern = Pattern.compile(artifactclassRegex);
			Matcher artifactclassRegexMatcher = artifactclassRegexPattern.matcher(artifactclassId); // only in relative to the artifact path 
			if(artifactclassRegexMatcher.matches()) {
				isMatch = true;
			}
		}
		
		return isMatch;
	}

	private Job createProcessingJob(String processingtaskId, Flowelement flowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact, boolean dryRun){
		logger.trace("Creating processing Job for " + flowelement.getId() + ":" + processingtaskId);
		String inputArtifactName = artifact.getName();
		Artifactclass inputArtifactclass = artifact.getArtifactclass();
		String inputArtifactPath = artifactclassUtil.getPath(inputArtifactclass) + File.separator + inputArtifactName;
		Volume groupVolume = null;
		Volume volume = null;
		String groupVolumeId = null;
		// If sourceJob of this processing flowelement is restore then the processing f/w should pick up the file from the restored tmp location instead of the artifactclass.pathprefix 
		if(sourceJob != null && sourceJob.getStoragetaskActionId() == Action.restore) {
			inputArtifactPath = restoreStorageTask.getRestoreLocation(sourceJob) + File.separator + inputArtifactName;
			groupVolume = sourceJob.getGroupVolume();
			volume = sourceJob.getVolume();
			//groupVolumeId = groupVolume != null ? groupVolume.getId() : null;
		}
		// TODO : What if the first parent job is a processing that has no files to process and hence no job created. So DependentJobs wont be created. Fine. So there is no Job in the request. Is it ok to create a request with no Jobs
		ProcessingJobManager processingJobManager = applicationContext.getBean(ProcessingJobManager.class);
		Taskconfig taskconfig =	flowelement.getTaskconfig();
		String pathnameRegex = null;
		if(taskconfig != null)
			pathnameRegex = taskconfig.getPathnameRegex();
		if(!processingJobManager.isJobToBeCreated(processingtaskId, inputArtifactPath, pathnameRegex)) { // Dont create jobs for something we know would eventually fail...
			logger.trace("Job not to be created - " + flowelement.getId());
			return null;
		}
		
		List<Integer> dependentJobIds = new ArrayList<Integer>();
		if(sourceJob != null) {
			dependentJobIds.add(sourceJob.getId());
		}
		
		// validating if all its other dependencies are complete
		boolean isJobGoodToBeCreated = isJobGoodToBeCreatedOrRequeued(flowelement, sourceJob, request, artifactclassId, artifact, groupVolumeId, dependentJobIds);
		if(isJobGoodToBeCreated) { // if all dependency jobs are complete...
			logger.trace("Creating Job for " + processingtaskId + ":" + flowelement.getId());
			Job job = createJob(flowelement, sourceJob, request, artifactclassId, artifact);
			
			if(dependentJobIds.size() > 0) {
				Collections.sort(dependentJobIds);
				job.setDependencies(dependentJobIds);
			}
			
			job.setProcessingtaskId(processingtaskId);
			if(processingtaskId.equals(ChecksumVerifier.CHECKSUM_VERIFIER_COMPONENT_NAME)) { // only updating this for checksum-verify jobs which are sort of connected to tape even though its a processing job.  Used to flag tape as suspect for checksum failures
				if(groupVolume != null)
					job.setGroupVolume(groupVolume);
				if(volume != null)
					job.setVolume(volume);
			}
			job = saveJob(job, dryRun);
			return job;
		}
		return null;
	}
	
	private boolean isJobGoodToBeCreatedOrRequeued(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact, String groupVolumeId, List<Integer> dependentJobIds) {
		boolean isJobGoodToBeCreated = true;
		logger.trace("nthFlowelement " + nthFlowelement.getId() + " sourceJob " + (sourceJob != null ? sourceJob.getId() : null) + " request " + (request != null ? request.getId() : null) + " artifactclassId " + artifactclassId + " artifact " +  (artifact != null ? artifact.getId() : null) + " groupVolumeId " + groupVolumeId );
		logger.trace("Validating if all dependencies Jobs of flowelement " + nthFlowelement + " are created and completed"); // For eg., For checksum-verify job to be created, both checksum-gen and restore are dependencies/prerequisites and had jobs created and completed.
		List<String> preRequesiteFlowelements = nthFlowelement.getDependencies();
		if(preRequesiteFlowelements != null) {
			for (String nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
				if(sourceJob != null && sourceJob.getFlowelementId().equals(nthPreRequesiteFlowelementId))
					continue;

				Flowelement prereqFlowelement = flowelementUtil.findById(nthPreRequesiteFlowelementId);
				logger.trace("Now verifying if flowelement " + nthFlowelement + "'s dependency flowelement " + nthPreRequesiteFlowelementId + " has job created and completed");
				if(groupVolumeId == null) {
					if(prereqFlowelement.getStoragetaskActionId() != null && sourceJob.getGroupVolume() != null)
						groupVolumeId = sourceJob.getGroupVolume().getId();
					logger.trace("Group Volume Id " + groupVolumeId);
				}				
				Job jobInQuestion = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifact.getId(), nthPreRequesiteFlowelementId, groupVolumeId);
				
				if(jobInQuestion == null || jobInQuestion.getStatus() != Status.completed) {
					if(logger.isTraceEnabled()) {
						if(jobInQuestion == null) {
								String msgPrefix = "Job for requestId " + request.getId() + " artifactId " + artifact.getId() +  " flowelement " + nthPreRequesiteFlowelementId + " groupVolumeId " + groupVolumeId;
								boolean isJobSupposedToBeCreated = dealWithInclusionExclusion(prereqFlowelement, artifactclassId, artifact);
								if(isJobSupposedToBeCreated) {
									logger.trace(msgPrefix + " is not created yet");
								} else {
									logger.trace(msgPrefix + " not configured to be created anyway. Check Flowelement " + prereqFlowelement.getId() + " taskconfig");
								}
						}
						else if(jobInQuestion.getStatus() != Status.completed) {
							logger.trace(jobInQuestion + " is not completed yet");	
							
						}
					}
					isJobGoodToBeCreated = false;
					break;
				}
				dependentJobIds.add(jobInQuestion.getId());
			}	
		}
		logger.debug("Is all dependencies Job for flowelement " + nthFlowelement + " are complete and Job good to be created - " + isJobGoodToBeCreated);
		return isJobGoodToBeCreated;
	}
	
	/*
	 * Deals with marked_failed jobs in 2 aspects
	 * 1) Should the dependent jobs to be let(let because job creation happens after this) created by further down code or if already created - don't let continue
	 * 2) Should the dependent job be requeued - by checking its dependencies are completed or not
	 * 
	 *  For eg., lets say proxy ChecksumGen(CG) and the proxy write jobs are intially marked-failed, then the already created job hierarchy structure are all marked-failed
		CG
		|Write1
		|	Restore2
		--------CV1
		|Write2
		|	Restore2
		--------CV2
	*
	*  This method takes care of scenarios like
	*  1) CG complete first, W/R job completes after CG - Respective R copy will requeue respective CV - Straight forward
	*  2) R2 completed first, CV2 still marked-failed, CG Completes but R1 still on, CV2 requeued but CV1 still remains marked-failed, R1 completes CV1 requeued
	*  3) R1/R2 completes, CV1/2 remains marked-failed, CG Completes, both CV1/2 requeued... 
	*/			
	private boolean dealWithMarkedFailedJobs(Flowelement flowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {
		boolean continueJobCreation = true;
		List<Job> jobList = new ArrayList<Job>();
		String groupVolumeId = null;
		if(sourceJob.getGroupVolume() != null) {
			groupVolumeId = sourceJob.getGroupVolume().getId();
			logger.trace("Group Volume Id " + groupVolumeId);
			Job jobInQuestion = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifact.getId(), flowelement.getId(), groupVolumeId);
			if(jobInQuestion != null) { //  && jobInQuestion.getStatus() == Status.marked_failed
				jobList.add(jobInQuestion);
				continueJobCreation = false;
			}
		}else {
			jobList = jobDao.findAllByRequestIdAndInputArtifactIdAndFlowelementId(request.getId(), artifact.getId(), flowelement.getId());
			List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(artifactclassId);
			if(artifactclassVolumeList.size() == jobList.size())
				continueJobCreation = false;
		}
		
		for (Job nthJob : jobList) {
			List<Integer> dependentJobIds = new ArrayList<Integer>();// dummy
			
			if(nthJob.getStatus() == Status.marked_failed) {
				// only requeue the job if all its other dependencies are complete
				boolean isJobGoodToBeRequeued = isJobGoodToBeCreatedOrRequeued(flowelement, sourceJob, request, artifactclassId, artifact, sourceJob.getStoragetaskActionId() != null ? null : nthJob.getGroupVolume().getId(), dependentJobIds);
	
				if(isJobGoodToBeRequeued) { // if all dependency jobs are complete...
					try {
						logger.info("Requeuing the marked_failed job " + nthJob.getId());
						jobServiceRequeueHelper.requeueJob(nthJob.getId(),DwaraConstants.SYSTEM_USER_NAME);
					} catch (Exception e1) {
						logger.error("Unable to auto requeue failed job..." + nthJob.getId(), e1);
					}
				}
			}
		}
		
		return continueJobCreation;
	}
	
	private Job createJob(Flowelement nthFlowelement, Job sourceJob, Request request, String artifactclassId, Artifact artifact) {
		Job job = new Job();
		job.setInputArtifactId(artifact != null ? artifact.getId() : null);
		job.setFlowelementId(nthFlowelement.getId());
		job.setRequest(request);				
		job.setCreatedAt(LocalDateTime.now());
		
		Status status = Status.queued;
		if(artifactclassId != null) {
			Taskconfig taskconfig =	nthFlowelement.getTaskconfig();
			if(taskconfig != null && taskconfig.isCreateHeldJobs())
				status = Status.on_hold;
		}
		job.setStatus(status);
		return job;
	}
	
	// public so can be used in unit testing
	public List<Flowelement> getDependentFlowelements(String flowId, Flowelement flowelement) {
		List<Flowelement> flowelementList = flowelementUtil.getAllFlowElements(flowId);
		List<Flowelement> dependentFlowelementList = new ArrayList<Flowelement>();
		for (Flowelement nthFlowelement : flowelementList) {
			if(nthFlowelement.getId().equals(flowelement.getId()))
				continue;
			
			List<String> preReqs = nthFlowelement.getDependencies();
			if(flowelement.getFlowRefId() != null) {
				if(preReqs == null)
					dependentFlowelementList.add(nthFlowelement);
			}
			else if(preReqs != null && preReqs.contains(flowelement.getId())) {
				dependentFlowelementList.add(nthFlowelement);
			}
		}
		return dependentFlowelementList;
	}
	
	private Job saveJob(Job job, boolean dryRun) {
		//logger.debug("DB Job row Creation");   
		if(!dryRun) {
			job = jobDao.save(job);
			logger.info(DwaraConstants.JOB + job.getId());
		}
		return job;
	}
}

