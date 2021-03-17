package org.ishafoundation.dwaraapi.db.model.transactional.jointables;

import java.io.Serializable;
import java.util.List;

public class TapeCatalog implements Serializable{
    private static final long serialVersionUID = 5079710499692080313L;
    public String volumeId;
    public String volumeGroup;
    public String format;
    public String location;
    public String status;
    public String initializedDate;
    public String finalizedDate;
    public Long usedCapacity;
    public Long capacity;
    public List<String> artifactClass;
    public boolean isSuspect;
    
    public TapeCatalog(String volumeId, String volumeGroup, String format, String location, String status,
            String initializedDate, String finalizedDate, Long usedCapacity, Long capacity, List<String> artifactClass, boolean isSuspect) {
        this.volumeId = volumeId;
        this.volumeGroup = volumeGroup;
        this.format = format;
        this.location = location;
        this.status = status;
        this.initializedDate = initializedDate;
        this.finalizedDate = finalizedDate;
        this.usedCapacity = usedCapacity;
        this.capacity = capacity;
        this.artifactClass = artifactClass;
        this.isSuspect = isSuspect;
    }
}
