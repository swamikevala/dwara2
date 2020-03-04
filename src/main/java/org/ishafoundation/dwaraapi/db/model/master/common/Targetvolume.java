package org.ishafoundation.dwaraapi.db.model.master.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="targetvolume")
public class Targetvolume {
	@Id
	@Column(name="targetvolume_id")
	private int targetvolumeId;
	
	@Column(name="name")
	private String name;
	

	public int getTargetvolumeId() {
		return targetvolumeId;
	}

	public void setTargetvolumeId(int targetvolumeId) {
		this.targetvolumeId = targetvolumeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
