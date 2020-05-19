package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity(name="Tapedrivetype")
@Table(name="tapedrivetype")
public class Tapedrivetype {

	@Id
	@Column(name="id")
	private int id; 
	
	@Column(name="name", unique = true)
	private String name;
	
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
  
}