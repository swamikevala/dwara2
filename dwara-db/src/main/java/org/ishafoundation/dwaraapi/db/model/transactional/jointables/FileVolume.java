package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
//import javax.persistence.Inheritanc;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.FileVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name="file_volume")
public class FileVolume {
	
	public static final String TABLE_NAME = File.TABLE_NAME_PREFIX +"<<DOMAIN>>_volume";
	
	@EmbeddedId
	protected FileVolumeKey id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;
		
	@Column(name="volume_block")
	private Integer volumeBlock; // volumeBlockStart

	@Column(name="archive_block")
	private Long archiveBlock; // archiveBlockStart

	@Column(name="header_blocks")
	private Integer headerBlocks; // no. of HeaderBlocks consumed by the file...
	
	@Column(name="encrypted")
	private boolean encrypted;
	
	@Column(name="verified_at")
	private LocalDateTime verifiedAt;
	
	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="hardlink_file_id")
	private Integer hardlinkFileId;
	
	public FileVolume() {
		
	}
	
	public FileVolume(int fileId, Volume volume) {
		this.volume = volume;
		this.id = new FileVolumeKey(fileId, volume.getId());
	}

	public FileVolumeKey getId() {
		return id;
	}

	public void setId(FileVolumeKey id) {
		this.id = id;
	}

	public Volume getVolume() {
		return volume;
	}

	public Integer getVolumeBlock() {
		return volumeBlock;
	}

	public void setVolumeBlock(Integer volumeBlock) {
		this.volumeBlock = volumeBlock;
	}

	public Long getArchiveBlock() {
		return archiveBlock;
	}

	public void setArchiveBlock(Long archiveBlock) {
		this.archiveBlock = archiveBlock;
	}

	public Integer getHeaderBlocks() {
		return headerBlocks;
	}

	public void setHeaderBlocks(Integer headerBlocks) {
		this.headerBlocks = headerBlocks;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public LocalDateTime getVerifiedAt() {
		return verifiedAt;
	}

	public void setVerifiedAt(LocalDateTime verifiedAt) {
		this.verifiedAt = verifiedAt;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getHardlinkFileId() {
		return hardlinkFileId;
	}

	public void setHardlinkFileId(Integer hardlinkFileId) {
		this.hardlinkFileId = hardlinkFileId;
	}
}