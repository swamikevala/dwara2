package org.ishafoundation.dwaraapi.model;

public class TaskResponse extends CommandLineExecutionResponse{
	
	private String destinationPathname;

	public String getDestinationPathname() {
		return destinationPathname;
	}

	public void setDestinationPathname(String destinationPathname) {
		this.destinationPathname = destinationPathname;
	}
}
