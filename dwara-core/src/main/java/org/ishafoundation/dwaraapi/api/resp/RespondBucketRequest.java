package org.ishafoundation.dwaraapi.api.resp;

public class RespondBucketRequest {
    private String approvedStatus;
    public String getApprovedStatus() {
        return approvedStatus;
    }
    public void setApprovedStatus(String approvedStatus) {
        this.approvedStatus = approvedStatus;
    }
    public String getApprovedDate() {
        return approvedDate;
    }
    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }
    private String approvedDate;
}
