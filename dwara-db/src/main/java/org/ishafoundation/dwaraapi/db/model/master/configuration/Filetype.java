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
	private Integer id; // is Integer and hence value can be null - "Copy Tasks" in task table has no relevance/meaning for filetype
	
	@Column(name="name", unique = true)
	private String name;

    @OneToMany(mappedBy = "filetype",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ExtensionFiletype> extensions = new ArrayList<>();
	
    public Filetype() {
    	
    }
    
    public Filetype(Integer id, String name) {
    	this.id = id;
    	this.name = name;
    }
    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Filetype filetype = (Filetype) o;
        return Objects.equals(name, filetype.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }    
}