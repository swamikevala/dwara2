package org.ishafoundation.dwaraapi.process.request;

public class File {

	private int id;
	
	private String pathname;

	private byte[] checksum;

	private long size;

	private boolean deleted;
	
	private File fileRef;

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

	public File getFileRef() {
		return fileRef;
	}

	public void setFileRef(File fileRef) {
		this.fileRef = fileRef;
	}
}