package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;

@MappedSuperclass
public class FileColumns {

	@Column(length=16384, name="pathname")
	private String pathname;
	
	@Type(type="org.hibernate.type.BinaryType") 
	@Column(length=20, name="pathname_checksum", unique=true)
	private byte[] pathnameChecksum;

	@Type(type="org.hibernate.type.BinaryType") 
	@Column(length=20, name="checksum")
	private byte[] checksum;

	@Column(name="size")
	private long size;

	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="directory")
	private boolean directory;

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
	
	public byte[] getPathnameChecksum() {
		return pathnameChecksum;
	}

	public void setPathnameChecksum(byte[] pathnameChecksum) {
		this.pathnameChecksum = pathnameChecksum;
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

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileColumns file = (FileColumns) o;
        return Objects.equals(pathname, file.pathname);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(pathname);
    }
}