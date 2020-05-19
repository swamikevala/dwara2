package org.ishafoundation.dwaraapi.db.model.master;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapetype")
public class Tapetype {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="capacity")
	private long capacity;

	@Column(name="blocksize")
	private int blocksize;
	
	public Tapetype() {}
	
	public Tapetype(int id, String name, long capacity) {
		this.id = id;
		this.name = name;
		this.capacity = capacity;
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
	
	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}
	
	public int getBlocksize() {
		return blocksize;
	}
	
	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}
	
}