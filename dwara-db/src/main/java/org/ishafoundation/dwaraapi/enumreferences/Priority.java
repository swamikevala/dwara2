package org.ishafoundation.dwaraapi.enumreferences;

public enum Priority { 
	// Critical - High - Medium - Low ???
	// A lower priority number means that the higher is the urgency
	critical(1), 
	high(2),
	normal(3); // named normal instead of low - because its the default priority used normally... 
	
	private int priorityValue;
	
	Priority(int priorityValue) {
	    this.priorityValue = priorityValue;
	}
	
	public int getPriorityValue() {
		return priorityValue;
	}
}
