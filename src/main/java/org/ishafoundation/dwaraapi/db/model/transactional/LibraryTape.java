package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="library_tape")
public class LibraryTape {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="library_tape_id")
	private int libraryTapeId;
	
	@Column(name="library_id")
	private int libraryId;

	@Column(name="tape_id")
	private int tapeId;

	@Column(name="copy_number")
	private int copyNumber;

	@Column(name="encrypted")
	private boolean encrypted;

	@Column(name="block")
	private int block;
		
	public int getLibraryTapeId() {
		return libraryTapeId;
	}

	public void setLibraryTapeId(int libraryTapeId) {
		this.libraryTapeId = libraryTapeId;
	}
	
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}
	
	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}
	
	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

}