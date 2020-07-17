package org.ishafoundation.dwaraapi.process;

import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecutionResponse;

public class ProcessingtaskResponse extends CommandLineExecutionResponse{

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
