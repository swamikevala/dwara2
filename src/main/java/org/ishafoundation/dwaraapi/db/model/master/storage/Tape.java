package org.ishafoundation.dwaraapi.db.model.master.storage;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tape")
public class Tape {

	@Id
	@Column(name="tape_id")
	private int tapeId;
	
	@Column(name="tapeset_id")
	private int tapesetId;

	@Column(name="tapetype_id")
	private int tapetypeId;

	@Column(name="barcode")
	private String barcode;

	@Column(name="finalized")
	private boolean finalized;

	@Column(name="blocksize")
	private int blocksize;

		
	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}
	
	public int getTapesetId() {
		return tapesetId;
	}

	public void setTapesetId(int tapesetId) {
		this.tapesetId = tapesetId;
	}
	
	public int getTapetypeId() {
		return tapetypeId;
	}

	public void setTapetypeId(int tapetypeId) {
		this.tapetypeId = tapetypeId;
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

}