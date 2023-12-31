package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionFiletype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Extension")
@Table(name="extension")
public class Extension implements Cacheable{
	
	@Id
	@Column(name="id")
	private String id;

	@Column(name="description")
	private String description;
	
	@Column(name="\"ignore\"")
	private Boolean ignore;
	
    @OneToMany(mappedBy = "extension",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ExtensionFiletype> filetypes = new ArrayList<>();

    public Extension() {
    }
 
    public Extension(String id, String description) {
    	this.id = id;
        this.description = description;
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

	public Boolean getIgnore() {
		return ignore;
	}

	public void setIgnore(Boolean ignore) {
		this.ignore = ignore;
	}

	@JsonIgnore
    public List<ExtensionFiletype> getFiletypes() {
		return filetypes;
	}

	@JsonIgnore
	public void setFiletypes(List<ExtensionFiletype> filetypes) {
		this.filetypes = filetypes;
	}

	public void addFiletype(Filetype filetype) {
        ExtensionFiletype extensionFiletype = new ExtensionFiletype(this, filetype);
        filetypes.add(extensionFiletype);
        filetype.getExtensions().add(extensionFiletype);
    }
    
    public void removeFiletype(Filetype filetype) {
        for (Iterator<ExtensionFiletype> iterator = filetypes.iterator();
             iterator.hasNext(); ) {
            ExtensionFiletype extensionFiletype = iterator.next();
 
            if (extensionFiletype.getExtension().equals(this) &&
                    extensionFiletype.getFiletype().equals(filetype)) {
                iterator.remove();
                extensionFiletype.getFiletype().getExtensions().remove(extensionFiletype);
                extensionFiletype.setFiletype(null);
                extensionFiletype.setExtension(null);
            }
        }
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Extension extension = (Extension) o;
        return Objects.equals(id, extension.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }	
	
}