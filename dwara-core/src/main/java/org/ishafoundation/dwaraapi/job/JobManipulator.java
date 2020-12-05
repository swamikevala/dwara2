package org.ishafoundation.dwaraapi.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ActionArtifactclassFlowDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassVolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
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
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Actiontype;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.ishafoundation.dwaraapi.storage.storagetask.AbstractStoragetaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Used by Job API 
 *
 */
@Component
public class JobManipulator {

	private static final Logger logger = LoggerFactory.getLogger(JobManipulator.class);

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

	@Autowired
	private DomainUtil domainUtil;
	
	public List<Job> getJobs(Request request){
		Action requestedBusinessAction = request.getActionId();
		org.ishafoundation.dwaraapi.db.model.master.reference.Action action = configurationTablesUtil.getAction(requestedBusinessAction);

		List<Job> alreadyCreatedJobList = jobDao.findAllByRequestId(request.getId());
		
		List<Job> jobList = new ArrayList<Job>();

		if(Actiontype.complex == action.getType()) {
			logger.trace("Complex action");
			String sourceArtifactclassId = request.getDetails().getArtifactclassId();
			// get all the flows for the action on the artifactclass - Some could be global across artifactclasses and some specific to that artifactclass. so using "_all_" for global
			List<ActionArtifactclassFlow> actionArtifactclassFlowList = actionArtifactclassFlowDao.findAllByArtifactclassIdOrArtifactclassIdAndActionIdAndActiveTrue("_all_", sourceArtifactclassId, requestedBusinessAction.name()); //
			for (ActionArtifactclassFlow actionArtifactclassFlow : actionArtifactclassFlowList) {
				String nthFlowId = actionArtifactclassFlow.getFlow().getId();
				logger.trace("flow " + nthFlowId);
				iterateFlow(request, sourceArtifactclassId, nthFlowId, null, alreadyCreatedJobList, jobList);
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

	/**
	 * 
	 * @param request - 
	 * @param artifactclassId - the artifactclass relevant to the flow 
	 * @param nthFlowId -
	 * @param referencingFlowelement - The flowelement that refers the above flow - It is neither a storage task nor a processing task but a references another flow
	 * @param alreadyCreatedJobList - The jobs that are already in DB
	 * @param jobList - the joblist for response...
	 */
	private void iterateFlow(Request request, String artifactclassId, String nthFlowId, Flowelement referencingFlowelement, List<Job> alreadyCreatedJobList, List<Job> jobList) {
		logger.trace("Iterating flow " + nthFlowId);

		List<Artifact> artifactList = null;
		Domain[] domains = Domain.values();
		for (Domain nthDomain : domains) {
			ArtifactRepository<Artifact> artifactRepository = domainUtil.getDomainSpecificArtifactRepository(nthDomain);

			artifactList = artifactRepository.findAllByWriteRequestId(request.getId());
			
			if(artifactList != null && artifactList.size() > 0) {
				break;
			}
		}
		
		//  get all the flow elements for the flow
		List<Flowelement> flowelementList = flowelementDao.findAllByFlowIdAndActiveTrueOrderByDisplayOrderAsc(nthFlowId);
		for (Flowelement nthFlowelement : flowelementList) {
			int nthFlowelementId = nthFlowelement.getId();
			logger.trace("Flowelement " + nthFlowelementId);

			List<Integer> flowelementDepsList = nthFlowelement.getDependencies();

			String outputArtifactclassId = null;
			boolean processingtaskWithDependencyStoragetask = false;
			if(flowelementDepsList != null) {
				String outputArtifactclassSuffix = null;
				
				for (Integer nthFlowelementDependencyId : flowelementDepsList) {
					Flowelement prereqFlowelement = flowelementDao.findById(nthFlowelementDependencyId).get();
					
					Action storagetaskDependency = prereqFlowelement.getStoragetaskActionId();  
					if(storagetaskDependency != null) // Is the dependency a Storage task?
						processingtaskWithDependencyStoragetask = true;
					else { // Is the dependency a processing task?
						if(outputArtifactclassSuffix != null) // if outputArtifactclassSuffix is already set 
							continue; // Dont break it, as we need to loop through the entire dependencies to see if any processingtaskWithDependencyStoragetask
						else { // Now use a processing task that generates an output [NOTE ONLY ONE PROCESSING TASK CAN GENERATE AN OUTPUT ARTIFACT IN A DEPENDENCY LIST - TODO: REASON OUT WHY?]
							String processingtaskId = prereqFlowelement.getProcessingtaskId();
		
							logger.trace("Dependency processing task is " + processingtaskId);
							Processingtask processingtask = null;
							Optional<Processingtask> processingtaskOpt = processingtaskDao.findById(processingtaskId);
							if(processingtaskOpt.isPresent())
								processingtask = processingtaskOpt.get();
							
							outputArtifactclassSuffix = processingtask != null ? processingtask.getOutputArtifactclassSuffix() : null; // Does the dependent processing task generate an output?
						}
					}
				}
				if(outputArtifactclassSuffix != null)
					outputArtifactclassId = artifactclassId + outputArtifactclassSuffix;
				else
					outputArtifactclassId = artifactclassId;
			}
			
			Flow referredFlow = nthFlowelement.getFlowRef(); 
			if(referredFlow != null) { // If the flowelement references a flow(has a flowRef), that means one of this flowelement dependency is a processing task generating an output artifact, that is to be consumed as input

				logger.trace("Output ArtifactclassId " + outputArtifactclassId);
				iterateFlow(request, outputArtifactclassId, referredFlow.getId(), nthFlowelement, alreadyCreatedJobList, jobList);
			} else {
				
				Action storagetaskAction = nthFlowelement.getStoragetaskActionId();
				String processingtaskId = nthFlowelement.getProcessingtaskId();
				
				String artifactclassIdToBeUsed = artifactclassId;
				String referencingFlowPrefix = "";
				List<Integer> dependencies = nthFlowelement.getDependencies();
				if(referencingFlowelement != null) {
					referencingFlowPrefix = referencingFlowelement.getId() + "_";
					if(dependencies == null) {
						dependencies = referencingFlowelement.getDependencies();
					}
				} 
				
				if(outputArtifactclassId != null){
					artifactclassIdToBeUsed = outputArtifactclassId;
				}
				
				Artifact artifact = null;
				for (Artifact nthArtifact : artifactList) {
					if(nthArtifact.getArtifactclass().getId().equals(artifactclassIdToBeUsed)) {
						artifact = nthArtifact;
						break;
					}
				}
				Integer artifactId = (artifact != null ? artifact.getId() : null);
				List<String> uIdDependencies = null; 
				if(storagetaskAction != null || processingtaskWithDependencyStoragetask) {
					
					List<ArtifactclassVolume> artifactclassVolumeList = artifactclassVolumeDao.findAllByArtifactclassIdAndActiveTrue(artifactclassId);
					logger.trace("No. of copies " + artifactclassVolumeList.size());
					for (ArtifactclassVolume artifactclassVolume : artifactclassVolumeList) {
						Volume volume = artifactclassVolume.getVolume();
						String uId = referencingFlowPrefix + nthFlowelementId + "_" + volume.getCopy().getId();

						if(dependencies != null) {
							uIdDependencies = new ArrayList<String>();
							for (Integer dependentFlowelementId : dependencies) {
								Flowelement dependentFlowelement = flowelementDao.findById(dependentFlowelementId).get();

								String copyId = "";
								if(dependentFlowelement.getStoragetaskActionId() != null)
									copyId = "_" + volume.getCopy().getId();

								if(nthFlowelement.getDependencies() == null)
									uIdDependencies.add(dependentFlowelementId + copyId);
								else
									uIdDependencies.add(referencingFlowPrefix + dependentFlowelementId + copyId);
							}
						}
						
						logger.trace("uId : " + uId);
						// check if job already created and details available... 
						Job job = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifactId, nthFlowelementId, volume.getId());
						if(job == null) {
							job = new Job();
							job.setInputArtifactId(artifactId);
							job.setFlowelement(nthFlowelement);
							job.setRequest(request);	
							if(storagetaskAction != null) {
								job.setStoragetaskActionId(storagetaskAction);
							}else
								job.setProcessingtaskId(processingtaskId);
							job.setGroupVolume(volume);
						}
						job.setuId(uId);
						job.setuIdDependencies(uIdDependencies);
						//flowelementUid_Job_Map.put(uId, job);
						jobList.add(job);

					}
				}else {
					String uId = referencingFlowPrefix + nthFlowelementId;
					logger.trace("uid - " + uId);
					// check if job already created and details available... 
					Job job = jobDao.findByRequestIdAndInputArtifactIdAndFlowelementIdAndGroupVolumeId(request.getId(), artifactId, nthFlowelementId, null);
					if(job == null) {
						job = new Job();
						job.setInputArtifactId(artifactId);
						job.setFlowelement(nthFlowelement);
						job.setRequest(request);	
						job.setProcessingtaskId(processingtaskId);
					}
						
					if(dependencies != null) {
						uIdDependencies = new ArrayList<String>();
						for (Integer dependentFlowelementId : dependencies) {
							Flowelement dependentFlowelement = flowelementDao.findById(dependentFlowelementId).get();

							String copyId = "";
							if(dependentFlowelement.getStoragetaskActionId() != null)
								copyId = ""; // TODO : We dont know how to support this just yet...  "_" + volume.getCopy().getId();

							if(nthFlowelement.getDependencies() == null)
								uIdDependencies.add(dependentFlowelementId + copyId);
							else
								uIdDependencies.add(referencingFlowPrefix + dependentFlowelementId + copyId);
						}
					}

					job.setuId(uId);
					job.setuIdDependencies(uIdDependencies);
//					flowelementUid_Job_Map.put(uId, job);
					jobList.add(job);
				}
			}
		}
	}
}

