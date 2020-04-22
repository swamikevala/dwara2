package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ExtensionTaskfiletypeKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionTaskfiletype;
import org.springframework.data.repository.CrudRepository;

public interface ExtensionTaskfiletypeDao extends CrudRepository<ExtensionTaskfiletype,ExtensionTaskfiletypeKey> {
	
	List<ExtensionTaskfiletype> findAllByTaskfiletypeId(int taskfiletypeId);
}