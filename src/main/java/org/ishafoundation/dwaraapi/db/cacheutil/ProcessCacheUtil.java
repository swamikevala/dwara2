package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.workflow.ProcessDao;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessCacheUtil {
	@Autowired
	private ProcessDao processDao;
	
	private List<Process> processList = null;
	private Map<Integer, Process> processMap = new HashMap<Integer, Process>(); 

	@PostConstruct
	private void loadProcessList() {
    	processList = (List<Process>) processDao.findAll();
		for (Process process : processList) {
			processMap.put(process.getProcessId(), process);
		}
	}
	
	public List<Process> getProcessList(){
		return processList;
	}
	
    public Process getProcess(int processId) {
    	return processMap.get(processId);
    }
    
    public Set<Integer> getAllProcessIds() {
    	return processMap.keySet();
    }	
}
