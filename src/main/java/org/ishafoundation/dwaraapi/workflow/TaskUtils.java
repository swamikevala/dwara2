package org.ishafoundation.dwaraapi.workflow;

import org.ishafoundation.dwaraapi.db.dao.master.common.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.RequesttypeLibraryclassDao;
import org.ishafoundation.dwaraapi.db.model.master.common.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.ingest.RequesttypeLibraryclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.model.TaskOrTasksetDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskUtils {
	
	@Autowired
	private RequesttypeLibraryclassDao requesttypeLibraryclassDao;

	@Autowired
	private RequesttypeDao requesttypeDao;


	public TaskOrTasksetDetails getTaskOrTasksetDetails(Request request) {
		int requesttypeId = request.getRequesttypeId();
		int libraryclassId = request.getLibraryclassId();
		
		int tasksetId = 0;
		int taskId = 0;
		RequesttypeLibraryclass requesttypeLibraryclass = requesttypeLibraryclassDao.findByRequesttypeIdAndLibraryclassId(requesttypeId, libraryclassId);
		if(requesttypeLibraryclass != null) { // means library class specific workflow
			tasksetId = requesttypeLibraryclass.getTasksetId();
			taskId = requesttypeLibraryclass.getTaskId();
		}else { // means default ingest workflow or restore
			Requesttype requesttype = requesttypeDao.findById(requesttypeId).get();
			tasksetId = requesttype.getTasksetId();
			taskId = requesttype.getTaskId();
		}
		
		TaskOrTasksetDetails taskOrTasksetDetails = new TaskOrTasksetDetails();
		taskOrTasksetDetails.setTasksetId(tasksetId);
		taskOrTasksetDetails.setTaskId(taskId);
		return taskOrTasksetDetails; // if both tasksetId and taskId are 0 it means no job need to be created... 
	}
}
