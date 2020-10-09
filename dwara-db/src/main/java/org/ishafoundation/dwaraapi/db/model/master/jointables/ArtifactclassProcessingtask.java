package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ArtifactclassProcessingtaskKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;

//@Entity(name = "ArtifactclassProcessingtask")
@Table(name="artifactclass_processingtask")
public class ArtifactclassProcessingtask {

	@EmbeddedId
	private ArtifactclassProcessingtaskKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artifactclassId")
	private Artifactclass artifactclass;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("processingtaskId")
	private Processingtask processingtask;

//	@OneToOne(fetch = FetchType.LAZY)
//    private Artifactclass outputArtifactclass;
	
    @Column(name="pathname_regex")
    private String pathnameRegex;
    
	public ArtifactclassProcessingtask() {
		
	}

	public ArtifactclassProcessingtask(Artifactclass artifactclass, Processingtask processingtask) {
		this.artifactclass = artifactclass;
		this.processingtask = processingtask;
		this.id = new ArtifactclassProcessingtaskKey(artifactclass.getId(), processingtask.getId());
	}
	
    public ArtifactclassProcessingtaskKey getId() {
		return id;
	}

	public void setId(ArtifactclassProcessingtaskKey id) {
		this.id = id;
	}

	public Artifactclass getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(Artifactclass artifactclass) {
		this.artifactclass = artifactclass;
	}

	public Processingtask getProcessingtask() {
		return processingtask;
	}

	public void setProcessingtask(Processingtask processingtask) {
		this.processingtask = processingtask;
	}

    public String getPathnameRegex() {
		return pathnameRegex;
	}

	public void setPathnameRegex(String pathnameRegex) {
		this.pathnameRegex = pathnameRegex;
	}
	
//	public Artifactclass getOutputArtifactclass() {
//		return outputArtifactclass;
//	}
//
//	public void setOutputArtifactclass(Artifactclass outputArtifactclass) {
//		this.outputArtifactclass = outputArtifactclass;
//	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ArtifactclassProcessingtask that = (ArtifactclassProcessingtask) o;
        return Objects.equals(artifactclass, that.artifactclass) &&
               Objects.equals(processingtask, that.processingtask);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(artifactclass, processingtask);
    }

}