package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Extension;
import org.springframework.data.repository.CrudRepository;

public interface ExtensionDao extends CrudRepository<Extension, Integer> {
	
	Extension findTopByOrderByIdDesc();
	
	/* will frame a query like 
		select * from extension 
			join extension_filetype on extension.id = extension_filetype.extension_id 
			join filetype on extension_filetype.filetype_id = filetype.id 
			where filetype.id = 4001;
	*/
	List<Extension> findAllByTaskfiletypesTaskfiletypeId(int taskfiletypeId);
	
	/*
	 * we might also need sidecar along with the list of extensions. 
	 * 
	 * select *,extension_filetype.sidecar from extension 
			join extension_filetype on extension.id = extension_filetype.extension_id 
			join filetype on extension_filetype.filetype_id = filetype.id 
			where filetype.id = 4001;
	 *
	 * TODO How to achieve this using JPA methods and what would be the response object list like?
	 * 
	 * Alternative is we use the join table directly and get the sidecar info as we already know the composite key's values involved...
	 *
	 */
}
