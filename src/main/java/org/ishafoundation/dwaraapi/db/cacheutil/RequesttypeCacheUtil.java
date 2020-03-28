package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequesttypeCacheUtil {
	@Autowired
	private RequesttypeDao requesttypeDao;
	
	private List<Requesttype> requesttypeList = null;
	private Map<Integer, Requesttype> requesttypeId_requesttype_Map = new HashMap<Integer, Requesttype>();
	private Map<String, Requesttype> requesttypeName_requesttype_Map = new HashMap<String, Requesttype>();

	@PostConstruct
	private void loadRequesttypeList() {
    	requesttypeList = (List<Requesttype>) requesttypeDao.findAll();
		for (Requesttype requesttype : requesttypeList) {
			requesttypeId_requesttype_Map.put(requesttype.getId(), requesttype);
			requesttypeName_requesttype_Map.put(requesttype.getName(), requesttype);
		}
	}
	
	public List<Requesttype> getRequesttypeList(){
		return requesttypeList;
	}
    
	public Requesttype getRequesttype(int requesttypeId) {
    	return requesttypeId_requesttype_Map.get(requesttypeId);
    }
    
    public Requesttype getRequesttype(String requesttypeName) {
    	return requesttypeName_requesttype_Map.get(requesttypeName);
    }
    
    public Set<Integer> getAllRequesttypeIds() {
    	return requesttypeId_requesttype_Map.keySet();
    }	
}
