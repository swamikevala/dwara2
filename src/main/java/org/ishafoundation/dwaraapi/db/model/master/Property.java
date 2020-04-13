package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassProperty;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="property")
public class Property {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="regex")
	private String regex;

	@Column(name="replace_char_space")
	private String replaceCharSpace;

    @OneToMany(mappedBy = "property",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassProperty> libraryclassProperty = new ArrayList<>();
    
    @OneToMany(mappedBy = "property",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryProperty> libraryProperty = new ArrayList<>();
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	
	@JsonIgnore
	public List<LibraryclassProperty> getLibraryclassProperty() {
		return libraryclassProperty;
	}
	
	@JsonIgnore
	public void setLibraryclassProperty(List<LibraryclassProperty> libraryclassProperty) {
		this.libraryclassProperty = libraryclassProperty;
	}
	
	@JsonIgnore
	public List<LibraryProperty> getLibraryProperty() {
		return libraryProperty;
	}
	
	@JsonIgnore
	public void setLibraryProperty(List<LibraryProperty> libraryProperty) {
		this.libraryProperty = libraryProperty;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(name, property.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}