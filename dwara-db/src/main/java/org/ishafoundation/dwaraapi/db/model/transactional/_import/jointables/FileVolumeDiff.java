package org.ishafoundation.dwaraapi.db.model.transactional._import.jointables;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.keys.FileVolumeKey;

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
@Table(name="file_volume_diff")
public class FileVolumeDiff {
	
	@EmbeddedId
	protected FileVolumeKey id;
	
	@Column(name="size")
	private long size;
	
	@Type(type="org.hibernate.type.BinaryType") 
	@Column(length=32, name="checksum")
	private byte[] checksum;
	
	public FileVolumeDiff() {
		
	}
	
	public FileVolumeDiff(int fileId, String volumeId) {
		this.id = new FileVolumeKey(fileId, volumeId);
	}

	public FileVolumeKey getId() {
		return id;
	}

	public void setId(FileVolumeKey id) {
		this.id = id;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}
}