package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.enumreferences.Storagesubtype;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;

import com.vladmihalcea.hibernate.type.json.JsonStringType;

// Reference - https://art.iyc.ishafoundation.org/x/VJCv
	
@Entity
@Table(name="volume")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class Volume {

	@Id
	@Column(name="id")
	private String id; // Holds barcode for tapes and disks, bucket name for cloud, barcode prefix for volume groups
	
	@Enumerated(EnumType.STRING)
	@Column(name="volumetype")
	private Volumetype volumetype;
	
	@Enumerated(EnumType.STRING)
	@Column(name="storagetype")
	private Storagetype storagetype;
	
	//@Enumerated(EnumType.STRING)
	@Column(name="storagesubtype")
	private Storagesubtype storagesubtype;
	
	@Enumerated(EnumType.STRING)
	@Column(name="storagelevel")
	private Storagelevel storagelevel;

	@ManyToOne(fetch = FetchType.LAZY)
    private Volume volumeRef;
	
	@Enumerated(EnumType.STRING)
	@Column(name="checksumtype")
	private Checksumtype checksumtype;
	
	@Column(name="formatted_at")
	private LocalDateTime formattedAt;
	
	@Column(name="finalized")
	private boolean finalized;

	@Column(name="imported")
	private boolean imported;
	
	@OneToOne
	private Archiveformat archiveformat;
	
	@Column(name="capacity")
	private long capacity;
	
	@OneToOne
	private Location location;
	
	@OneToOne
	private Sequence sequence;
	
	@Type(type = "json")
	@Column(name="details", columnDefinition = "json")
	private VolumeDetails details;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Volumetype getVolumetype() {
		return volumetype;
	}

	public void setVolumetype(Volumetype volumetype) {
		this.volumetype = volumetype;
	}

	public Storagetype getStoragetype() {
		return storagetype;
	}

	public void setStoragetype(Storagetype storagetype) {
		this.storagetype = storagetype;
	}

	public Storagesubtype getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(Storagesubtype storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Storagelevel getStoragelevel() {
		return storagelevel;
	}

	public void setStoragelevel(Storagelevel storagelevel) {
		this.storagelevel = storagelevel;
	}

	public Volume getVolumeRef() {
		return volumeRef;
	}

	public void setVolumeRef(Volume volumeRef) {
		this.volumeRef = volumeRef;
	}
	
	public Checksumtype getChecksumtype() {
		return checksumtype;
	}

	public void setChecksumtype(Checksumtype checksumtype) {
		this.checksumtype = checksumtype;
	}

	public LocalDateTime getFormattedAt() {
		return formattedAt;
	}

	public void setFormattedAt(LocalDateTime formattedAt) {
		this.formattedAt = formattedAt;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public Archiveformat getArchiveformat() {
		return archiveformat;
	}

	public void setArchiveformat(Archiveformat archiveformat) {
		this.archiveformat = archiveformat;
	}

	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public VolumeDetails getDetails() {
		return details;
	}

	public void setDetails(VolumeDetails details) {
		this.details = details;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volume volume = (Volume) o;
        return Objects.equals(id, volume.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}