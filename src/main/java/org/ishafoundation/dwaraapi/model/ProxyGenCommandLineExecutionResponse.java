package org.ishafoundation.dwaraapi.model;

public class ProxyGenCommandLineExecutionResponse extends CommandLineExecutionResponse{
	
	private String destinationPathname;

	public String getDestinationPathname() {
		return destinationPathname;
	}

	public void setDestinationPathname(String destinationPathname) {
		this.destinationPathname = destinationPathname;
	}
}
