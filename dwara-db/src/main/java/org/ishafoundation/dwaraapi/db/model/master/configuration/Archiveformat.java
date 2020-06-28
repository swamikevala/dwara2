package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="archiveformat")
public class Archiveformat{
	
	@Id
	@Column(name="id")
	private String id;

	@Column(name="description")
	private String description;
	
	@Column(name="blocksize")
	private int blocksize;
	
	@Column(name="restore_verify")
	private boolean restoreVerify;

	@Column(name="filesize_increase_rate")
	private Float filesizeIncreaseRate;
	
	@Column(name="filesize_increase_const")
	private Integer filesizeIncreaseConst;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

	public boolean isRestoreVerify() {
		return restoreVerify;
	}

	public void setRestoreVerify(boolean restoreVerify) {
		this.restoreVerify = restoreVerify;
	}

	public Float getFilesizeIncreaseRate() {
		return filesizeIncreaseRate;
	}

	public void setFilesizeIncreaseRate(Float filesizeIncreaseRate) {
		this.filesizeIncreaseRate = filesizeIncreaseRate;
	}

	public Integer getFilesizeIncreaseConst() {
		return filesizeIncreaseConst;
	}

	public void setFilesizeIncreaseConst(Integer filesizeIncreaseConst) {
		this.filesizeIncreaseConst = filesizeIncreaseConst;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Archiveformat obj = (Archiveformat) o;
        return Objects.equals(id, obj.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}