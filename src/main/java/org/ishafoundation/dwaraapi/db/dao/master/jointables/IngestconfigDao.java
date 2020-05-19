package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.jointables.Ingestconfig;
import org.ishafoundation.dwaraapi.enumreferences.Tasktype;
import org.springframework.data.repository.CrudRepository;

public interface IngestconfigDao extends CrudRepository<Ingestconfig,Integer> {
	
	Ingestconfig findByTaskIdAndTasktypeAndInputLibraryclassIdAndTapesetId(int taskId, Tasktype tasktype, int libraryclassId, int tapesetId);
	
	List<Ingestconfig> findAllByInputLibraryclassIdOrderByDisplayOrderAsc(int libraryclassId);
	
	//List<Ingestconfig> findAllByTasktypeAndLibraryclassIdOrderByDisplayOrderAsc(Tasktype tasktype, int libraryclassId);
	
	List<Ingestconfig> findAllByPreProcessingTaskIdAndInputLibraryclassIdOrderByDisplayOrderAsc(Integer preProcessingTaskId, int libraryclassId);

}