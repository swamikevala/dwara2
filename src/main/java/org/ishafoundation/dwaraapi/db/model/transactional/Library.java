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

	@Column(name="name")
	private String name;

	@Column(name="file_count")
	private int fileCount;

	@Column(name="total_size")
	private double totalSize;
	
	@Column(name="deleted")
	private boolean deleted;

	@Column(name="file_structure_md5")
	private String fileStructureMd5;

	/* leaves a cyclic association with itself and not able to drop the table  
	@ManyToOne
	@JoinColumn(name="library_id_ref")
	private Library libraryRef;
	*/
	@Column(name="library_id_ref") 
	private Integer library_id_ref;
	
	/* leaves a cyclic association with both subreq pointing to Library and viceversa and so just using it as an Integer to store the latest Id
	@OneToOne
	@JoinColumn(name="q_latest_subrequest_id") 
	private Subrequest qLatestSubrequest;
	*/
	
	@Column(name="q_latest_subrequest_id") 
	private int qLatestSubrequestId;
	
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

	//@JsonIgnore
	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	//@JsonIgnore
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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	/*
	public Library getLibraryRef() {
		return libraryRef;
	}

	public void setLibraryRef(Library libraryRef) {
		this.libraryRef = libraryRef;
	}

	
	public Subrequest getqLatestSubrequest() {
		return qLatestSubrequest;
	}

	public void setqLatestSubrequest(Subrequest qLatestSubrequest) {
		this.qLatestSubrequest = qLatestSubrequest;
	}	
	*/
	
	public Integer getLibrary_id_ref() {
		return library_id_ref;
	}

	public void setLibrary_id_ref(Integer library_id_ref) {
		this.library_id_ref = library_id_ref;
	}

	public int getqLatestSubrequestId() {
		return qLatestSubrequestId;
	}

	public void setqLatestSubrequestId(int qLatestSubrequestId) {
		this.qLatestSubrequestId = qLatestSubrequestId;
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