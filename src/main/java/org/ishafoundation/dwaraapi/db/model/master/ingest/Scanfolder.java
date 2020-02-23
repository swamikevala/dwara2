package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="scanfolder")
public class Scanfolder {

	@Id
	@Column(name="scanfolder_id")
	private int scanfolderId;
	
	@Column(name="name")
	private String name;

		
	public int getScanfolderId() {
		return scanfolderId;
	}

	public void setScanfolderId(int scanfolderId) {
		this.scanfolderId = scanfolderId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}