package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.List;

public class AutoloaderResponse
{
    private String id;

    private List<Drives> drives;

    private List<Tapes> tapes;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setDrives(List<Drives> drives){
        this.drives = drives;
    }
    public List<Drives> getDrives(){
        return this.drives;
    }
    public void setTapes(List<Tapes> tapes){
        this.tapes = tapes;
    }
    public List<Tapes> getTapes(){
        return this.tapes;
    }
}


