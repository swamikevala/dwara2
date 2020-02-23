package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="property_library")
public class PropertyLibrary {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="property_library_id")
	private int propertyLibraryId;
	
	@Column(name="property_id")
	private int propertyId;

	@Column(name="library_id")
	private int libraryId;

	@Column(name="value")
	private String value;

		
	public int getPropertyLibraryId() {
		return propertyLibraryId;
	}

	public void setPropertyLibraryId(int propertyLibraryId) {
		this.propertyLibraryId = propertyLibraryId;
	}
	
	public int getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}
	
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}