package org.ishafoundation.dwaraapi.db.cacheutil;

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
	private Map<Integer, Action> actionId_action_Map = new HashMap<Integer, Action>();
	private Map<String, Action> actionName_action_Map = new HashMap<String, Action>();

	@PostConstruct
	private void loadActionList() {
    	actionList = (List<Action>) actionDao.findAll();
		for (Action action : actionList) {
			actionId_action_Map.put(action.getId(), action);
			actionName_action_Map.put(action.getName(), action);
		}
	}
	
	public List<Action> getActionList(){
		return actionList;
	}
    
	public Action getAction(int actionId) {
    	return actionId_action_Map.get(actionId);
    }
    
    public Action getAction(String actionName) {
    	return actionName_action_Map.get(actionName);
    }
    
    public Set<Integer> getAllActionIds() {
    	return actionId_action_Map.keySet();
    }	
}
