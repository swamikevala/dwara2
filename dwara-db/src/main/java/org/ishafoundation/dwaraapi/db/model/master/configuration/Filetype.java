package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Filetype")
@Table(name="filetype")
public class Filetype{

	@Id
	@Column(name="id")
	private String id;
	
	@Column(name="description")
	private String description;

    @OneToMany(mappedBy = "filetype",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ExtensionFiletype> extensions = new ArrayList<>();
	
    @Column(name="pathname_regex")
    private String pathnameRegex;
    
    public Filetype() {
    	
    }
    
    public Filetype(String id) {
    	this.id = id;
    }
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@JsonIgnore
	public List<ExtensionFiletype> getExtensions() {
		return extensions;
	}

	@JsonIgnore
	public void setExtensions(List<ExtensionFiletype> extensions) {
		this.extensions = extensions;
	}
	/*
    public void addExtension(Extension extension) {
        ExtensionFiletype extensionFiletype = new ExtensionFiletype(extension, this);
        extensions.add(extensionFiletype);
        extension.getFiletypes().add(extensionFiletype);
    }
    
    public void removeExtension(Extension extension) {
        for (Iterator<ExtensionFiletype> iterator = extensions.iterator();
             iterator.hasNext(); ) {
            ExtensionFiletype extensionFiletype = iterator.next();
 
            if (extensionFiletype.getFiletype().equals(this) &&
                    extensionFiletype.getExtension().equals(extension)) {
                iterator.remove();
                extensionFiletype.getExtension().getFiletypes().remove(extensionFiletype);
                extensionFiletype.setFiletype(null);
                extensionFiletype.setExtension(null);
            }
        }
    }*/
    
    public String getPathnameRegex() {
		return pathnameRegex;
	}

	public void setPathnameRegex(String pathnameRegex) {
		this.pathnameRegex = pathnameRegex;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Filetype filetype = (Filetype) o;
        return Objects.equals(id, filetype.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }    
}