package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.FlowelementDao;
import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.CoreFlowelement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowelementUtil {

	@Autowired
	private FlowelementDao flowelementDao;

	public List<Flowelement> getAllFlowElements(String flowId) {
		List<Flowelement> flowelementList = null;
		
		if(flowId.startsWith("core"))
			flowelementList = getCoreFlowElements(flowId);
		else
			flowelementList = flowelementDao.findAllByFlowIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc(flowId);
		return flowelementList;
	}

	private List<Flowelement> getCoreFlowElements(String flowId) {
		List<Flowelement> flowelementList = new ArrayList<Flowelement>();
		
		List<CoreFlowelement> coreFlowelementList = CoreFlowelement.findAllByFlowId(flowId);
		for (CoreFlowelement nthCoreFlowelement : coreFlowelementList) {
			Flowelement flowElement = new Flowelement();
			flowElement.setActive(nthCoreFlowelement.isActive());
			if(nthCoreFlowelement.getDependencies() != null)
				flowElement.setDependencies(Arrays.asList(nthCoreFlowelement.getDependencies()));
			
			flowElement.setDeprecated(nthCoreFlowelement.isDeprecated());
			flowElement.setDisplayOrder(nthCoreFlowelement.getDisplayOrder());
			flowElement.setFlowId(nthCoreFlowelement.getFlowId());
			flowElement.setFlowRefId(nthCoreFlowelement.getFlowRefId());
			flowElement.setId(nthCoreFlowelement.getId());
			if(nthCoreFlowelement.getStoragetaskActionId() != null)
				flowElement.setStoragetaskActionId(Action.valueOf(nthCoreFlowelement.getStoragetaskActionId()));
			else
				flowElement.setProcessingtaskId(nthCoreFlowelement.getProcessingtaskId());
			
			flowelementList.add(flowElement);
		}
		
//		if(flowId.equals("core-archive-flow")) {
//			
//			// TODO move this as a enum 
//			flowelementList = 
//			
//			Flowelement checksumGenFlowElement = new Flowelement();
//			checksumGenFlowElement.setActive(true);
//			checksumGenFlowElement.setDependencies(null);
//			checksumGenFlowElement.setDeprecated(false);
//			checksumGenFlowElement.setDisplayOrder(2100000000);
//			checksumGenFlowElement.setFlowId(flowId);
//			checksumGenFlowElement.setFlowRefId(null);
//			checksumGenFlowElement.setId(2100000000);
//			checksumGenFlowElement.setProcessingtaskId("checksum-gen");
//
//			flowelementList.add(checksumGenFlowElement);
//			
//			Flowelement writeFlowElement = new Flowelement();
//			writeFlowElement.setActive(true);
//			writeFlowElement.setDependencies(null);
//			writeFlowElement.setDeprecated(false);
//			writeFlowElement.setDisplayOrder(2100000001);
//			writeFlowElement.setFlowId(flowId);
//			writeFlowElement.setFlowRefId(null);
//			writeFlowElement.setId(2100000001);
//			writeFlowElement.setStoragetaskActionId(Action.write);
//			
//			flowelementList.add(writeFlowElement);
//			
//			Flowelement restoreFlowElement = new Flowelement();
//			restoreFlowElement.setActive(true);
//			List<Integer> restoreDependencies = new ArrayList<Integer>();
//			restoreDependencies.add(2100000001);
//			restoreFlowElement.setDependencies(restoreDependencies);
//			restoreFlowElement.setDeprecated(false);
//			restoreFlowElement.setDisplayOrder(2100000002);
//			restoreFlowElement.setFlowId(flowId);
//			restoreFlowElement.setFlowRefId(null);
//			restoreFlowElement.setId(2100000002);
//			restoreFlowElement.setStoragetaskActionId(Action.restore);
//			
//			flowelementList.add(restoreFlowElement);
//			
//			Flowelement checksumVerifyFlowElement = new Flowelement();
//			checksumVerifyFlowElement.setActive(true);
//			List<Integer> checksumVerifyDependencies = new ArrayList<Integer>();
//			checksumVerifyDependencies.add(2100000001);
//			checksumVerifyDependencies.add(2100000003);
//			checksumVerifyFlowElement.setDependencies(checksumVerifyDependencies);
//			checksumVerifyFlowElement.setDeprecated(false);
//			checksumVerifyFlowElement.setDisplayOrder(2100000004);
//			checksumVerifyFlowElement.setFlowId(flowId);
//			checksumVerifyFlowElement.setFlowRefId(null);
//			checksumVerifyFlowElement.setId(2100000004);
//			checksumVerifyFlowElement.setProcessingtaskId("checksum-verify");
//			
//			flowelementList.add(checksumVerifyFlowElement);
//		}
		return flowelementList;
	}


}
