package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;

@Entity
@Table(name="label")
public class Tag {
    @Id
	@Column(name="tag")
	String tag;

	@ManyToMany
	@JoinTable(
		name = "artifact_label",
		joinColumns = @JoinColumn(name = "tag"),
		inverseJoinColumns = @JoinColumn(name = "artifact_id")
	)
	Set<Artifact> artifacts;

	public Tag(){

	}

	public Tag(String value) {
		tag = value;
	}

	public Artifact getArtifactById(int artifactId) {
		if(artifacts != null) {
			for (Artifact artifact : artifacts) {
				if(artifact.getId() == artifactId)
					return artifact;
			}
		}
		return null;
	}

	public Artifact addArtifact(Artifact r) {
		if(artifacts == null)
			artifacts = new LinkedHashSet<Artifact>();
		artifacts.add(r);
		return r;
	}

	public void deleteArtifact(Artifact r) {
		if(r != null) {
			artifacts.remove(r);
		}
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Set<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Set<Artifact> artifacts) {
		this.artifacts = artifacts;
	}
}
