package org.ishafoundation.dwaraapi.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassFlowDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Flow;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassFlow;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
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
	private ActionArtifactclassFlowDao actionArtifactclassFlowDao;

	@Autowired
	private FlowelementDao flowelementDao;

	@Autowired
	private ArtifactclassVolumeDao artifactclassVolumeDao;

	@Autowired
    private ProcessingtaskDao processingtaskDao;

	@Autowired
	private Map<String, AbstractStoragetaskAction> storagetaskActionMap;

	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;


	// only if action is async create job should be called...
	public List<Job> createJobs(Request request, Artifact sourceArtifact){
		Action requestedBusinessAction = request.getActionId();
		List<Integer>  flowelementsToBeSkipped = request.getDetails().getSkipActionelements();
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);

		List<Job> jobList = new ArrayList<Job>();

		if(Actiontype.complex == action.getType()) {
			String sourceArtifactclassId = sourceArtifact.getArtifactclass().getId();
			// get all the flows for the action on the artifactclass - Some could be global across artifactclasses and some specific to that artifactclass. so using "_all_" for global
			List<ActionArtifactclassFlow> actionArtifactclassFlowList = actionArtifactclassFlowDao.findAllByArtifactclassIdOrArtifactclassIdAndActionIdAndActiveTrue("_all_", sourceArtifactclassId, requestedBusinessAction.name()); //
			for (ActionArtifactclassFlow actionArtifactclassFlow : actionArtifactclassFlowList) {
				String nthFlowId = actionArtifactclassFlow.getFlow().getId();

				// TODO Skip individual flow - How? Need to be defined

				Map<String, Job> flowelementId_Job_Map = new HashMap<>();// used for getting referenced jobs
				Map<String, Job> flowelementId_CopyNumber_Job_Map = new HashMap<>();

				iterateFlow(request, sourceArtifactclassId, sourceArtifact.getId(), nthFlowId, null, jobList, flowelementId_Job_Map, flowelementId_CopyNumber_Job_Map);

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

	private void iterateFlow(Request request, String artifactclassId, Integer artifactId, String nthFlowId, Flowelement flowRefFlowelement, List<Job> jobList,
			Map<String, Job> flowelementId_Job_Map, Map<String, Job> flowelementId_CopyNumber_Job_Map) {
		//  get all the flow elements for the flow
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndActiveTrueOrderByDisplayOrderAsc(nthFlowId);
		for (Flowelement nthFlowelement : flowelementList) {
			Flow flowRef = nthFlowelement.getFlowRef();
			
			if(flowRef != null) { // If the flowelement has a flowref, that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input
				List<Integer> refFlowelementDepsList = nthFlowelement.getDependencies();
				if(refFlowelementDepsList == null) {
					logger.warn("Are you sure you dont want any dependencies defined on - " + nthFlowelement.getId() + ". Whats the point of this flowelement with flowref then?");
				}else {
					// Now use one of the processing task that too generating an output
					String outputArtifactclassSuffix = null;
					for (Integer nthRefFlowelementDepId : refFlowelementDepsList) {
						Flowelement prereqFlowelement = flowelementDao.findById(nthRefFlowelementDepId).get();
						String processingtaskId = prereqFlowelement.getProcessingtaskId();  
						if(processingtaskId == null) // Is the dependency a processing task?
							continue;
						Processingtask processingtask = processingtaskDao.findById(processingtaskId).get();
						outputArtifactclassSuffix = processingtask.getOutputArtifactclassSuffix(); // Does the dependent processing task generate an output?
						if(outputArtifactclassSuffix != null)
							break;
					}
					String outputArtifactclassId = request.getDetails().getArtifactclassId() + outputArtifactclassSuffix;
					iterateFlow(request, outputArtifactclassId, null, flowRef.getId(), nthFlowelement, jobList, flowelementId_Job_Map, flowelementId_CopyNumber_Job_Map);
				}
			} else {
				createJob(request, artifactclassId, artifactId, flowRefFlowelement, nthFlowelement, jobList, flowelementId_Job_Map, flowelementId_CopyNumber_Job_Map);
			}
		}
	}

	private void createJob(Request request,  String artifactclassId, Integer artifactId, Flowelement flowRefFlowelement, Flowelement nthFlowelement, List<Job> jobList,
			Map<String, Job> flowelementId_Job_Map, Map<String, Job> flowelementId_CopyNumber_Job_Map) {

		String putKeyPrefix="";
		String getKeyPrefix = "";
		List<Integer> preRequesiteFlowelements = null;
		if(flowRefFlowelement == null) {
			preRequesiteFlowelements = nthFlowelement.getDependencies();
		}
		else {
			putKeyPrefix = flowRefFlowelement.getId() + "_";
			if(nthFlowelement.getDependencies() == null)
				preRequesiteFlowelements = flowRefFlowelement.getDependencies();
			if(nthFlowelement.getDependencies() != null) {
				preRequesiteFlowelements = nthFlowelement.getDependencies();
				getKeyPrefix = flowRefFlowelement.getId() + "_";
			}
		}
		// TODO Skip individual flow elements - How? Need to be defined
		Action storagetaskAction = nthFlowelement.getStoragetaskActionId();

		if(storagetaskAction == Action.write || storagetaskAction == Action.verify) {
			List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassId(artifactclassId);
			for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
				if(artifactclassVolume.isActive()) {
					Volume volume = artifactclassVolume.getVolume();
					Job job = new Job();
					job.setStoragetaskActionId(storagetaskAction);
					job.setInputArtifactId(artifactId);
					job.setFlowelement(nthFlowelement);
					job.setRequest(request);				
					job.setCreatedAt(LocalDateTime.now());
					job.setStatus(Status.queued);
					job.setGroupVolume(volume); // we dont know the physical volume yet... How about provisioned volumes?

					// saving all the pre requisite jobs needed for this job...
					List<Integer> dependentJobIds = new ArrayList<Integer>();
					if(preRequesiteFlowelements != null) {
						for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
							Job nthPreRequesiteJob = null;
							Flowelement nthPreRequesiteFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get(); // TODO Cache it
							if(nthPreRequesiteFlowelement.getStoragetaskActionId() == Action.write)
								nthPreRequesiteJob = flowelementId_CopyNumber_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelement.getId() + "_" + volume.getCopy().getId());
							else
								nthPreRequesiteJob = flowelementId_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelement.getId());


							dependentJobIds.add(nthPreRequesiteJob.getId());
						}
					}
					if(dependentJobIds.size() > 0)
						job.setDependencies(dependentJobIds);
					job = saveJob(job);
					jobList.add(job);

					flowelementId_CopyNumber_Job_Map.put(putKeyPrefix + nthFlowelement.getId() + "_" + volume.getCopy().getId(), job);
					flowelementId_Job_Map.put(putKeyPrefix + nthFlowelement.getId(), job);		
				}
			}
		}
		else {
			String processingtaskId = nthFlowelement.getProcessingtaskId();

			Job job = new Job();
			if(storagetaskAction != null)
				job.setStoragetaskActionId(storagetaskAction);
			if(processingtaskId != null)
				job.setProcessingtaskId(processingtaskId);


			if(storagetaskAction != null || (processingtaskId != null && preRequesiteFlowelements == null)) {
				// If a processing task contains prerequisite task that means its a derived one, for which the input library id needs to set by the prerequisite/parent task's job at the time of its processing and not at the time of job creation...
				// for eg., Mezz copy wont have a input library id upfront, which will be generated by its prerequisite/parent Mezz Transcoding job...
				job.setInputArtifactId(artifactId);
			}
			job.setFlowelement(nthFlowelement);
			job.setRequest(request);				
			job.setCreatedAt(LocalDateTime.now());
			job.setStatus(Status.queued);
			List<Integer> dependentJobIds = new ArrayList<Integer>();
			if(preRequesiteFlowelements != null) {
				for (Integer nthPreRequesiteFlowelementId : preRequesiteFlowelements) {
					Job nthPreRequesiteJob = null;
					//					Flowelement nthPreRequesiteFlowelement = flowelementDao.findById(nthPreRequesiteFlowelementId).get(); // TODO Cache it
					//					nthPreRequesiteJob = flowelementId_Job_Map.get(nthPreRequesiteFlowelement.getId());
					nthPreRequesiteJob = flowelementId_Job_Map.get(getKeyPrefix + nthPreRequesiteFlowelementId);
					dependentJobIds.add(nthPreRequesiteJob.getId());
				}
				job.setDependencies(dependentJobIds);
			}
			
			job = saveJob(job);
			jobList.add(job);
			flowelementId_Job_Map.put(putKeyPrefix + nthFlowelement.getId(), job);
		}
	}
	
	private Job saveJob(Job job) {
		//logger.debug("DB Job row Creation");   
		job = jobDao.save(job);
		logger.info(DwaraConstants.JOB + job.getId());
		return job;
	}
}

