package org.ishafoundation.dwaraapi.db.model.transactional.domain;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class File {

	public static final String TABLE_NAME_PREFIX = "file";
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "file_sequence")
	@Column(name="id")
	private int id;
	
	@Lob
	@Column(name="pathname", unique = true)
	private String pathname;

	@Column(name="checksum")
	private byte[] checksum;

	@Column(name="size")
	private long size;

	@Column(name="deleted")
	private boolean deleted;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(pathname, file.pathname);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(pathname);
    }
}