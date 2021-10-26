package org.ishafoundation.dwaraapi.api.resp.report;

public class RespondReportRestoreTime {
    public String artifactName;
    public long size;
    public String requestedTime;
    public String startedTime;
    public String completedTime;
    public long timeTaken;
    public long totalTime;

    public RespondReportRestoreTime(String artifactName, long size, String requestedTime, String startedTime, String completedTime,
            long timeTaken, long totalTime) {
        this.artifactName = artifactName;
        this.size = size;
        this.requestedTime = requestedTime;
        this.startedTime = startedTime;
        this.completedTime = completedTime;
        this.timeTaken = timeTaken;
        this.totalTime = totalTime;
    }
}
