package org.ishafoundation.dwaraapi.db.model.master;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapelibrary")
public class Tapelibrary {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;

	@Column(name="slots")
	private int slots;
	
	public Tapelibrary() {}
	public Tapelibrary(int id, String name, int slots){
		this.id = id;
		this.name = name;
		this.slots= slots;
	}
		
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
	
	public int getSlots() {
		return slots;
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}

}