package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="t_file")
@SequenceGenerator(initialValue = 1, name = "t_file_sequence", allocationSize = 1)
public class TFile extends FileColumns{

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "t_file_sequence")
	@Column(name="id")
	private int id;
	
	@Column(name="artifact_id")
	private int artifactId;

	@Column(name="file_ref_id")
	private Integer fileRefId;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}

	public Integer getFileRefId() {
		return fileRefId;
	}

	public void setFileRefId(Integer fileRefId) {
		this.fileRefId = fileRefId;
	}
}