package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="tape")
public class Tape {

	@Id
	@Column(name="id")
	private int id;
	
	// Many tapes in one tapeset
	// unidirectional reference is enough
	@ManyToOne
	private Tapeset tapeset;

	// Many tapes of one tapetype
	// unidirectional reference is enough
	@ManyToOne
	private Tapetype tapetype;

	@Column(name="barcode")
	private String barcode;

	@Column(name="finalized")
	private boolean finalized;

	@Column(name="blocksize")
	private int blocksize;
	
    @OneToMany(mappedBy = "tape",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<FileTape> fileTape = new ArrayList<>();

    @OneToMany(mappedBy = "tape",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryTape> libraryTape = new ArrayList<>();
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Tapeset getTapeset() {
		return tapeset;
	}

	public void setTapeset(Tapeset tapeset) {
		this.tapeset = tapeset;
	}

	public Tapetype getTapetype() {
		return tapetype;
	}

	public void setTapetype(Tapetype tapetype) {
		this.tapetype = tapetype;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
	
	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
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
        Tape tape = (Tape) o;
        return Objects.equals(barcode, tape.barcode);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(barcode);
    }

}