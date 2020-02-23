package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="file_tape")
public class FileTape {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="file_tape_id")
	private int fileTapeId;
	
	@Column(name="file_id")
	private int fileId;

	@Column(name="tape_id")
	private int tapeId;

	@Column(name="block")
	private int block;

	@Column(name="offset")
	private int offset;

	@Column(name="deleted")
	private boolean deleted;

		
	public int getFileTapeId() {
		return fileTapeId;
	}

	public void setFileTapeId(int fileTapeId) {
		this.fileTapeId = fileTapeId;
	}
	
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	
	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}
	
	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}