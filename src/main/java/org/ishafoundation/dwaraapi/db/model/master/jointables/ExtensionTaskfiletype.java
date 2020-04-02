package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ExtensionTaskfiletypeKey;
import org.ishafoundation.dwaraapi.db.model.master.Extension;
import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "ExtensionTaskfiletype")
@Table(name="extension_taskfiletype")
public class ExtensionTaskfiletype {

	@EmbeddedId
	private ExtensionTaskfiletypeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("extensionId")
	private Extension extension;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskfiletypeId")
	private Taskfiletype taskfiletype;
	
	@Column(name="sidecar")
	private boolean sidecar;

	public ExtensionTaskfiletype() {
		
	}
	
	public ExtensionTaskfiletype(Extension extension, Taskfiletype taskfiletype) {
		this.extension = extension;
		this.taskfiletype = taskfiletype;
		this.id = new ExtensionTaskfiletypeKey(extension.getId(), taskfiletype.getId());
	}
	
    public ExtensionTaskfiletypeKey getId() {
		return id;
	}

	public void setId(ExtensionTaskfiletypeKey id) {
		this.id = id;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Taskfiletype getTaskfiletype() {
		return taskfiletype;
	}

	public void setTaskfiletype(Taskfiletype taskfiletype) {
		this.taskfiletype = taskfiletype;
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
 
        ExtensionTaskfiletype that = (ExtensionTaskfiletype) o;
        return Objects.equals(extension, that.extension) &&
               Objects.equals(taskfiletype, that.taskfiletype);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(extension, taskfiletype);
    }
}