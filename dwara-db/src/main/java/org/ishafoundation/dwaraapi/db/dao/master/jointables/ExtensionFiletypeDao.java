package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.keys.ExtensionFiletypeKey;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;
import org.springframework.data.repository.CrudRepository;

public interface ExtensionFiletypeDao extends CrudRepository<ExtensionFiletype,ExtensionFiletypeKey> {
	
	List<ExtensionFiletype> findAllByFiletypeId(int filetypeId);
}
