package org.ishafoundation.dwaraapi.db.model.master.process;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="extension")
public class Extension {

	@Id
	@Column(name="extension_id")
	private int extensionId;
	
	@Column(name="name")
	private String name;

	@Column(name="description")
	private String description;

		
	public int getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(int extensionId) {
		this.extensionId = extensionId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}