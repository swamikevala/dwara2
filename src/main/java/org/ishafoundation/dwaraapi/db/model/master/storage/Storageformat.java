package org.ishafoundation.dwaraapi.db.model.master.storage;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="storageformat")
public class Storageformat {

	@Id
	@Column(name="storageformat_id")
	private int storageformatId;
	
	@Column(name="name")
	private String name;

		
	public int getStorageformatId() {
		return storageformatId;
	}

	public void setStorageformatId(int storageformatId) {
		this.storageformatId = storageformatId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}