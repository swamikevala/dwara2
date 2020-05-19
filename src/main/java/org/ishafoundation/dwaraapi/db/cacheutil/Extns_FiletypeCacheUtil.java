package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.FiletypeDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Filetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Extns_FiletypeCacheUtil {
	@Autowired
	private FiletypeDao filetypeDao;
	
	private List<Filetype> filetypeList = null;
	private Map<String, Filetype> extns_FiletypeMap = new HashMap<String, Filetype>(); 

	@PostConstruct
	private void loadExtns_FiletypeMap() {
		filetypeList = (List<Filetype>) filetypeDao.findAll();
		for (Filetype filetype : filetypeList) {
			String[] filetypeExtns = {};// TODO : commentiong out for now filetype.getExtensions().split(",");
			for (int i = 0; i < filetypeExtns.length; i++) {
				String nthExtn = filetypeExtns[i].trim().toUpperCase();
				if(extns_FiletypeMap.get(nthExtn) == null) // if the extn is not already mapped to a file type...
					extns_FiletypeMap.put(nthExtn, filetype);
			}
		}
	}
	
	public Map<String, Filetype> getExtns_FiletypeMap() {
		return extns_FiletypeMap;
	}
}
