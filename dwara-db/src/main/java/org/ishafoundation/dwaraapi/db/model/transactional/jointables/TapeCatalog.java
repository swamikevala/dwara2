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
    public Long usedSpace;
    public Long capacity;
    public List<String> artifactClass;
    
    public TapeCatalog(String volumeId, String volumeGroup, String format, String location, String status,
            String initializedDate, String finalizedDate, Long usedSpace, Long capacity, List<String> artifactClass) {
        this.volumeId = volumeId;
        this.volumeGroup = volumeGroup;
        this.format = format;
        this.location = location;
        this.status = status;
        this.initializedDate = initializedDate;
        this.finalizedDate = finalizedDate;
        this.usedSpace = usedSpace;
        this.capacity = capacity;
        this.artifactClass = artifactClass;
    }
}
