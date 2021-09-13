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

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;

@Entity
@Table(name="label")
public class Tag {
    @Id
	@Column(name="tag")
	String tag;

	@ManyToMany
	@JoinTable(
		name = "artifact1_label",
		joinColumns = @JoinColumn(name = "tag"),
		inverseJoinColumns = @JoinColumn(name = "artifact1_id")
	)
	Set<Artifact1> artifacts;

	public Tag(){

	}

	public Tag(String value) {
		tag = value;
	}

	public Artifact1 getArtifactById(int artifactId) {
		if(artifacts != null) {
			for (Artifact1 artifact : artifacts) {
				if(artifact.getId() == artifactId)
					return artifact;
			}
		}
		return null;
	}

	public Artifact1 addArtifact(Artifact1 r) {
		if(artifacts == null)
			artifacts = new LinkedHashSet<Artifact1>();
		artifacts.add(r);
		return r;
	}

	public void deleteArtifact(Artifact1 r) {
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

	public Set<Artifact1> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Set<Artifact1> artifacts) {
		this.artifacts = artifacts;
	}
}
