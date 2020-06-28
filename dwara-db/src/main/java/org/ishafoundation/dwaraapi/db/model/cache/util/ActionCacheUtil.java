package org.ishafoundation.dwaraapi.db.model.cache.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.ActionDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionCacheUtil {
	@Autowired
	private ActionDao actionDao;
	
	private List<Action> actionList = null;
	private Map<String, Action> actionId_action_Map = new HashMap<String, Action>();

	@PostConstruct
	private void loadActionList() {
    	actionList = (List<Action>) actionDao.findAll();
		for (Action action : actionList) {
			actionId_action_Map.put(action.getId(), action);
		}
	}
	
	public List<Action> getActionList(){
		return actionList;
	}
    
    public Action getAction(String actionName) {
    	return actionId_action_Map.get(actionName);
    }
    
    public Set<String> getAllActionIds() {
    	return actionId_action_Map.keySet();
    }	
}
