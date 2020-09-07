package org.ishafoundation.dwaraapi.api.resp.mapdrives;

import org.ishafoundation.dwaraapi.enumreferences.Status;

public class SystemRequestForMapDriveResponse
{
    private int id;

    private int jobId;

    private Status status;
    
    private String autoloaderId;

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
    public void setJobId(int jobId){
        this.jobId = jobId;
    }
    public int getJobId(){
        return this.jobId;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public Status getStatus(){
        return this.status;
    }
	public String getAutoloaderId() {
		return autoloaderId;
	}
	public void setAutoloaderId(String autoloaderId) {
		this.autoloaderId = autoloaderId;
	}
}
