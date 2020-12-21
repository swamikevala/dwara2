package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.enumreferences.SequenceType;


@Entity
@Table(name="sequence")
public class Sequence implements Cacheable{
	
	@Id
	@Column(name="id")
	private String id;
	
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
	private Integer currrentNumber;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequenceRef;

	@Column(name="prefix")
	private String prefix;

	@Column(name="code_regex")
	private String codeRegex;

	@Column(name="number_regex")
	private String numberRegex;
	
	@Column(name="force_match")
	private Boolean forceMatch;
	
	@Column(name="keep_code")
	private Boolean keepCode;

	@Column(name="replace_code")
	private Boolean replaceCode;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public Integer getCurrrentNumber() {
		return currrentNumber;
	}

	public void setCurrrentNumber(Integer currrentNumber) {
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

	public String getCodeRegex() {
		return codeRegex;
	}

	public void setCodeRegex(String codeRegex) {
		this.codeRegex = codeRegex;
	}

	public String getNumberRegex() {
		return numberRegex;
	}

	public void setNumberRegex(String numberRegex) {
		this.numberRegex = numberRegex;
	}

	public Boolean getForceMatch() {
		return forceMatch;
	}

	public void setForceMatch(Boolean forceMatch) {
		this.forceMatch = forceMatch;
	}

	public Boolean isKeepCode() {
		return keepCode;
	}

	public void setKeepCode(Boolean keepCode) {
		this.keepCode = keepCode;
	}
	
	public Boolean isReplaceCode() {
		return replaceCode;
	}

	public void setReplaceCode(Boolean replaceCode) {
		this.replaceCode = replaceCode;
	}

	public Integer incrementCurrentNumber() {
		if(currrentNumber != null)
			this.currrentNumber += 1;
		return currrentNumber;
	}
	
	public Integer decrementCurrentNumber() {
		if(currrentNumber != null)
			this.currrentNumber -= 1;
		return currrentNumber;
	}
}