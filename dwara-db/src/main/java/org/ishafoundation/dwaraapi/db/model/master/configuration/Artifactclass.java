package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassActionUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassDestinationpath;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Artifactclass")
@Table(name="artifactclass")
public class Artifactclass implements Cacheable{
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;
	
	@Column(name="uid", unique = true)
	private String uid;

	@Column(name="domain_id")
	private Integer domainId;
	
	@Column(name="path_prefix")
	private String pathPrefix;	

	// unidirectional reference
	// Many artifactclasses can share the same sequence...
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequence;

	@Column(name="source")
	private boolean source;
	
//	@Column(name="preservation_version")
//	private boolean preservationVersion;

	@Column(name="concurrent_volume_copies")
	private boolean concurrentVolumeCopies;
	
	@Column(name="display_order")
	private int displayOrder;
	
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassDestinationpath> artifactclassDestinationpath = new ArrayList<>();
    
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassActionUser> artifactclassActionUser = new ArrayList<>();   
    

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}
	
	/*Comment this out and expose only the needed fields...*/
	@JsonIgnore
	public Sequence getSequence() {
		return sequence;
	}

	@JsonIgnore
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public int getSequenceId() {
		return sequence.getId();
	}
	
	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}
	
	public boolean isConcurrentVolumeCopies() {
		return concurrentVolumeCopies;
	}

	public void setConcurrentVolumeCopies(boolean concurrentVolumeCopies) {
		this.concurrentVolumeCopies = concurrentVolumeCopies;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@JsonIgnore
	public List<ArtifactclassDestinationpath> getArtifactclassDestinationpath() {
		return artifactclassDestinationpath;
	}

	@JsonIgnore
	public void setArtifactclassDestinationpath(List<ArtifactclassDestinationpath> artifactclassDestinationpath) {
		this.artifactclassDestinationpath = artifactclassDestinationpath;
	}
	
	@JsonIgnore
	public List<ArtifactclassActionUser> getArtifactclassActionUser() {
		return artifactclassActionUser;
	}

	@JsonIgnore
	public void setArtifactclassActionUser(List<ArtifactclassActionUser> artifactclassActionUser) {
		this.artifactclassActionUser = artifactclassActionUser;
	}

	//@JsonIgnore
	public String getCategory() {
		String category = "public";
		// TODO : should this be private1/2/3 
		if(getName().toLowerCase().startsWith("private")) {
			category = "private";
		}
		return category;
	}

	//@JsonIgnore
	public String getPath() {
		String pathWithOutLibrary = null;
		if(isSource())
			pathWithOutLibrary = getPathPrefix();
		else
			pathWithOutLibrary = getPathPrefix() + java.io.File.separator + getCategory();

		return pathWithOutLibrary;
	}
	
    public void addDestinationpath(Destinationpath destinationpath) {
    	// linking the join table entry to this owning object
    	ArtifactclassDestinationpath artifactclassDestinationpath = new ArtifactclassDestinationpath(this, destinationpath);
    	this.artifactclassDestinationpath.add(artifactclassDestinationpath);

    	// inversing linking the join table entry to the target object
        destinationpath.getArtifactclassDestinationpath().add(artifactclassDestinationpath);
    }
     
    public void addActionUser(Action action, User user) {
    	// linking the join table entry to this owning object
    	ArtifactclassActionUser artifactclassActionUser = new ArtifactclassActionUser(this, action, user);
    	this.artifactclassActionUser.add(artifactclassActionUser);

    	// inversing linking the join table entry to the target object
    	action.getArtifactclassActionUser().add(artifactclassActionUser);
    	user.getArtifactclassActionUser().add(artifactclassActionUser);
    }
    
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifactclass obj = (Artifactclass) o;
        return Objects.equals(uid, obj.uid);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}