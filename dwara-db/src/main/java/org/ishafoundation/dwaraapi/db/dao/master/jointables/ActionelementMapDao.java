package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ActionelementMapKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionelementMap;
import org.springframework.data.repository.CrudRepository;

public interface ActionelementMapDao extends CrudRepository<ActionelementMap,ActionelementMapKey> {
	// gets all prerequisite actionelements
	List<ActionelementMap> findAllByIdActionelementId(int actionelementId);


	// gets all dependent actionelements
	//List<ActionelementMap> findAllByIdActionelementRefId(int actionelementRefId);
	


}