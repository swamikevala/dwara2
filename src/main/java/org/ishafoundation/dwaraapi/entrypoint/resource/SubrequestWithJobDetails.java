package org.ishafoundation.dwaraapi.entrypoint.resource;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.ingest.Job;

public class SubrequestWithJobDetails extends Subrequest {
	private List<Job> jobList;

	public List<Job> getJobList() {
		return jobList;
	}

	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}
}
