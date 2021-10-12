package org.ishafoundation.dwaraapi.api.resp.report;

public class RespondReportIngestSize {
    public String artifactClass;
    public String time;
    public Long size;
    public RespondReportIngestSize(String artifactClass, String time, Long size) {
        this.artifactClass = artifactClass;
        this.time = time;
        this.size = size;
    }
}
