package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;

public interface ExtensionDao extends CacheableRepository<Extension> {
	
	// get all the extensions which are neither flagged as ignore nor attached to filetypes
	Iterable<Extension> findAllByIgnoreIsNullAndFiletypesIsNull();

	// get only the extensions which are either flagged as ignore or attached to a filetype
	Iterable<Extension> findAllByIgnoreIsTrueOrFiletypesIsNotNull();
}
