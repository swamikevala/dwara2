package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="processingtask")
public class Processingtask{

	@Id
	@Column(name="id")
	private String id;

	@Column(name="description")
	private String description;
	
	@Column(name="max_errors")
	private int maxErrors;

	// Many Task can share the same filetype...
	// Eg.,
	// Pubvideo - video
	// Privvideo - video
//	@ManyToOne(fetch = FetchType.LAZY)
//	private Filetype filetype;
	@Column(name="filetype_id")
	private String filetypeId;
	
	@Column(name="output_artifactclass_suffix")
	private String outputArtifactclassSuffix;
	
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

	public int getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
	}

	public String getFiletypeId() {
		return filetypeId;
	}

	public void setFiletypeId(String filetypeId) {
		this.filetypeId = filetypeId;
	}
	
    public String getOutputArtifactclassSuffix() {
		return outputArtifactclassSuffix;
	}

	public void setOutputArtifactclassSuffix(String outputArtifactclassSuffix) {
		this.outputArtifactclassSuffix = outputArtifactclassSuffix;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        Processingtask task = (Processingtask) o;
        return Objects.equals(id, task.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}