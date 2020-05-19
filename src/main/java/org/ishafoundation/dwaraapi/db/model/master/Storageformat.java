package org.ishafoundation.dwaraapi.db.model.master;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="storageformat")
public class Storageformat {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="filesize_increase_rate")
	private float filesizeIncreaseRate;
	
	@Column(name="filesize_increase_const")
	private int filesizeIncreaseConst;
	
	public Storageformat() {
		
	}
	
	public Storageformat(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getStorageformatId() {
		return id;
	}

	public void setStorageformatId(int storageformatId) {
		this.id = storageformatId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}