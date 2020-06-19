package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;


@Entity
@Table(name="archiveformat")
public class Archiveformat implements Cacheable{
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@Column(name="blocksize")
	private int blocksize;
	
	@Column(name="restore_verify")
	private boolean restoreVerify;

	@Column(name="filesize_increase_rate")
	private Float filesizeIncreaseRate;
	
	@Column(name="filesize_increase_const")
	private Integer filesizeIncreaseConst;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
        return Objects.equals(name, obj.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}