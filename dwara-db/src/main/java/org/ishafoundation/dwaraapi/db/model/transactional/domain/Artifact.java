package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public class Artifact {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;

	@OneToOne
	private Artifactclass artifactclass;

	@Column(name="original_name", unique = true)
	private String originalName;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="sequence_code")
	private String sequenceCode;
	
	@Column(name="prev_sequence_code")
	private String prev_sequence_code;
	
	@Column(name="file_count")
	private int fileCount;

	@Column(name="total_size")
	private long totalSize;
	
	@Column(name="deleted")
	private boolean deleted;

	@Column(name="file_structure_md5")
	private String fileStructureMd5;

 	// Causes cyclic associations 
 	// Many subrequest could have happened on the same library. Like rerun etc., But this holds the most recent subrequest so that it can be queried easily
	@OneToOne
	@JoinColumn(name="q_latest_subrequest_id") 
	private Subrequest qLatestSubrequest;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonIgnore
	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	@JsonIgnore
	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}
	
	//@JsonIgnore
	public int getArtifactclassId() {
		return this.artifactclass.getId();
	}
	
	//@JsonIgnore
	public String getArtifactclassName() {
		return this.artifactclass.getName();
	}
	
	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSequenceCode() {
		return sequenceCode;
	}

	public void setSequenceCode(String sequenceCode) {
		this.sequenceCode = sequenceCode;
	}

	public String getPrev_sequence_code() {
		return prev_sequence_code;
	}

	public void setPrev_sequence_code(String prev_sequence_code) {
		this.prev_sequence_code = prev_sequence_code;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getFileStructureMd5() {
		return fileStructureMd5;
	}

	public void setFileStructureMd5(String fileStructureMd5) {
		this.fileStructureMd5 = fileStructureMd5;
	}
	
	@JsonIgnore
	public Subrequest getqLatestSubrequest() {
		return qLatestSubrequest;
	}

	@JsonIgnore
	public void setqLatestSubrequest(Subrequest qLatestSubrequest) {
		this.qLatestSubrequest = qLatestSubrequest;
	}	
	
	public int getqLatestSubrequestId() {
		return this.qLatestSubrequest.getId();
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact library = (Artifact) o;
        return Objects.equals(fileStructureMd5, library.fileStructureMd5);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileStructureMd5);
    }
}