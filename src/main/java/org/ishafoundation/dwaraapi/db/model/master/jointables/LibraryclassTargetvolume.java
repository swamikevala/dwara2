package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassTargetvolumeKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;

@Entity(name = "LibraryclassTargetvolume")
@Table(name="libraryclass_targetvolume")
public class LibraryclassTargetvolume {

	@EmbeddedId
	private LibraryclassTargetvolumeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryclassId")
    Libraryclass libraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("targetvolumeId")
    Targetvolume targetvolume;

	public LibraryclassTargetvolume() {
		
	}

	public LibraryclassTargetvolume(Libraryclass libraryclass, Targetvolume targetvolume) {
		this.libraryclass = libraryclass;
		this.targetvolume = targetvolume;
		this.id = new LibraryclassTargetvolumeKey(libraryclass.getId(), targetvolume.getId());
	}
	
    public LibraryclassTargetvolumeKey getId() {
		return id;
	}

	public void setId(LibraryclassTargetvolumeKey id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}

	public Targetvolume getTargetvolume() {
		return targetvolume;
	}

	public void setTargetvolume(Targetvolume targetvolume) {
		this.targetvolume = targetvolume;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        LibraryclassTargetvolume that = (LibraryclassTargetvolume) o;
        return Objects.equals(libraryclass, that.libraryclass) &&
               Objects.equals(targetvolume, that.targetvolume);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclass, targetvolume);
    }

}