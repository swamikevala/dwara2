package org.ishafoundation.dwaraapi.db.model.master.reference;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity(name="Devicetype")
@Table(name="devicetype")
public class Devicetype {

	@Id
	@Column(name="id")
	private int id; 
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="description")
	private String description;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}    
}