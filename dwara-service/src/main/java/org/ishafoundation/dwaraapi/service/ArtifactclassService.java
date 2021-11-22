package org.ishafoundation.dwaraapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.artifactclass.ArtifactclassResponse;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArtifactclassService {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactclassService.class);
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	public List<ArtifactclassResponse> getAllArtifactClasses(){
		/*
		 * TODO Action element - breaking change
		 * 
		// *** Caching actionelement and actionelementmaps start ***
		
		// artifactclass, actions, actionelements
		// pub-video , ingest , [1,2,3]
		// pub-video , process , [11,12,13]
		Map<String, Map<String, List<Actionelement>>> artifactclass_Action_ActionelementObj_Map = new HashMap<String, Map<String, List<Actionelement>>>();
		
		Iterable<Actionelement> actionelementList = actionelementDao.findAll();
		for (Actionelement actionelement : actionelementList) {
			String artifactclassId = actionelement.getArtifactclassId();
			// actions, actionelements
			// ingest , [1,2,3]
			// process , [11,12,13]
			Map<String, List<Actionelement>> artifactclassSpecificAction_ActionelementObj_Map = artifactclass_Action_ActionelementObj_Map.get(artifactclassId);
			
			Action action = actionelement.getComplexActionId();
			if(artifactclassSpecificAction_ActionelementObj_Map == null) {
				// actionelements
				// [11,12,13]
				List<Actionelement> artifactclassSpecificAction_ActionelementList = new ArrayList<Actionelement>();
				artifactclassSpecificAction_ActionelementObj_Map = new HashMap<String, List<Actionelement>>();
				artifactclassSpecificAction_ActionelementObj_Map.put(action.name(), artifactclassSpecificAction_ActionelementList);
				
				artifactclass_Action_ActionelementObj_Map.put(artifactclassId, artifactclassSpecificAction_ActionelementObj_Map);
			}
			// actionelements - [11,12,13]
			List<Actionelement> artifactclassSpecificAction_ActionelementList = artifactclassSpecificAction_ActionelementObj_Map.get(action.name());
			artifactclassSpecificAction_ActionelementList.add(actionelement);
		}
		
		Map<Integer, List<Integer>> actionelement_PrerequisiteActionelements = new HashMap<Integer, List<Integer>>();
		Iterable<ActionelementMap> actionelementMapList = actionelementMapDao.findAll();
		for (ActionelementMap actionelementMap : actionelementMapList) {
			Integer dependentActionelement = actionelementMap.getId().getActionelementId();
			List<Integer> prerequesiteActionelements = actionelement_PrerequisiteActionelements.get(dependentActionelement);
			if(prerequesiteActionelements == null) {
				prerequesiteActionelements = new ArrayList<Integer>();
				actionelement_PrerequisiteActionelements.put(dependentActionelement, prerequesiteActionelements);
			}
			prerequesiteActionelements.add(actionelementMap.getId().getActionelementRefId());
		}
		
		// *** Caching actionelement and actionelementmaps end ***
		*/
		List<ArtifactclassResponse> artifactclassResponseList = new ArrayList<ArtifactclassResponse>();
		List<Artifactclass> artifactclassList = configurationTablesUtil.getAllArtifactclasses();
		
		// ordering the artifactclass by displayorder
		Collections.sort(artifactclassList);

//		Map<Integer, Artifactclass> displayOrder_Artifactclass = new HashMap<Integer, Artifactclass>();
//		// ordering the artifactclass by displayorder
//		for (Artifactclass artifactclass : artifactclassList) {
//			displayOrder_Artifactclass.put(artifactclass.getDisplayOrder(), artifactclass);
//		}
//		Set<Integer> displayOrderedArtifactclassList = displayOrder_Artifactclass.keySet();
//		List<Integer> list = new ArrayList<String>(displayOrderedArtifactclassList); 
//        Collections.sort(list);
//		
//		for (Integer integer : displayOrderedArtifactclassList) {
//			
//		}
//		
		
		for (Artifactclass artifactclass : artifactclassList) {
			ArtifactclassResponse artifactclassResponse = new ArtifactclassResponse();
			String artifactclassId = artifactclass.getId();
			artifactclassResponse.setId(artifactclassId);
			// TODO - Breaking change...
			artifactclassResponse.setName(artifactclassId);// artifactclassResponse.setName(artifactclass.getName());
//			if(artifactclass.getDomain() != null)
//				artifactclassResponse.setDomain(Integer.parseInt(domainAttributeConverter.convertToDatabaseColumn(artifactclass.getDomain()))); // FIXME - Domain - Parsing as Integer
			artifactclassResponse.setSource(artifactclass.isSource());
			artifactclassResponse.setDisplayOrder(artifactclass.getDisplayOrder());
			
			/* TODO Action element - breaking change
			Map<String, List<Actionelement>> artifactclassSpecificAction_ActionelementObj_Map = artifactclass_Action_ActionelementObj_Map.get(artifactclassId);
			if(artifactclassSpecificAction_ActionelementObj_Map != null) {
				Set<String> actions = artifactclassSpecificAction_ActionelementObj_Map.keySet();
				List<ComplexAction> complexActions = new ArrayList<ComplexAction>();
				for (String action : actions) {
					ComplexAction complexAction = new ComplexAction();
					complexAction.setAction(action);
					List<org.ishafoundation.dwaraapi.api.resp.artifactclass.Actionelement> actionelementReponseList = new ArrayList<org.ishafoundation.dwaraapi.api.resp.artifactclass.Actionelement>();
					List<Actionelement> artifactclassSpecificAction_ActionelementList = artifactclassSpecificAction_ActionelementObj_Map.get(action);
					for(Actionelement actionelement : artifactclassSpecificAction_ActionelementList) {
						org.ishafoundation.dwaraapi.api.resp.artifactclass.Actionelement actionelementReponse = new org.ishafoundation.dwaraapi.api.resp.artifactclass.Actionelement();
						int actionelementId = actionelement.getId();
						actionelementReponse.setId(actionelementId);
						actionelementReponse.setActive(actionelement.isActive());
						Action storagetask = actionelement.getStoragetaskActionId();
						if(storagetask != null) {
							actionelementReponse.setStoragetaskAction(storagetask.name());
							//actionelementReponse.setVolume(actionelement.getVolumeId());
						}else {
							actionelementReponse.setProcessingTask(actionelement.getProcessingtaskId());
						}
						
						actionelementReponse.setPrerequisites(actionelement_PrerequisiteActionelements.get(actionelementId));
						actionelementReponseList.add(actionelementReponse);
					}
					complexAction.setElements(actionelementReponseList);
					complexActions.add(complexAction);
				}
				artifactclassResponse.setComplexActions(complexActions);
			}
			*/
			artifactclassResponseList.add(artifactclassResponse);
		}
		return artifactclassResponseList;
	}
	
}

