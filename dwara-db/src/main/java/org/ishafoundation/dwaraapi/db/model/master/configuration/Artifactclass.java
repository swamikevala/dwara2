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
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassDestination;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassVolume;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Artifactclass")
@Table(name="artifactclass")
public class Artifactclass implements Cacheable, Comparable<Artifactclass>{
	
	@Id
	@Column(name="id")
	private String id;
	
	@Column(name="name", unique = true)
	private String name;
	
	@Column(name="domain_id")
	private org.ishafoundation.dwaraapi.enumreferences.Domain domain;
	
	@Column(name="\"group\"")
	private boolean group;

	@ManyToOne(fetch = FetchType.LAZY)
	private Artifactclass groupRef;	

	@Column(name="path_prefix")
	private String pathPrefix;	

	// unidirectional reference
	// Many artifactclasses can share the same sequence...
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequence;

	@Column(name="source")
	private Boolean source;
	
//	@Column(name="preservation_version")
//	private boolean preservationVersion;

	@Column(name="concurrent_volume_copies")
	private Boolean concurrentVolumeCopies;
	
	@Column(name="display_order")
	private Integer displayOrder;
	
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassDestination> artifactclassDestination = new ArrayList<>();
    
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassActionUser> artifactclassActionUser = new ArrayList<>();   

    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassVolume> artifactclassVolume = new ArrayList<>();   

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public org.ishafoundation.dwaraapi.enumreferences.Domain getDomain() {
		return domain;
	}

	public void setDomain(org.ishafoundation.dwaraapi.enumreferences.Domain domain) {
		this.domain = domain;
	}
	
	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public Artifactclass getGroupRef() {
		return groupRef;
	}

	public void setGroupRef(Artifactclass groupRef) {
		this.groupRef = groupRef;
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

	public String getSequenceId() {
		return sequence.getId();
	}
	
	public Boolean isSource() {
		return source;
	}

	public void setSource(Boolean source) {
		this.source = source;
	}
	
	public boolean isConcurrentVolumeCopies() {
		return concurrentVolumeCopies;
	}

	public void setConcurrentVolumeCopies(boolean concurrentVolumeCopies) {
		this.concurrentVolumeCopies = concurrentVolumeCopies;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@JsonIgnore
	public List<ArtifactclassDestination> getArtifactclassDestination() {
		return artifactclassDestination;
	}

	@JsonIgnore
	public void setArtifactclassDestination(List<ArtifactclassDestination> artifactclassDestination) {
		this.artifactclassDestination = artifactclassDestination;
	}
	
	@JsonIgnore
	public List<ArtifactclassActionUser> getArtifactclassActionUser() {
		return artifactclassActionUser;
	}

	@JsonIgnore
	public void setArtifactclassActionUser(List<ArtifactclassActionUser> artifactclassActionUser) {
		this.artifactclassActionUser = artifactclassActionUser;
	}

	@JsonIgnore
	public List<ArtifactclassVolume> getArtifactclassVolume() {
		return artifactclassVolume;
	}

	@JsonIgnore
	public void setArtifactclassVolume(List<ArtifactclassVolume> artifactclassVolume) {
		this.artifactclassVolume = artifactclassVolume;
	}
	
	//@JsonIgnore
	public String getCategory() {
		String category = "public";
		// TODO : should this be private1/2/3 
		if(getId().toLowerCase().contains("priv")) {
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
	
    public void addDestinationpath(Destination destinationpath) {
    	// linking the join table entry to this owning object
    	ArtifactclassDestination artifactclassDestination = new ArtifactclassDestination(this, destinationpath);
    	this.artifactclassDestination.add(artifactclassDestination);

    	// inversing linking the join table entry to the target object
        destinationpath.getArtifactclassDestination().add(artifactclassDestination);
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
        return Objects.equals(id, obj.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

	@Override
	public int compareTo(Artifactclass artifactclass) {
		return this.getDisplayOrder().compareTo(artifactclass.getDisplayOrder());
	}
}