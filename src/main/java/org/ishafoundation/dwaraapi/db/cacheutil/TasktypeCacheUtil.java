package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.TasktypeDao;
import org.ishafoundation.dwaraapi.db.model.master.Tasktype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TasktypeCacheUtil {
	@Autowired
	private TasktypeDao tasktypeDao;
	
	private List<Tasktype> tasktypeList = null;
	private Map<Integer, Tasktype> tasktypeMap = new HashMap<Integer, Tasktype>(); 

	@PostConstruct
	private void loadTasktypeList() {
    	tasktypeList = (List<Tasktype>) tasktypeDao.findAll();
		for (Tasktype tasktype : tasktypeList) {
			tasktypeMap.put(tasktype.getId(), tasktype);
		}
	}
	
	public List<Tasktype> getTasktypeList(){
		return tasktypeList;
	}
	
    public Tasktype getTasktype(int tasktypeId) {
    	return tasktypeMap.get(tasktypeId);
    }
    
    public Set<Integer> getAllTasktypeIds() {
    	return tasktypeMap.keySet();
    }	
}
