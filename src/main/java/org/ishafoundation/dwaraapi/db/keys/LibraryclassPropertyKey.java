package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LibraryclassPropertyKey implements Serializable {
 
	private static final long serialVersionUID = -6287084481290875948L;

	@Column(name = "libraryclass_id")
    private int libraryclassId;
 
    @Column(name = "property_id")
    private int propertyId;
 
    public LibraryclassPropertyKey() {}
    
    public LibraryclassPropertyKey(
        int libraryclassId,
        int propertyId) {
        this.libraryclassId = libraryclassId;
        this.propertyId = propertyId;
    }
 
	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
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
 
        LibraryclassPropertyKey that = (LibraryclassPropertyKey) o;
        return Objects.equals(libraryclassId, that.libraryclassId) &&
               Objects.equals(propertyId, that.propertyId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclassId, propertyId);
    }
}