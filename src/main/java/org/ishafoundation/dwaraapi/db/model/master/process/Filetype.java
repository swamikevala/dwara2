package org.ishafoundation.dwaraapi.db.model.master.process;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="filetype")
public class Filetype {

	@Id
	@Column(name="filetype_id")
	private int filetypeId;
	
	@Column(name="name")
	private String name;

	@Column(name="extensions")
	private String extensions;
	
	@Column(name="include_sidecar_files")
	private boolean includeSidecarFiles;
	
	@Column(name="sidecar_extensions")
	private String sidecarExtensions;
	
	public int getFiletypeId() {
		return filetypeId;
	}

	public void setFiletypeId(int filetypeId) {
		this.filetypeId = filetypeId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtensions() {
		return extensions;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}

	public boolean isIncludeSidecarFiles() {
		return includeSidecarFiles;
	}

	public void setIncludeSidecarFiles(boolean includeSidecarFiles) {
		this.includeSidecarFiles = includeSidecarFiles;
	}

	public String getSidecarExtensions() {
		return sidecarExtensions;
	}

	public void setSidecarExtensions(String sidecarExtensions) {
		this.sidecarExtensions = sidecarExtensions;
	}
	
	public String[] getExtensionsAsArray() {
		if(extensions != null) {
			return extensions.split(",");
		}
		return null;
	}
	
	public String[] getSidecarExtensionsAsArray() {
		if(sidecarExtensions != null) {
			return sidecarExtensions.split(",");
		}
		return null;
	}
}