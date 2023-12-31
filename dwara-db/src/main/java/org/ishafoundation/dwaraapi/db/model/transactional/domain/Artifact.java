package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tag;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public class Artifact {
	
	public static final String TABLE_NAME_PREFIX = "artifact";
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "artifact_sequence")
	@Column(name="id")
	private int id;

	@OneToOne
	private Artifactclass artifactclass;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="sequence_code")
	private String sequenceCode;
	
	@Column(name="prev_sequence_code")
	private String prevSequenceCode;
	
	@Column(name="file_count")
	private int fileCount;

	@Column(name="total_size")
	private long totalSize;
	
	@Column(name="deleted")
	private boolean deleted;

	@Column(name="file_structure_md5")
	private String fileStructureMd5;

	@OneToOne
	@JoinColumn(name="write_request_id") 
	private Request writeRequest;	
	
 	// Causes cyclic associations 
 	// Many subrequest could have happened on the same library. Like rerun etc., But this holds the most recent subrequest so that it can be queried easily
	@OneToOne
	@JoinColumn(name="q_latest_request_id") 
	private Request qLatestRequest;

	@ManyToMany(mappedBy = "artifacts")
	Set<Tag> tags;

	public Artifact() {

	}

	public Artifact(int _id) {
		id = _id;
	}

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

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
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
	public Request getWriteRequest() {
		return writeRequest;
	}

	@JsonIgnore
	public void setWriteRequest(Request writeRequest) {
		this.writeRequest = writeRequest;
	}

	@JsonIgnore
	public Request getqLatestRequest() {
		return qLatestRequest;
	}

	@JsonIgnore
	public void setqLatestRequest(Request qLatestRequest) {
		this.qLatestRequest = qLatestRequest;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(Tag t){
		if(this.tags == null) {
			this.tags = new LinkedHashSet<Tag>();
		}
		this.tags.add(t);
	}

	public void deleteTag(Tag t) {
		if(this.tags != null) {
			this.tags.remove(t);
		}
	}
	


	/* @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact library = (Artifact) o;
        return Objects.equals(fileStructureMd5, library.fileStructureMd5);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileStructureMd5);
	} */
}