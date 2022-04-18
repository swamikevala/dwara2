package org.ishafoundation.dwaraapi.enumreferences;

public enum JiraTransition {
	waiting_for_footage("Waiting For Footage"),
	footage_request_closed("Footage Request Closed");
	
	private String transitionAsInJira;
	
	JiraTransition(String transitionAsInJira) {
	    this.transitionAsInJira = transitionAsInJira;
	}
	
	public String getTransitionAsInJira() {
		return transitionAsInJira;
	}
	
	public static JiraTransition getJiraStatus(String transitionAsInJira){
		JiraTransition jiraTransition = null;
	    for (JiraTransition js : JiraTransition.values()) {
	        if (js.transitionAsInJira.equals(transitionAsInJira)) {
	        	jiraTransition = js;
	        	break;
	        }
	    }
		return jiraTransition;
	}
}
