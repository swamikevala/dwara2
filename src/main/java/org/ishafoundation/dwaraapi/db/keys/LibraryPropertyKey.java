package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryPropertyKey implements Serializable {
 
	private static final long serialVersionUID = -7091019118335418993L;

	@Column(name = "library_id")
    private int libraryId;
 
    @Column(name = "property_id")
    private int propertyId;
 
    public LibraryPropertyKey() {}
    
    public LibraryPropertyKey(
        int libraryId,
        int propertyId) {
        this.libraryId = libraryId;
        this.propertyId = propertyId;
    }
 
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public int getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(int propertyId) {
		this.propertyId = propertyId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryPropertyKey that = (LibraryPropertyKey) o;
        return Objects.equals(libraryId, that.libraryId) &&
               Objects.equals(propertyId, that.propertyId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryId, propertyId);
    }
}