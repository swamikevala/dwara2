package org.ishafoundation.dwaraapi.db.model.transactional.jointables;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class FileVolumeColumns {
	
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
