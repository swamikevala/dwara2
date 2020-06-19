package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;

//@Entity(name="Archive")
//@Table(name="archive")
public class Archive {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="archive_id", allocationSize = 1)
	@Column(name="id")
	private int id;

	@Column(name="artifact_id")
	private int artifactId; 
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Volume volume;
	
	@Column(name="encryption")
	private boolean encryption;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Archiveformat archiveformat;	

	@Column(name="volume_block")
	private Integer volumeBlock;
	
	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="archive_checksum")
	private long archiveChecksum;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public boolean isEncryption() {
		return encryption;
	}

	public void setEncryption(boolean encryption) {
		this.encryption = encryption;
	}

	public Archiveformat getArchiveformat() {
		return archiveformat;
	}

	public void setArchiveformat(Archiveformat archiveformat) {
		this.archiveformat = archiveformat;
	}

	public Integer getVolumeBlock() {
		return volumeBlock;
	}

	public void setVolumeBlock(Integer volumeBlock) {
		this.volumeBlock = volumeBlock;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public long getArchiveChecksum() {
		return archiveChecksum;
	}

	public void setArchiveChecksum(long archiveChecksum) {
		this.archiveChecksum = archiveChecksum;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Archive job = (Archive) o;
        return Objects.equals(id, job.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}