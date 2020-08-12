package org.ishafoundation.dwaraapi.api.resp.format;

import org.ishafoundation.dwaraapi.enumreferences.Status;

public class SystemRequestsForFormatResponse
{
    private int id;

    private int jobId;

    private String volume;

    private Status status;

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
    public void setVolume(String volume){
        this.volume = volume;
    }
    public String getVolume(){
        return this.volume;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public Status getStatus(){
        return this.status;
    }
}
