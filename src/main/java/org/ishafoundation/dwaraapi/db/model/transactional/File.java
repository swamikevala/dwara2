package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ApplicationFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="file")
public class File {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="file_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	// Many files from the same library
	@ManyToOne(fetch = FetchType.LAZY)
	private Library library;
	
	@Column(name="pathname", unique = true)
	private String pathname;

	@Column(name="crc")
	private String crc;

	@Column(name="size")
	private double size;

	@Column(name="deleted")
	private boolean deleted;

	@OneToOne
	@JoinColumn(name="file_ref_id")
	private File fileRef;

	@OneToMany(mappedBy = "file",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ApplicationFile> applicationFile = new ArrayList<>();
    
    @OneToMany(mappedBy = "file",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<FileTape> fileTape = new ArrayList<>();

    @OneToMany(mappedBy = "file",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<TFileJob> tFileJob = new ArrayList<>();
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public String getCrc() {
		return crc;
	}

	public void setCrc(String crc) {
		this.crc = crc;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public File getFileRef() {
		return fileRef;
	}

	public void setFileRef(File fileRef) {
		this.fileRef = fileRef;
	}
	
	@JsonIgnore
	public List<ApplicationFile> getApplicationFile() {
		return applicationFile;
	}

	@JsonIgnore
	public void setApplicationFile(List<ApplicationFile> applicationFile) {
		this.applicationFile = applicationFile;
	}

	@JsonIgnore
	public List<FileTape> getFileTape() {
		return fileTape;
	}

	@JsonIgnore
	public void setFileTape(List<FileTape> fileTape) {
		this.fileTape = fileTape;
	}
	
	@JsonIgnore
	public List<TFileJob> gettFileJob() {
		return tFileJob;
	}

	@JsonIgnore
	public void settFileJob(List<TFileJob> tFileJob) {
		this.tFileJob = tFileJob;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(pathname, file.pathname);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(pathname);
    }
}