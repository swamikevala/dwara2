package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.jointables.Flowelement;
import org.springframework.data.repository.CrudRepository;

public interface FlowelementDao extends CrudRepository<Flowelement,Integer> {
	
	List<Flowelement> findAllByFlowIdAndDeprecatedFalseAndActiveTrueOrderByDisplayOrderAsc(String flowId);
	
}