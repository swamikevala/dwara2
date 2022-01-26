package org.ishafoundation.dwaraapi.api.req.clip;

import java.util.List;

public class ClipListRequest {
    int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    String name;
    //int createdby;
    List<Integer> clipIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public List<Integer> getClipIds() {
        return clipIds;
    }

    public void setClipIds(List<Integer> clipIds) {
        this.clipIds = clipIds;
    }
}
