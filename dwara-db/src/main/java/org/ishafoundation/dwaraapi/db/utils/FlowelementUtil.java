package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

	public Flowelement findById(String flowelementId) {
		Flowelement flowelement = null;
		CoreFlowelement coreFlowelement = CoreFlowelement.findById(flowelementId);
		
		if(coreFlowelement != null)
			flowelement = getFlowelement(coreFlowelement);
		else {		
			Optional<Flowelement> optFlowelement = flowelementDao.findById(flowelementId);
			if(optFlowelement.isPresent())
				flowelement = optFlowelement.get();
		}
		return flowelement;
	}
	
	public List<Flowelement> getAllFlowElements(String flowId) {
		List<Flowelement> flowelementList = getCoreFlowelements(flowId);
		
		if(flowelementList.size() == 0)
			flowelementList = flowelementDao.findAllByFlowIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc(flowId);

		return flowelementList;
	}

	private Flowelement getFlowelement(CoreFlowelement nthCoreFlowelement) {
		Flowelement flowelement = new Flowelement();
		flowelement.setActive(nthCoreFlowelement.isActive());
		if(nthCoreFlowelement.getDependencies() != null)
			flowelement.setDependencies(Arrays.asList(nthCoreFlowelement.getDependencies()));
		
		flowelement.setDeprecated(nthCoreFlowelement.isDeprecated());
		flowelement.setDisplayOrder(nthCoreFlowelement.getDisplayOrder());
		flowelement.setFlowId(nthCoreFlowelement.getFlowId());
		flowelement.setFlowRefId(nthCoreFlowelement.getFlowRefId());
		flowelement.setId(nthCoreFlowelement.getId());
		if(nthCoreFlowelement.getStoragetaskActionId() != null)
			flowelement.setStoragetaskActionId(Action.valueOf(nthCoreFlowelement.getStoragetaskActionId()));
		else
			flowelement.setProcessingtaskId(nthCoreFlowelement.getProcessingtaskId());
		
		return flowelement;
	}
	
	private List<Flowelement> getCoreFlowelements(String flowId) {
		List<Flowelement> flowelementList = new ArrayList<Flowelement>();
		
		List<CoreFlowelement> coreFlowelementList = CoreFlowelement.findAllByFlowId(flowId);
		for (CoreFlowelement nthCoreFlowelement : coreFlowelementList) {
			Flowelement flowelement = getFlowelement(nthCoreFlowelement);
			flowelementList.add(flowelement);
		}
		
		return flowelementList;
	}
}
