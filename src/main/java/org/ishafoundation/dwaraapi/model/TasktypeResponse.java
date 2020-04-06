package org.ishafoundation.dwaraapi.model;

public class TasktypeResponse extends CommandLineExecutionResponse{

	private String destinationPathname;

	private boolean needDbUpdate; // Do application related data need to be updated in DB
	
	private String appId; // only relevant if above is true. Holds application 
	

	public String getDestinationPathname() {
		return destinationPathname;
	}

	public void setDestinationPathname(String destinationPathname) {
		this.destinationPathname = destinationPathname;
	}

	public boolean needDbUpdate() {
		return needDbUpdate;
	}

	public void setNeedDbUpdate(boolean needDbUpdate) {
		this.needDbUpdate = needDbUpdate;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
}
