package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="processingtask")
public class Processingtask {

	@Id
	@Column(name="id")
	private Integer id; // Is Integer so compatibe with - field in library class (only applicable where source = false)
	
	@Column(name="name", unique=true)
	private String name;

	@Column(name="description")
	private String description;
	
	@Column(name="max_errors")
	private int maxErrors;

	// Many Task can share the same filetype...
	// Eg.,
	// Pubvideo - video
	// Privvideo - video
	@ManyToOne(fetch = FetchType.LAZY)
	private Filetype filetype;

	@OneToOne(fetch = FetchType.LAZY)
	private Application application;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
	}

	public Filetype getFiletype() {
		return filetype;
	}

	public void setFiletype(Filetype filetype) {
		this.filetype = filetype;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Processingtask task = (Processingtask) o;
        return Objects.equals(name, task.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}