package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.enumreferences.SequenceType;


@Entity
@Table(name="sequence")
public class Sequence{// implements Cacheable{
	
	@Id
	@Column(name="id")
	private int id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private SequenceType type;
	
	@Column(name="\"group\"")
	private boolean group;
	
	@Column(name="starting_number")
	private Integer startingNumber;
	
	@Column(name="ending_number")
	private Integer endingNumber;
	
	@Column(name="current_number")
	private int currrentNumber;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequenceRef;

	@Column(name="prefix")
	private String prefix;

	@Column(name="artifact_extraction_regex")
	private String artifactExtractionRegex;

	@Column(name="artifact_keep_code")
	private Boolean artifactKeepCode;

		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public SequenceType getType() {
		return type;
	}

	public void setType(SequenceType type) {
		this.type = type;
	}

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public Integer getStartingNumber() {
		return startingNumber;
	}

	public void setStartingNumber(Integer startingNumber) {
		this.startingNumber = startingNumber;
	}
	
	public Integer getEndingNumber() {
		return endingNumber;
	}

	public void setEndingNumber(Integer endingNumber) {
		this.endingNumber = endingNumber;
	}

	public int getCurrrentNumber() {
		return currrentNumber;
	}

	public void setCurrrentNumber(int currrentNumber) {
		this.currrentNumber = currrentNumber;
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

	public Boolean isArtifactKeepCode() {
		return artifactKeepCode;
	}

	public void setArtifactKeepCode(Boolean artifactKeepCode) {
		this.artifactKeepCode = artifactKeepCode;
	}

	public void incrementCurrentNumber() {
		this.currrentNumber += 1;
	}
	
	public void decrementCurrentNumber() {
		this.currrentNumber -= 1;
	}
}