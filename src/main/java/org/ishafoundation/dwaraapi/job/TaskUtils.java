package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryclassTapesetDao;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskUtils {
	
	@Autowired
	private LibraryclassTapesetDao libraryclassTapesetDao;
	
	public boolean isTaskStorageV2(Task task) {
		
		LibraryclassTapeset libraryclassTapeset = libraryclassTapesetDao.findByTaskId(task.getId());

		boolean isTaskStorage = false;
		if (libraryclassTapeset != null)  // means its a copy job...
			isTaskStorage = true;
		
		return isTaskStorage;
	}
	
	public boolean isTaskStorageV1(Task task) {

		boolean isTaskStorage = true;		if(task.getTaskfiletype() != null)

			isTaskStorage = false;
		
		return isTaskStorage;
	}
	
	public boolean isTaskStorage(Task task) {
		return task.getCopyNumber() != null ? true : false;
	}
//	
//	@Autowired
//	private ActionLibraryclassDao actionLibraryclassDao;
//
//	@Autowired
//	private ActionDao actionDao;
//
//
//	public TaskOrTasksetDetails getTaskOrTasksetDetails(int actionId, int libraryclassId) {
//		int tasksetId = 0;
//		int taskId = 0;
//		ActionLibraryclass actionLibraryclass = actionLibraryclassDao.findByActionIdAndLibraryclassId(actionId, libraryclassId);
//		if(actionLibraryclass != null) { // means library class specific workflow
//			tasksetId = actionLibraryclass.getTasksetId();
//			taskId = actionLibraryclass.getTaskId();
//		}else { // means default ingest workflow or restore
//			Action action = actionDao.findById(actionId).get();
//			tasksetId = action.getTasksetId();
//			taskId = action.getTaskId();
//		}
//		
//		TaskOrTasksetDetails taskOrTasksetDetails = new TaskOrTasksetDetails();
//		taskOrTasksetDetails.setTasksetId(tasksetId);
//		taskOrTasksetDetails.setTaskId(taskId);
//		return taskOrTasksetDetails; // if both tasksetId and taskId are 0 it means no job need to be created... 
//	}
}
