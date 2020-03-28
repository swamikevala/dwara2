package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ExtensionFiletypeKey;
import org.ishafoundation.dwaraapi.db.model.master.Extension;
import org.ishafoundation.dwaraapi.db.model.master.Filetype;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "ExtensionFiletype")
@Table(name="extension_filetype")
public class ExtensionFiletype {

	@EmbeddedId
	private ExtensionFiletypeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("extensionId")
	private Extension extension;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("filetypeId")
	private Filetype filetype;
	
	@Column(name="sidecar")
	private boolean sidecar;

	public ExtensionFiletype() {
		
	}
	
	public ExtensionFiletype(Extension extension, Filetype filetype) {
		this.extension = extension;
		this.filetype = filetype;
		this.id = new ExtensionFiletypeKey(extension.getId(), filetype.getId());
	}
	
    public ExtensionFiletypeKey getId() {
		return id;
	}

	public void setId(ExtensionFiletypeKey id) {
		this.id = id;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Filetype getFiletype() {
		return filetype;
	}

	public void setFiletype(Filetype filetype) {
		this.filetype = filetype;
	}

	public boolean isSidecar() {
		return sidecar;
	}

	public void setSidecar(boolean sidecar) {
		this.sidecar = sidecar;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ExtensionFiletype that = (ExtensionFiletype) o;
        return Objects.equals(extension, that.extension) &&
               Objects.equals(filetype, that.filetype);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(extension, filetype);
    }
}