package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.springframework.data.repository.CrudRepository;

public interface LibraryclassDao extends CrudRepository<Libraryclass,Integer> {
	
	Libraryclass findByName(String name);
	
	List<Libraryclass> findAllByLibraryclassTargetvolumeTargetvolumeId(int targetvolumeId);
	
	Libraryclass findByGeneratorTaskId(int generatorTaskId);

	//Libraryclass findByTaskId(int generatorTaskId);
}