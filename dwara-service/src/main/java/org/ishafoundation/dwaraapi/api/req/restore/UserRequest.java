package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.Domain;

public class UserRequest {
	/*
	{
		  "domain": "one",
		  "location": "IIIT",
		  "output_folder": "JIRA-1234-Randommm",
		  "destinationpath": "/data/restored",
		  "verify": false,
		  "fileParams": [
		    {
		      "file_id": 67,
		      "priority": 0
		    }  
		  ]
	}*/
	private Domain domain;
	private String location;
	private String output_folder;
	private String destinationpath;
	private boolean verify;
	private List<FileParams> fileParams;

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getOutput_folder() {
		return output_folder;
	}

	public void setOutput_folder(String output_folder) {
		this.output_folder = output_folder;
	}

	public String getDestinationpath() {
		return destinationpath;
	}

	public void setDestinationpath(String destinationpath) {
		this.destinationpath = destinationpath;
	}

	public boolean isVerify() {
		return verify;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	public List<FileParams> getFileParams() {
		return fileParams;
	}

	public void setFileParams(List<FileParams> fileParams) {
		this.fileParams = fileParams;
	}

}
