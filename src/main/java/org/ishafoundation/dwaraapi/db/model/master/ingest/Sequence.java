package org.ishafoundation.dwaraapi.db.model.master.ingest;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="sequence")
public class Sequence {

	@Id
	@Column(name="sequence_id")
	private int sequenceId;
	
	@Column(name="last_number")
	private int lastNumber;

	@Column(name="prefix")
	private String prefix;

	@Column(name="extraction_regex")
	private String extractionRegex;

	@Column(name="keep_extracted_code")
	private boolean keepExtractedCode;

		
	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public int getLastNumber() {
		return lastNumber;
	}

	public void setLastNumber(int lastNumber) {
		this.lastNumber = lastNumber;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getExtractionRegex() {
		return extractionRegex;
	}

	public void setExtractionRegex(String extractionRegex) {
		this.extractionRegex = extractionRegex;
	}
	
	public boolean isKeepExtractedCode() {
		return keepExtractedCode;
	}

	public void setKeepExtractedCode(boolean keepExtractedCode) {
		this.keepExtractedCode = keepExtractedCode;
	}

}