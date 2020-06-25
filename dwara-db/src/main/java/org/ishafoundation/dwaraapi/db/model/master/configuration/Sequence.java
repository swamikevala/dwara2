package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;


@Entity
@Table(name="sequence")
public class Sequence implements Cacheable{

	@Override
	public String getName() {
		// Just to make the class cacheable
		return null;
	}
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="barcode")
	private boolean barcode;
	
	// TODO - Not needed. any sequence_ref_id null is a group
	@Column(name="group_")
	private boolean group;
	
	@Column(name="last_number")
	private int lastNumber;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequenceRef;

	@Column(name="prefix")
	private String prefix;

	@Column(name="artifact_extraction_regex")
	private String artifactExtractionRegex;

	@Column(name="artifact_keep_code")
	private boolean artifactKeepCode;

		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isBarcode() {
		return barcode;
	}

	public void setBarcode(boolean barcode) {
		this.barcode = barcode;
	}

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public int getLastNumber() {
		return lastNumber;
	}

	public void setLastNumber(int lastNumber) {
		this.lastNumber = lastNumber;
	}
	
	public Sequence getSequenceRef() {
		return sequenceRef;
	}

	public void setSequenceRef(Sequence sequenceRef) {
		this.sequenceRef = sequenceRef;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getArtifactExtractionRegex() {
		return artifactExtractionRegex;
	}

	public void setArtifactExtractionRegex(String artifactExtractionRegex) {
		this.artifactExtractionRegex = artifactExtractionRegex;
	}

	public boolean isArtifactKeepCode() {
		return artifactKeepCode;
	}

	public void setArtifactKeepCode(boolean artifactKeepCode) {
		this.artifactKeepCode = artifactKeepCode;
	}

	public void incrementLastNumber() {
		this.lastNumber += 1;
	}
	
	public void decrementLastNumber() {
		this.lastNumber -= 1;
	}
}