package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.ArrayList;
import java.util.List;

public class JobDaoQueryProps {
	
	private List<String> taskNameList = new ArrayList<String>();
	
	private int limit;

	public List<String> getTaskNameList() {
		return taskNameList;
	}

	public int getLimit() {
		return limit;
	}

	public void setTaskNameList(List<String> taskNameList) {
		this.taskNameList = taskNameList;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
