package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.StatusDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusCacheUtil {
	@Autowired
	private StatusDao statusDao;
	
	private List<Status> statusList = null;
	private Map<Integer, Status> statusId_status_Map = new HashMap<Integer, Status>();
	private Map<String, Status> statusName_status_Map = new HashMap<String, Status>();

	@PostConstruct
	private void loadStatusList() {
    	statusList = (List<Status>) statusDao.findAll();
		for (Status status : statusList) {
			statusId_status_Map.put(status.getId(), status);
			statusName_status_Map.put(status.getName(), status);
		}
	}
	
	public List<Status> getStatusList(){
		return statusList;
	}
    
	public Status getStatus(int statusId) {
    	return statusId_status_Map.get(statusId);
    }
    
    public Status getStatus(String statusName) {
    	return statusName_status_Map.get(statusName);
    }
    
    public Set<Integer> getAllStatusIds() {
    	return statusId_status_Map.keySet();
    }	
}
