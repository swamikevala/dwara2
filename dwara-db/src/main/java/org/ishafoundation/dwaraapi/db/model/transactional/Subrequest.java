package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.model.transactional.json.SubrequestDetails;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Status;



@Entity
@Table(name="subrequest")
public class Subrequest {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="subrequest_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	// Many Subrequests in a request
	@ManyToOne(fetch = FetchType.LAZY)
    private Request request;
	
	@Column(name="status_id")
	private Status status;
	
	@Column(name="action_id")
	private Action action;
	

	 /* This converter does the trick */
	@Lob
	@Column(name="details")
	//@Convert(converter = SubrequestDetailsAttributeConverter.class)
	private SubrequestDetails details;
	  
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public SubrequestDetails getDetails() {
		return details;
	}

	public void setDetails(SubrequestDetails details) {
		this.details = details;
	}
}