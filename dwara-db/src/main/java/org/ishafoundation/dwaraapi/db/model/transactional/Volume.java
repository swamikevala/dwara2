package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.json.VolumeDetails;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.enumreferences.Storagelevel;
import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.ishafoundation.dwaraapi.enumreferences.Volumetype;


@Entity
@Table(name="volume")
public class Volume {

	// TODO Check this out...
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="uid", unique = true)
	private String uid;
	
	@Column(name="volumetype_id")
	private Volumetype volumetype;
	
	@Column(name="storagetype_id")
	private Storagetype storagetype;// points to enumreference
	
	@Column(name="storagelevel_id")
	private Storagelevel storagelevel;

	@ManyToOne(fetch = FetchType.LAZY)
    private Volume volumeRef;
	
	@Column(name="checksumtype_id")
	private Checksumtype checksumtype;
	
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
	
	@Lob
	@Column(name="details")
	//@Convert(converter = SubrequestDetailsAttributeConverter.class)
	private VolumeDetails details;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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
        return Objects.equals(uid, volume.uid);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

}