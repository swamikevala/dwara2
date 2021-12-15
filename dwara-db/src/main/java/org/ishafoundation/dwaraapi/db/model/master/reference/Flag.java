package org.ishafoundation.dwaraapi.db.model.master.reference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="flag")
public class Flag {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="value")
	private String value;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
