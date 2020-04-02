package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.FileTapeKey;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.File;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/tapeguide/html_single/Hibernate_Tape_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "FileTape")
@Table(name="file_tape")
public class FileTape {

	@EmbeddedId
	private FileTapeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
	private File file;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tapeId")
	private Tape tape;
	
	@Column(name="block")
	private int block;

	@Column(name="offset")
	private int offset;

	@Column(name="deleted")
	private boolean deleted;

	@Column(name="encrypted")
	private boolean encrypted;

	public FileTape() {
		
	}
	
	public FileTape(File file, Tape tape) {
		this.file = file;
		this.tape = tape;
		this.id = new FileTapeKey(file.getId(), tape.getId());
	}
	
	@JsonIgnore
    public FileTapeKey getId() {
		return id;
	}

	@JsonIgnore
	public void setId(FileTapeKey id) {
		this.id = id;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Tape getTape() {
		return tape;
	}

	public void setTape(Tape tape) {
		this.tape = tape;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        FileTape that = (FileTape) o;
        return Objects.equals(file, that.file) &&
               Objects.equals(tape, that.tape);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(file, tape);
    }
}