package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="property_libraryclass")
public class PropertyLibraryclass {

	@Id
	@Column(name="property_libraryclass_id")
	private int propertyLibraryclassId;
	
	@Column(name="property_id")
	private int propertyId;

	@Column(name="libraryclass_id")
	private int libraryclassId;

	@Column(name="position")
	private int position;

	@Column(name="optional")
	private boolean optional;

		
	public int getPropertyLibraryclassId() {
		return propertyLibraryclassId;
	}

	public void setPropertyLibraryclassId(int propertyLibraryclassId) {
		this.propertyLibraryclassId = propertyLibraryclassId;
	}
	
	public int getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}
	
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

}