package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AutoloaderResponse
{
    private String id;

    private List<Drive> drives;

    private List<Tape> tapes;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setDrives(List<Drive> drives){
        this.drives = drives;
    }
    public List<Drive> getDrives(){
        return this.drives;
    }
    public void setTapes(List<Tape> tapes){
        this.tapes = tapes;
    }
    public List<Tape> getTapes(){
        return this.tapes;
    }
}


