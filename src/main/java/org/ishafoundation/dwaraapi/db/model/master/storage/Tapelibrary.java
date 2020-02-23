package org.ishafoundation.dwaraapi.db.model.master.storage;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapelibrary")
public class Tapelibrary {

	@Id
	@Column(name="tapelibrary_id")
	private int tapelibraryId;
	
	@Column(name="name")
	private String name;

	@Column(name="slots")
	private int slots;

		
	public int getTapelibraryId() {
		return tapelibraryId;
	}

	public void setTapelibraryId(int tapelibraryId) {
		this.tapelibraryId = tapelibraryId;
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