package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.enumreferences.DiffValues;

@MappedSuperclass
public class FileColumns {

	@Column(length=4096, name="pathname")
	private String pathname;
	
	@Type(type="org.hibernate.type.BinaryType") 
	@Column(length=20, name="pathname_checksum", unique=true)
	private byte[] pathnameChecksum;

	@Type(type="org.hibernate.type.BinaryType") 
	@Column(length=32, name="checksum")
	private byte[] checksum;

	@Column(name="size")
	private long size;

	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="directory")
	private boolean directory;
	
	// type of the file - hardlink/symlink/sparse file etc.,
//	@Enumerated(EnumType.STRING)
//	@Column(name="type") 
//	private FileType type;
	
	@Column(name="symlink_file_id")
	private Integer symlinkFileId;
	
	@Column(length=4096, name="symlink_path") 
	private String symlinkPath;
	
	@Column(name="bad")
	private Boolean bad= false;
	
	@Lob
	@Column( name ="reason") 
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name="diff", length=1)
	private DiffValues diff;
	
	
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

//	public FileType getType() {
//		return type;
//	}
//
//	public void setType(FileType type) {
//		this.type = type;
//	}

	public Integer getSymlinkFileId() {
		return symlinkFileId;
	}

	public void setSymlinkFileId(Integer symlinkFileId) {
		this.symlinkFileId = symlinkFileId;
	}

	public String getSymlinkPath() {
		return symlinkPath;
	}

	public void setSymlinkPath(String symlinkPath) {
		this.symlinkPath = symlinkPath;
	}

	public Boolean getBad() {
		return bad;
	}

	public void setBad(Boolean bad) {
		this.bad = bad;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public DiffValues getDiff() {
		return diff;
	}

	public void setDiff(DiffValues diff) {
		this.diff = diff;
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