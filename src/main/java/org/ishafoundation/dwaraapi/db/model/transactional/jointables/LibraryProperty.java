package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryPropertyKey;
import org.ishafoundation.dwaraapi.db.model.master.Property;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;

@Entity(name = "LibraryProperty")
@Table(name="library_property")
public class LibraryProperty {

	@EmbeddedId
	private LibraryPropertyKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryId")
	private Library library;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("propertyId")
	private Property property;

	@Column(name="value")
	private String value;
	
	public LibraryProperty() {
		
	}

	public LibraryProperty(Library library, Property property) {
		this.library = library;
		this.property = property;
		this.id = new LibraryPropertyKey(library.getId(), property.getId());
	}
	
    public LibraryPropertyKey getId() {
		return id;
	}

	public void setId(LibraryPropertyKey id) {
		this.id = id;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryProperty that = (LibraryProperty) o;
        return Objects.equals(library, that.library) &&
               Objects.equals(property, that.property);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(library, property);
    }

}