package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="property")
public class Property {

	@Id
	@Column(name="property_id")
	private int propertyId;
	
	@Column(name="name")
	private String name;

	@Column(name="regex")
	private String regex;

	@Column(name="replace_char_space")
	private String replaceCharSpace;

		
	public int getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	public String getReplaceCharSpace() {
		return replaceCharSpace;
	}

	public void setReplaceCharSpace(String replaceCharSpace) {
		this.replaceCharSpace = replaceCharSpace;
	}

}