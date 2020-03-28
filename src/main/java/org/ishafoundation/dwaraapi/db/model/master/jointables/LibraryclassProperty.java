package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassPropertyKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Property;

@Entity(name = "LibraryclassProperty")
@Table(name="libraryclass_property")
public class LibraryclassProperty {

	@EmbeddedId
	private LibraryclassPropertyKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryclassId")
	private Libraryclass libraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("propertyId")
	private Property property;

	@Column(name="position")
	private int position;

	@Column(name="optional")
	private boolean optional;
	
	public LibraryclassProperty() {
		
	}

	public LibraryclassProperty(Libraryclass libraryclass, Property property) {
		this.libraryclass = libraryclass;
		this.property = property;
		this.id = new LibraryclassPropertyKey(libraryclass.getId(), property.getId());
	}
	
    public LibraryclassPropertyKey getId() {
		return id;
	}

	public void setId(LibraryclassPropertyKey id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryclassProperty that = (LibraryclassProperty) o;
        return Objects.equals(libraryclass, that.libraryclass) &&
               Objects.equals(property, that.property);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclass, property);
    }

}