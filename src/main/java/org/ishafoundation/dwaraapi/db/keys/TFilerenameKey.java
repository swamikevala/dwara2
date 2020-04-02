package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TFilerenameKey implements Serializable {
 
	private static final long serialVersionUID = 4331264468542278375L;

	@Column(name="source_path")
	private String sourcePath;
	
	@Column(name="old_filename")
	private String oldFilename;
	
    public TFilerenameKey() {}
    
    public TFilerenameKey(String sourcePath, String oldFilename) {
        this.sourcePath = sourcePath;
        this.oldFilename = oldFilename;
    }
 
    public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getOldFilename() {
		return oldFilename;
	}

	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TFilerenameKey that = (TFilerenameKey) o;
        return Objects.equals(sourcePath, that.sourcePath) &&
               Objects.equals(oldFilename, that.oldFilename);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(sourcePath, oldFilename);
    }
}