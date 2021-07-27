package org.ishafoundation.dwaraapi.api.req.catalog;

public class ArtifactCatalogRequest {
    public String[] artifactClass;
    public String[] volumeGroup;
    public String[] copyNumber;
    public String volumeId;
    public String startDate;
    public String endDate;
    public String artifactName;
    public boolean deleted;
    public boolean softRenamed;
}
