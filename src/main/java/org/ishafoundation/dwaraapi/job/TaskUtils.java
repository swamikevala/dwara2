package org.ishafoundation.dwaraapi.job;

import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskUtils {
	
	public boolean isTaskStorage(Task task) {
		boolean isTaskStorage = true;
		if(task.getTaskfiletype() != null)
			isTaskStorage = false;
		
		return isTaskStorage;
	}
//	
//	@Autowired
//	private RequesttypeLibraryclassDao requesttypeLibraryclassDao;
//
//	@Autowired
//	private RequesttypeDao requesttypeDao;
//
//
//	public TaskOrTasksetDetails getTaskOrTasksetDetails(int requesttypeId, int libraryclassId) {
//		int tasksetId = 0;
//		int taskId = 0;
//		RequesttypeLibraryclass requesttypeLibraryclass = requesttypeLibraryclassDao.findByRequesttypeIdAndLibraryclassId(requesttypeId, libraryclassId);
//		if(requesttypeLibraryclass != null) { // means library class specific workflow
//			tasksetId = requesttypeLibraryclass.getTasksetId();
//			taskId = requesttypeLibraryclass.getTaskId();
//		}else { // means default ingest workflow or restore
//			Requesttype requesttype = requesttypeDao.findById(requesttypeId).get();
//			tasksetId = requesttype.getTasksetId();
//			taskId = requesttype.getTaskId();
//		}
//		
//		TaskOrTasksetDetails taskOrTasksetDetails = new TaskOrTasksetDetails();
//		taskOrTasksetDetails.setTasksetId(tasksetId);
//		taskOrTasksetDetails.setTaskId(taskId);
//		return taskOrTasksetDetails; // if both tasksetId and taskId are 0 it means no job need to be created... 
//	}
}
