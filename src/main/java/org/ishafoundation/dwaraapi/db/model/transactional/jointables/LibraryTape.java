package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryTapeKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;

/*
 * 
 * References - 
 * 1) "Bidirectional many-to-many with a link entity" - https://docs.jboss.org/hibernate/orm/5.3/tapeguide/html_single/Hibernate_Tape_Guide.html#associations-many-to-many
 * 2) https://vladmihalcea.com/the-best-way-to-map-a-many-to-many-association-with-extra-columns-when-using-jpa-and-hibernate/
 * 3) https://www.baeldung.com/jpa-many-to-many
 * 
 * 
*/
@Entity(name = "LibraryTape")
@Table(name="library_tape")
public class LibraryTape {

	@EmbeddedId
	private LibraryTapeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryId")
	private Library library;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tapeId")
	private Tape tape;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "q_libraryclass_id")
	private Libraryclass libraryclass;
	
	@Column(name="block")
	private int block;

	@Column(name="encrypted")
	private boolean encrypted;

	public LibraryTape() {
		
	}
	
	public LibraryTape(Library library, Tape tape) {
		this.library = library;
		this.tape = tape;
		this.id = new LibraryTapeKey(library.getId(), tape.getId());
	}
	
    public LibraryTapeKey getId() {
		return id;
	}

	public void setId(LibraryTapeKey id) {
		this.id = id;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public Tape getTape() {
		return tape;
	}

	public void setTape(Tape tape) {
		this.tape = tape;
	}
	
	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}
	
	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
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
 
        LibraryTape that = (LibraryTape) o;
        return Objects.equals(library, that.library) &&
               Objects.equals(tape, that.tape);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(library, tape);
    }
}