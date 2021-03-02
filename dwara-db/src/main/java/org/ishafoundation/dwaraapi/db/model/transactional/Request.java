package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;

import com.vladmihalcea.hibernate.type.json.JsonStringType;



@Entity
@Table(name="request")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class Request {

	
	/*
	 * we want to generate the sequence per table, so we are going for TableGenerator, but when the jvm restarts it jumps values
	 * 
	 * see here
	 * 
	 * https://stackoverflow.com/questions/2895242/whats-the-reason-behind-the-jumping-generatedvaluestrategy-generationtype-tabl
	 * https://forum.hibernate.org/viewtopic.php?f=9&t=980566&start=0
	 * 
	 * To avoid this we are using allocationSize = 1, which has a performance hit, but solves the business usecase.
	 * 
	 * Also check out this, but we are not worried about the performace.
	 * https://vladmihalcea.com/why-you-should-never-use-the-table-identifier-generator-with-jpa-and-hibernate/
	 */
		
	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="request_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private RequestType type;

	@Column(name="action_id")
	private Action actionId;
	
	@OneToOne(fetch = FetchType.LAZY)
	private User requestedBy;
	
	@Column(name="requested_at")
	private LocalDateTime requestedAt;
	
	@Column(name="completed_at")
	private LocalDateTime completedAt;

	@ManyToOne(fetch = FetchType.LAZY)
    private Request requestRef;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
	
	@Column(name="external_ref")
	private String externalRef;
	
	@Type(type = "json")
	@Column(name="details", columnDefinition = "json")
	private RequestDetails details;
	
	public Request() {

	}

	public Request(int _id){
		id = _id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public RequestType getType() {
		return type;
	}

	public void setType(RequestType type) {
		this.type = type;
	}
	
	public Action getActionId() {
		return actionId;
	}

	public void setActionId(Action actionId) {
		this.actionId = actionId;
	}

	public User getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(User requestedBy) {
		this.requestedBy = requestedBy;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}
	
	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public Request getRequestRef() {
		return requestRef;
	}

	public void setRequestRef(Request requestRef) {
		this.requestRef = requestRef;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public RequestDetails getDetails() {
		return details;
	}

	public void setDetails(RequestDetails details) {
		this.details = details;
	}
}