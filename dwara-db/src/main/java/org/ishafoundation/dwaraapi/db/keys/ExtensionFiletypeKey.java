package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ExtensionFiletypeKey implements Serializable {
 
	private static final long serialVersionUID = -620517854806310403L;

	@Column(name = "extension_id")
    private int extensionId;
 
    @Column(name = "filetype_id")
    private int filetypeId;
 
    public ExtensionFiletypeKey() {}
    
    public ExtensionFiletypeKey(
        int extensionId,
        int filetypeId) {
        this.extensionId = extensionId;
        this.filetypeId = filetypeId;
    }
 
    public int getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(int extensionId) {
		this.extensionId = extensionId;
	}

	public int getFiletypeId() {
		return filetypeId;
	}

	public void setFiletypeId(int filetypeId) {
		this.filetypeId = filetypeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ExtensionFiletypeKey that = (ExtensionFiletypeKey) o;
        return Objects.equals(extensionId, that.extensionId) &&
               Objects.equals(filetypeId, that.filetypeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(extensionId, filetypeId);
    }
}