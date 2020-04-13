package org.ishafoundation.dwaraapi.db.model.master;
		
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

import org.ishafoundation.dwaraapi.db.model.master.jointables.ExtensionTaskfiletype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Extension")
@Table(name="extension")
public class Extension {
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="description")
	private String description;
	
    @OneToMany(mappedBy = "extension",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ExtensionTaskfiletype> taskfiletypes = new ArrayList<>();

    public Extension() {
    }
 
    public Extension(int id, String name, String description) {
    	this.id = id;
        this.name = name;
        this.description = description;
    }
		
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonIgnore
    public List<ExtensionTaskfiletype> getTaskfiletypes() {
		return taskfiletypes;
	}

	@JsonIgnore
	public void setTaskfiletypes(List<ExtensionTaskfiletype> taskfiletypes) {
		this.taskfiletypes = taskfiletypes;
	}

	public void addTaskfiletype(Taskfiletype taskfiletype) {
        ExtensionTaskfiletype extensionTaskfiletype = new ExtensionTaskfiletype(this, taskfiletype);
        taskfiletypes.add(extensionTaskfiletype);
        taskfiletype.getExtensions().add(extensionTaskfiletype);
    }
    
    public void removeTaskfiletype(Taskfiletype taskfiletype) {
        for (Iterator<ExtensionTaskfiletype> iterator = taskfiletypes.iterator();
             iterator.hasNext(); ) {
            ExtensionTaskfiletype extensionTaskfiletype = iterator.next();
 
            if (extensionTaskfiletype.getExtension().equals(this) &&
                    extensionTaskfiletype.getTaskfiletype().equals(taskfiletype)) {
                iterator.remove();
                extensionTaskfiletype.getTaskfiletype().getExtensions().remove(extensionTaskfiletype);
                extensionTaskfiletype.setTaskfiletype(null);
                extensionTaskfiletype.setExtension(null);
            }
        }
    }
    


	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Extension extension = (Extension) o;
        return Objects.equals(name, extension.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }	
	
}