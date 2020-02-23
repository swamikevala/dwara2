package org.ishafoundation.dwaraapi.db.model.master.storage;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapetype")
public class Tapetype {

	@Id
	@Column(name="tapetype_id")
	private int tapetypeId;
	
	@Column(name="name")
	private String name;

	@Column(name="capacity")
	private String capacity;

		
	public int getTapetypeId() {
		return tapetypeId;
	}

	public void setTapetypeId(int tapetypeId) {
		this.tapetypeId = tapetypeId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

}