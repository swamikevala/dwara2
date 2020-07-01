package org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain;
		
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileVolumeFactory;
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
//@Table(name=File.TABLE_NAME_PREFIX + "2_volume")
@Table(name="file2_volume")
public class File2Volume extends FileVolume{
    static {
//    	DomainSpecificFileVolumeFactory.register(File.TABLE_NAME_PREFIX + "2" + "_volume", File2Volume.class);
    	DomainSpecificFileVolumeFactory.register(TABLE_NAME.replace("<<DOMAIN>>", "2"), File2Volume.class);
    }
//	@EmbeddedId
//	private FileVolumeKey id;
//
////	@ManyToOne(fetch = FetchType.LAZY)
////    @MapsId("file2Id")
////	private File2 file2;
//
//
	public File2Volume() {
		super();
	}
	
	public File2Volume(int fileId, Volume volume) {
		super(fileId, volume);
	}
//	
//	@JsonIgnore
//    public FileVolumeKey getId() {
//		return id;
//	}
//
//	@JsonIgnore
//	public void setId(FileVolumeKey id) {
//		this.id = id;
//	}
//
//	@Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
// 
//        if (o == null || getClass() != o.getClass())
//            return false;
// 
//        File2Volume that = (File2Volume) o;
//        return Objects.equals(id, that.id);
//    }
// 
//    @Override
//    public int hashCode() {
//    	 return Objects.hash(id);
//    }

}