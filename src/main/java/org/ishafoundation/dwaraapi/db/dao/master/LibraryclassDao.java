package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassDao extends CrudRepository<Libraryclass,Integer> {
	
	//Libraryclass findByTaskId(int taskId);
	
	List<Libraryclass> findAllByLibraryclassTargetvolumeTargetvolumeId(int targetvolumeId);

}