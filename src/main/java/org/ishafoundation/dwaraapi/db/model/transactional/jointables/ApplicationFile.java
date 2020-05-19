package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ApplicationFileKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Application;
import org.ishafoundation.dwaraapi.db.model.transactional.File;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/fileguide/html_single/Hibernate_File_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "ApplicationFile")
@Table(name="application_file")
public class ApplicationFile {

	@EmbeddedId
	private ApplicationFileKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("applicationId")
	private Application application;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
	private File file;
	
	@Column(name="identifier")
	private String identifier;

	public ApplicationFile() {
		
	}
	
	public ApplicationFile(Application application, File file) {
		this.application = application;
		this.file = file;
		this.id = new ApplicationFileKey(application.getId(), file.getId());
	}
	
    public ApplicationFileKey getId() {
		return id;
	}

	public void setId(ApplicationFileKey id) {
		this.id = id;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ApplicationFile that = (ApplicationFile) o;
        return Objects.equals(application, that.application) &&
               Objects.equals(file, that.file);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(application, file);
    }
}