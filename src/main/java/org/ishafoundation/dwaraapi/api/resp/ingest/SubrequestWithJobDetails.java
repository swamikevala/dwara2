package org.ishafoundation.dwaraapi.api.resp.ingest;

import java.util.List;

public class SubrequestWithJobDetails extends Subrequest {
	private List<Job> jobList;

	public List<Job> getJobList() {
		return jobList;
	}

	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}
}
