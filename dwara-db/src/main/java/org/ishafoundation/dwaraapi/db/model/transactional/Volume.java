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
import org.ishafoundation.dwaraapi.db.model.master.configuration.Copy;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Sequence;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
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
	
	@Column(name="uuid")
	private String uuid;
	
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private Volumetype type;
	
	@Enumerated(EnumType.STRING)
	@Column(name="storagetype")
	private Storagetype storagetype;
	
	//@Enumerated(EnumType.STRING)
	@Column(name="storagesubtype")
	private String storagesubtype;
	
	@Enumerated(EnumType.STRING)
	@Column(name="storagelevel")
	private Storagelevel storagelevel;

	@ManyToOne(fetch = FetchType.LAZY)
    private Copy copy;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private Volume groupRef;
	
	@OneToOne
	private Sequence sequence;
	
	@Enumerated(EnumType.STRING)
	@Column(name="checksumtype")
	private Checksumtype checksumtype;
	
	@Column(name="initialized_at")
	private LocalDateTime initializedAt;
	
	@Column(name="finalized")
	private boolean finalized;

	@Column(name="imported")
	private boolean imported;

	@Column(name="suspect")
	private Boolean suspect; // Not on VolumeGroups

	@Column(name="defective")
	private Boolean defective; // Not on VolumeGroups

	@OneToOne
	private Archiveformat archiveformat;
	
	@Column(name="capacity")
	private Long capacity;
	
	@OneToOne
	private Location location;
	
	@Type(type = "json")
	@Column(name="details", columnDefinition = "json")
	private VolumeDetails details;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Volumetype getType() {
		return type;
	}

	public void setType(Volumetype type) {
		this.type = type;
	}

	public Storagetype getStoragetype() {
		return storagetype;
	}

	public void setStoragetype(Storagetype storagetype) {
		this.storagetype = storagetype;
	}

	public String getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(String storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Storagelevel getStoragelevel() {
		return storagelevel;
	}

	public void setStoragelevel(Storagelevel storagelevel) {
		this.storagelevel = storagelevel;
	}

	public Copy getCopy() {
		return copy;
	}

	public void setCopy(Copy copy) {
		this.copy = copy;
	}

	public Volume getGroupRef() {
		return groupRef;
	}

	public void setGroupRef(Volume groupRef) {
		this.groupRef = groupRef;
	}
	
	public Checksumtype getChecksumtype() {
		return checksumtype;
	}

	public void setChecksumtype(Checksumtype checksumtype) {
		this.checksumtype = checksumtype;
	}

	public LocalDateTime getInitializedAt() {
		return initializedAt;
	}

	public void setInitializedAt(LocalDateTime initializedAt) {
		this.initializedAt = initializedAt;
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

	public Boolean getSuspect() {
		return suspect;
	}

	public void setSuspect(Boolean suspect) {
		this.suspect = suspect;
	}

	public Boolean getDefective() {
		return defective;
	}

	public void setDefective(Boolean defective) {
		this.defective = defective;
	}

	public Archiveformat getArchiveformat() {
		return archiveformat;
	}

	public void setArchiveformat(Archiveformat archiveformat) {
		this.archiveformat = archiveformat;
	}

	public Long getCapacity() {
		return capacity;
	}

	public void setCapacity(Long capacity) {
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