package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;

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
@MappedSuperclass
public class FileVolume {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;
		
	@Column(name="volume_block")
	private Integer volumeBlock;

	@Column(name="archive_block")
	private Integer archiveBlock;

	@Column(name="encrypted")
	private boolean encrypted;
	
	@Column(name="verified_at")
	private LocalDateTime verifiedAt;
	
	@Column(name="deleted")
	private boolean deleted;
	

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public Integer getVolumeBlock() {
		return volumeBlock;
	}

	public void setVolumeBlock(Integer volumeBlock) {
		this.volumeBlock = volumeBlock;
	}

	public Integer getArchiveBlock() {
		return archiveBlock;
	}

	public void setArchiveBlock(Integer archiveBlock) {
		this.archiveBlock = archiveBlock;
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
}