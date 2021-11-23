package org.ishafoundation.dwaraapi.api.resp.restore;

import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Column;
import javax.persistence.Lob;
import java.util.List;

public class RestoreBucketResponse  {

    String creatorName;

    String externalRefID;
    String type;
    String approvalStatus;
    String approver;
    String approverEmail;
    Integer requestedBy;
    Integer createdBy;
    String destinationPath;
    String priority;
    String approvalDate;
    List<RestoreBucketFile> details ;

RestoreBucketResponse(){


}



    public String getExternalRefID() {
        return externalRefID;
    }

    public void setExternalRefID(String externalRefID) {
        this.externalRefID = externalRefID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApproverEmail() {
        return approverEmail;
    }

    public void setApproverEmail(String approverEmail) {
        this.approverEmail = approverEmail;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public List<RestoreBucketFile> getDetails() {
        return details;
    }

    public void setDetails(List<RestoreBucketFile> details) {
        this.details = details;
    }

    public RestoreBucketResponse(TRestoreBucket tRestoreBucket){
    this.approvalDate=tRestoreBucket.getApprovalDate();
    this.createdBy=tRestoreBucket.getCreatedBy();
    this.approvalStatus=tRestoreBucket.getApprovalStatus();
    this.approver=tRestoreBucket.getApprover();
    this.approverEmail = tRestoreBucket.getApproverEmail();
    this.destinationPath=tRestoreBucket.getDestinationPath();
    this.details =tRestoreBucket.getDetails();
    this.externalRefID=tRestoreBucket.getExternalRefID();
    this.priority=tRestoreBucket.getPriority();
    this.requestedBy =tRestoreBucket.getRequestedBy();
    this.type =tRestoreBucket.getType();
    //
}

public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
