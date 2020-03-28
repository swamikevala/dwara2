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
	private Integer id; // is Integer and hence value can be null - "Copy Tasks" in task table are defaulted tasktype
	
	@Column(name="name")
	private String name;

		
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}