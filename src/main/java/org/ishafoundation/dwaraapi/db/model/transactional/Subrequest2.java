package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.attributeconverter.SubrequestActionColumnsAttributeConverter;
import org.ishafoundation.dwaraapi.enumreferences.Status;



@Entity
@Table(name="subrequest2")
public class Subrequest2 {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="subrequest2_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	// Many Subrequests in a request
	@ManyToOne(fetch = FetchType.LAZY)
    private Request request;
	
	@Column(name="status_id")
	private Status status;

	 /* This converter does the trick */
	@Lob
	@Convert(converter = SubrequestActionColumnsAttributeConverter.class)
	private ActionColumns actionColumns;
	  
	
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

	public ActionColumns getActionColumns() {
		return actionColumns;
	}

	public void setActionColumns(ActionColumns actionColumns) {
		this.actionColumns = actionColumns;
	}
}