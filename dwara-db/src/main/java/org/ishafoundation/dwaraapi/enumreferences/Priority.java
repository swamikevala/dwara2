package org.ishafoundation.dwaraapi.enumreferences;

public enum Priority {
	critical(0),
	high(1),
	normal(2);
	
	private int priorityValue;
	
	Priority(int priorityValue) {
	    this.priorityValue = priorityValue;
	}
	
	public int getPriorityValue() {
		return priorityValue;
	}
}
