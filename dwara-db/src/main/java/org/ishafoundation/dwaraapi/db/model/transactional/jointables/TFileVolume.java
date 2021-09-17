package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;
import org.ishafoundation.dwaraapi.db.keys.FileVolumeKey;
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
@Table(name="t_file_volume")
public class TFileVolume extends FileVolumeColumns{
	@EmbeddedId
	protected FileVolumeKey id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volumeId")
	private Volume volume;
	
	
	public TFileVolume() {
		super();
	}
	
	
	public TFileVolume(int tfileId, Volume volume) {
		this.volume = volume;
		this.id = new FileVolumeKey(tfileId, volume.getId());
	}

	public FileVolumeKey getId() {
		return id;
	}
}