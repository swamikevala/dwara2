package org.ishafoundation.dwaraapi.model;

public class TaskResponse extends CommandLineExecutionResponse{

	private String destinationPathname;
	
	private String appId; // only relevant if above is true. Holds application 
	

	public String getDestinationPathname() {
		return destinationPathname;
	}

	public void setDestinationPathname(String destinationPathname) {
		this.destinationPathname = destinationPathname;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
}
