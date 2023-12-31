package org.ishafoundation.dwaraapi.db.model.master.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="error")
public class Error {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="description")
	private String description;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
