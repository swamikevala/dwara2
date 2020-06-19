package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.domain.FileVolumeKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/volumeguide/html_single/Hibernate_Volume_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "File2Volume")
@Table(name="file2_volume")
public class File2Volume extends FileVolume{

	@EmbeddedId
	private FileVolumeKey id;

//	@ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("file2Id")
//	private File2 file2;


	public File2Volume() {
		
	}
	
	public File2Volume(int fileId, Volume volume) {
		this.setVolume(volume);
		this.id = new FileVolumeKey(fileId, volume.getId());
	}
	
	@JsonIgnore
    public FileVolumeKey getId() {
		return id;
	}

	@JsonIgnore
	public void setId(FileVolumeKey id) {
		this.id = id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        File2Volume that = (File2Volume) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
    	 return Objects.hash(id);
    }

}