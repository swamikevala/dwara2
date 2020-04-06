package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.TaskfiletypeDao;
import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Extns_TaskfiletypeCacheUtil {
	@Autowired
	private TaskfiletypeDao taskfiletypeDao;
	
	private List<Taskfiletype> taskfiletypeList = null;
	private Map<String, Taskfiletype> extns_TaskfiletypeMap = new HashMap<String, Taskfiletype>(); 

	@PostConstruct
	private void loadExtns_FiletypeMap() {
		taskfiletypeList = (List<Taskfiletype>) taskfiletypeDao.findAll();
		for (Taskfiletype taskfiletype : taskfiletypeList) {
			String[] filetypeExtns = {};// TODO : commentiong out for now filetype.getExtensions().split(",");
			for (int i = 0; i < filetypeExtns.length; i++) {
				String nthExtn = filetypeExtns[i].trim().toUpperCase();
				if(extns_TaskfiletypeMap.get(nthExtn) == null) // if the extn is not already mapped to a file type...
					extns_TaskfiletypeMap.put(nthExtn, taskfiletype);
			}
		}
	}
	
	public Map<String, Taskfiletype> getExtns_FiletypeMap() {
		return extns_TaskfiletypeMap;
	}
}
