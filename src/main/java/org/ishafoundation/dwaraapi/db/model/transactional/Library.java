package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryProperty;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="library")
public class Library {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;

	@OneToOne
	private Libraryclass libraryclass;

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
	private double totalSize;
	
	@Column(name="deleted")
	private boolean deleted;

	@Column(name="file_structure_md5")
	private String fileStructureMd5;

	// Causes cyclic associations 
	// Many derived library reference the same parent library - Hence many to one
 	@ManyToOne
	@JoinColumn(name="library_ref_id")
	private Library libraryRef;
	
 	// Causes cyclic associations 
 	// Many subrequest could have happened on the same library. Like rerun etc., But this holds the most recent subrequest so that it can be queried easily
	@OneToOne
	@JoinColumn(name="q_latest_subrequest_id") 
	private Subrequest qLatestSubrequest;
	
    @OneToMany(mappedBy = "library",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryProperty> libraryProperty = new ArrayList<>();

    @OneToMany(mappedBy = "library",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryTape> libraryTape = new ArrayList<>();
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonIgnore
	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	@JsonIgnore
	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}
	
	//@JsonIgnore
	public int getLibraryclassId() {
		return this.libraryclass.getId();
	}
	
	//@JsonIgnore
	public String getLibraryclassName() {
		return this.libraryclass.getName();
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

	public double getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(double totalSize) {
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
	public Library getLibraryRef() {
		return libraryRef;
	}

	@JsonIgnore
	public void setLibraryRef(Library libraryRef) {
		this.libraryRef = libraryRef;
	}

	public int getLibraryRefId() {
		return this.libraryRef != null ? this.libraryRef.getId() : 0;
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

	@JsonIgnore
	public List<LibraryProperty> getLibraryProperty() {
		return libraryProperty;
	}
	
	@JsonIgnore
	public void setLibraryProperty(List<LibraryProperty> libraryProperty) {
		this.libraryProperty = libraryProperty;
	}
	
	@JsonIgnore
	public List<LibraryTape> getLibraryTape() {
		return libraryTape;
	}
	
	@JsonIgnore
	public void setLibraryTape(List<LibraryTape> libraryTape) {
		this.libraryTape = libraryTape;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return Objects.equals(fileStructureMd5, library.fileStructureMd5);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileStructureMd5);
    }
}