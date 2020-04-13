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

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ApplicationFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="application")
public class Application {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;
	
    @OneToMany(mappedBy = "application",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ApplicationFile> applicationFile = new ArrayList<>();
    
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
	
	@JsonIgnore
	public List<ApplicationFile> getApplicationFile() {
		return applicationFile;
	}

	@JsonIgnore
	public void setApplicationFile(List<ApplicationFile> applicationFile) {
		this.applicationFile = applicationFile;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application application = (Application) o;
        return Objects.equals(name, application.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
