package org.ishafoundation.dwaraapi.db.model.transactional.jointables;

import java.io.Serializable;

public class TapeCatalog implements Serializable{
    private static final long serialVersionUID = 5079710499692080313L;
    private String volumeId;
    private String volumeGroup;
    private String format;
    private String location;
    private String status;
    private String finalizedDate;
    private String usedSpace;
    
    public TapeCatalog(String volumeId, String volumeGroup, String format, String location, String status,
            String finalizedDate, String usedSpace) {
        this.volumeId = volumeId;
        this.volumeGroup = volumeGroup;
        this.format = format;
        this.location = location;
        this.status = status;
        this.finalizedDate = finalizedDate;
        this.usedSpace = usedSpace;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeGroup() {
        return volumeGroup;
    }

    public void setVolumeGroup(String volumeGroup) {
        this.volumeGroup = volumeGroup;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinalizedDate() {
        return finalizedDate;
    }

    public void setFinalizedDate(String finalizedDate) {
        this.finalizedDate = finalizedDate;
    }

    public String getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(String usedSpace) {
        this.usedSpace = usedSpace;
    }
}
