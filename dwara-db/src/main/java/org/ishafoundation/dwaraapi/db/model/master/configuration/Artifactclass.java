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

import org.hibernate.annotations.Type;
import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ActionArtifactclassUser;
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
	
	@Column(name="description")
	private String description;

	@Column(name="path_prefix")
	private String pathPrefix;	

	// unidirectional reference
	// Many artifactclasses can share the same sequence...
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequence;

	@Column(name="source")
	private Boolean source;
	
	// Many derived artifact classes like preview/mezz proxy can refer to the source artifactclass...
	@ManyToOne(fetch = FetchType.LAZY)
	private Artifactclass artifactclassRef;
	
	@Column(name="import_only")
	private Boolean importOnly;

	@Column(name="concurrent_volume_copies")
	private Boolean concurrentVolumeCopies;
	
	@Column(name="auto_ingest")
	private Boolean autoIngest;
	
	@Column(name="display_order")
	private Integer displayOrder;
	
	@Type(type = "json")
	@Column(name="config", columnDefinition = "json")
	private ArtifactclassConfig config;
	
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ArtifactclassDestination> artifactclassDestination = new ArrayList<>();
    
    @OneToMany(mappedBy = "artifactclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<ActionArtifactclassUser> artifactclassActionUser = new ArrayList<>();   

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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Artifactclass getArtifactclassRef() {
		return artifactclassRef;
	}

	public void setArtifactclassRef(Artifactclass artifactclassRef) {
		this.artifactclassRef = artifactclassRef;
	}

	public Boolean getImportOnly() {
		return importOnly;
	}

	public void setImportOnly(Boolean importOnly) {
		this.importOnly = importOnly;
	}

	public Boolean isConcurrentVolumeCopies() {
		return concurrentVolumeCopies;
	}

	public void setConcurrentVolumeCopies(Boolean concurrentVolumeCopies) {
		this.concurrentVolumeCopies = concurrentVolumeCopies;
	}

	public Boolean getAutoIngest() {
		return autoIngest;
	}

	public void setAutoIngest(Boolean autoIngest) {
		this.autoIngest = autoIngest;
	}

	public Boolean getSource() {
		return source;
	}
	
	public String getPathPrefixForArtifactclassUtil() {
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

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public ArtifactclassConfig getConfig() {
		return config;
	}

	public void setConfig(ArtifactclassConfig config) {
		this.config = config;
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
	public List<ActionArtifactclassUser> getArtifactclassActionUser() {
		return artifactclassActionUser;
	}

	@JsonIgnore
	public void setArtifactclassActionUser(List<ActionArtifactclassUser> artifactclassActionUser) {
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
	
	public String getCategory() {
		String category = "public";
		// TODO : should this be private1/2/3 
		if(getId().toLowerCase().contains("priv")) {
			category = "private";
		}
		return category;
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
    	ActionArtifactclassUser artifactclassActionUser = new ActionArtifactclassUser(action, this, user);
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