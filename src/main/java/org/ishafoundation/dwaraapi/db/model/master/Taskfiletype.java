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

import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionTaskfiletype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Taskfiletype")
@Table(name="taskfiletype")
public class Taskfiletype {

	@Id
	@Column(name="id")
	private Integer id; // is Integer and hence value can be null - "Copy Tasks" in task table has no relevance/meaning for filetype
	
	@Column(name="name")
	private String name;

    @OneToMany(mappedBy = "taskfiletype",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ExtensionTaskfiletype> extensions = new ArrayList<>();
	
    public Taskfiletype() {
    	
    }
    
    public Taskfiletype(Integer id, String name) {
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
	public List<ExtensionTaskfiletype> getExtensions() {
		return extensions;
	}

	@JsonIgnore
	public void setExtensions(List<ExtensionTaskfiletype> extensions) {
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
 
        Taskfiletype filetype = (Taskfiletype) o;
        return Objects.equals(name, filetype.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }    
}