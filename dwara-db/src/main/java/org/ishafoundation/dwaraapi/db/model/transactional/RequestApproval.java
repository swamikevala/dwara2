package org.ishafoundation.dwaraapi.db.model.transactional;


import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="request_approval")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class RequestApproval {
    @Id
    String id;
    String externalRefID;
    String type;
    String approvalStatus;
    String approver;
    String approverEmail;
    Integer requestedBy;
    Integer createdBy;
    String destinationPath;
    String priority;

    @Column(name="createdAt")
    LocalDateTime createdAt;

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    @Column(name = "ApprovalDate")
    String approvalDate;
    //@Column(name="details")
    @Lob
    @Type(type = "json")
    @Column(name="details", columnDefinition = "json")

    List<RestoreBucketFile> details ;
    public String getApproverEmail() {
        return approverEmail;
    }

    public void setApproverEmail(String approverEmail) {
        this.approverEmail = approverEmail;
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



    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RestoreBucketFile> getDetails() {
        return details;
    }

    public void setDetails(List<RestoreBucketFile> details) {

        this.details = details;
    }

    public void addDetails(List<RestoreBucketFile> details){
        this.details.addAll(details);
    }

   public   RequestApproval(){};

   public  RequestApproval(TRestoreBucket tRestoreBucket){
        this.approvalDate =tRestoreBucket.getApprovalDate();
        this.approvalStatus=tRestoreBucket.getApprovalStatus();
        this.createdAt=tRestoreBucket.getCreatedAt();
        this.createdBy=tRestoreBucket.getCreatedBy();
        this.approver=tRestoreBucket.getApprover();
        this.approverEmail=tRestoreBucket.getApproverEmail();
        this.destinationPath=tRestoreBucket.getDestinationPath();
        this.details =tRestoreBucket.getDetails();
        this.externalRefID =tRestoreBucket.getExternalRefID();
        this.id =tRestoreBucket.getId();
        this.priority =tRestoreBucket.getPriority();
        this.requestedBy=tRestoreBucket.getRequestedBy();
        this.type =tRestoreBucket.getType();
    }


}
