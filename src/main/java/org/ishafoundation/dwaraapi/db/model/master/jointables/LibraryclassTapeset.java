package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.LibraryclassTapesetKey;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.ishafoundation.dwaraapi.db.model.master.Task;

@Entity(name = "LibraryclassTapeset")
@Table(name="libraryclass_tapeset")
public class LibraryclassTapeset {

	@EmbeddedId
	private LibraryclassTapesetKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libraryclassId")
	private Libraryclass libraryclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tapesetId")
	private Tapeset tapeset;

	@Column(name="copy_number")
	private int copyNumber;

	//associated task that does the copy - one combination of LibraryclassTapeset to one task - so OnetoOne
	@OneToOne(fetch = FetchType.LAZY)
	private Task task;

	@Column(name="encrypted")
	private boolean encrypted;
	
	public LibraryclassTapeset() {
		
	}

	public LibraryclassTapeset(Libraryclass libraryclass, Tapeset tapeset) {
		this.libraryclass = libraryclass;
		this.tapeset = tapeset;
		this.id = new LibraryclassTapesetKey(libraryclass.getId(), tapeset.getId());
	}
	
    public LibraryclassTapesetKey getId() {
		return id;
	}

	public void setId(LibraryclassTapesetKey id) {
		this.id = id;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}

	public Tapeset getTapeset() {
		return tapeset;
	}

	public void setTapeset(Tapeset tapeset) {
		this.tapeset = tapeset;
	}

	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
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
 
        LibraryclassTapeset that = (LibraryclassTapeset) o;
        return Objects.equals(libraryclass, that.libraryclass) &&
               Objects.equals(tapeset, that.tapeset);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(libraryclass, tapeset);
    }

}