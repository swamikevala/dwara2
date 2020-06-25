package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.jointables.Actionelement;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.springframework.data.repository.CrudRepository;

public interface ActionelementDao extends CrudRepository<Actionelement,Integer> {
	
	
	
//	Actionelement findByActionAndTaskIdAndTasktypeAndArtifactclassIdAndVolumesetId(Action action, int taskId, Tasktype tasktype, int artifactclassId, Integer volumesetId);
	
	//Actionelement findByTaskIdAndTasktypeAndInputArtifactclassIdAndVolumesetId(int taskId, Tasktype tasktype, int artifactclassId, int volumesetId);
	
	List<Actionelement> findAllByComplexActionAndArtifactclassIdOrderByDisplayOrderAsc(Action action, int artifactclassId);
	
	//List<Actionelement> findAllByInputArtifactclassIdOrderByDisplayOrderAsc(int artifactclassId);
	
	//List<Actionelement> findAllByTasktypeAndArtifactclassIdOrderByDisplayOrderAsc(Tasktype tasktype, int artifactclassId);
	
//	List<Actionelement> findAllByArtifactclassIdAndPreTasktypeAndPreTaskIdAndOrderByDisplayOrderAsc(int artifactclassId, Tasktype tasktype, Integer preProcessingTaskId);

}