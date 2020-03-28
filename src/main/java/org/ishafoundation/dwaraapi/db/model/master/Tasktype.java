package org.ishafoundation.dwaraapi.db.model.master;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tasktype")
public class Tasktype {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
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