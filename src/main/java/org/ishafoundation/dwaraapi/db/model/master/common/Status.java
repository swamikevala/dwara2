package org.ishafoundation.dwaraapi.db.model.master.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="status")
public class Status {
	@Id
	@Column(name="status_id")
	private int statusId;
	
	@Column(name="name")
	private String name;
	

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
