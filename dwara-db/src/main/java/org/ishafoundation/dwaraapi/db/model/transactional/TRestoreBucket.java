package org.ishafoundation.dwaraapi.db.model.transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="t_restorebucket")
public class TRestoreBucket {
    @Id
    String id;
    String externalRefID;
    String type;
    String approvalStatus;
    String approver;
    String requestedBy;
    String createdBy;
    @Column(name="createdAt")
    Date createdAt;
    @Column(name="details")
    List<RestoreBucketFile> details;

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRequesyedBy() {
        return requestedBy;
    }

    public void setRequesyedBy(String requesyedBy) {
        this.requestedBy = requesyedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<RestoreBucketFile> getDetails() {
        return details;
    }

    public void setDetails(List<RestoreBucketFile> details) {
        this.details = details;
    }

    public TRestoreBucket(String id, String createdBy, Date createdAt) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public TRestoreBucket(){}
}
