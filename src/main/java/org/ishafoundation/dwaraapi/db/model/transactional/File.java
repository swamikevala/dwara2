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

import org.ishafoundation.dwaraapi.db.model.master.Filetype;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ApplicationFile;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="file")
public class File {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Library library;
	
	@Column(name="pathname")
	private String pathname;

	@Column(name="crc")
	private String crc;

	@Column(name="size")
	private double size;

	@Column(name="deleted")
	private boolean deleted;

	@Column(name="external_id")
	private String externalId;

	@OneToOne
	@JoinColumn(name="file_ref_id")
	private File fileRef;

	@OneToOne
	private Filetype filetype;
    
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

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public File getFileRef() {
		return fileRef;
	}

	public void setFileRef(File fileRef) {
		this.fileRef = fileRef;
	}

	public Filetype getFiletype() {
		return filetype;
	}

	public void setFiletype(Filetype filetype) {
		this.filetype = filetype;
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