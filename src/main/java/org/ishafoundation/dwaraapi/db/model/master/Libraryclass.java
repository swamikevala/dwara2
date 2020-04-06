package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassProperty;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassRequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;
import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTargetvolume;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity(name="Libraryclass")
@Table(name="libraryclass")
public class Libraryclass {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="path_prefix")
	private String pathPrefix;	

	// unidirectional reference
	// Many libraryclasses can share the same sequence...
	@ManyToOne(fetch = FetchType.LAZY)
    private Sequence sequence;

	@Column(name="source")
	private boolean source;

	// unidirectional reference
	// Many libraryclasses can share the same filetype...
	// Eg.,
	// Pubvideo - video
	// Privvideo - video
	@ManyToOne(fetch = FetchType.LAZY)
	private Taskfiletype taskfiletype;

	// unidirectional reference
	// task resposible for generating the library class (only applicable where source = false)
	// Can one task like proxy gen - can generate multiple library classes like mezz and preview - No tasks are separate now...
	// One libraryclass to one task only
	@OneToOne(optional = true)
	@JoinColumn(name="generator_task_id")
	private Task generatorTask;


	@Column(name="concurrent_copies")
	private boolean concurrentCopies;
	
	// Unidirectional is enough... 
	// Many libraryclasses can use the same default taskset
	@ManyToOne(fetch = FetchType.LAZY)
	private Taskset taskset;	
	
	@Column(name="display_order")
	private int displayOrder;
	
    @OneToMany(mappedBy = "libraryclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassTargetvolume> libraryclassTargetvolume = new ArrayList<>();

    @OneToMany(mappedBy = "libraryclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassTapeset> libraryclassTapeset = new ArrayList<>(); 
    
    @OneToMany(mappedBy = "libraryclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassProperty> libraryclassProperty = new ArrayList<>();     
    
    @OneToMany(mappedBy = "libraryclass",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<LibraryclassRequesttypeUser> libraryclassRequesttypeUser = new ArrayList<>();   
    
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
	
	@JsonIgnore
	public Taskfiletype getTaskfiletype() {
		return taskfiletype;
	}

	@JsonIgnore
	public void setTaskfiletype(Taskfiletype taskfiletype) {
		this.taskfiletype = taskfiletype;
	}

	public int getTaskfiletypeId() {
		return taskfiletype.getId();
	}
	/*
	@JsonIgnore
	public Task getTask() {
		return task;
	}

	@JsonIgnore
	public void setTask(Task task) {
		this.task = task;
	}

	public Integer getTaskId() {
		Integer taskId = null;
		if(task != null)
			taskId = task.getId();
		return taskId;
	}*/

	
	@JsonIgnore
	public Task getGeneratorTask() {
		return generatorTask;
	}

	@JsonIgnore
	public void setGeneratorTask(Task generatorTask) {
		this.generatorTask = generatorTask;
	}
	
	/*
	public Integer getGeneratorTaskId() {
		Integer generatorTaskId = null;
		if(generatorTask != null)
			generatorTaskId = generatorTask.getId();
		return generatorTaskId;
	}*/
	
	public boolean isConcurrentCopies() {
		return concurrentCopies;
	}

	public void setConcurrentCopies(boolean concurrentCopies) {
		this.concurrentCopies = concurrentCopies;
	}

	@JsonIgnore
	public Taskset getTaskset() {
		return taskset;
	}

	@JsonIgnore
	public void setTaskset(Taskset taskset) {
		this.taskset = taskset;
	}
	
	public int getTasksetId() {
		return taskset.getId();
	}	
	
	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	
	@JsonIgnore
	public List<LibraryclassTargetvolume> getLibraryclassTargetvolume() {
		return libraryclassTargetvolume;
	}

	@JsonIgnore
	public void setLibraryclassTargetvolume(List<LibraryclassTargetvolume> libraryclassTargetvolume) {
		this.libraryclassTargetvolume = libraryclassTargetvolume;
	}
	
	@JsonIgnore
	public List<LibraryclassTapeset> getLibraryclassTapeset() {
		return libraryclassTapeset;
	}

	@JsonIgnore
	public void setLibraryclassTapeset(List<LibraryclassTapeset> libraryclassTapeset) {
		this.libraryclassTapeset = libraryclassTapeset;
	}

	@JsonIgnore
	public List<LibraryclassProperty> getLibraryclassProperty() {
		return libraryclassProperty;
	}

	@JsonIgnore
	public void setLibraryclassProperty(List<LibraryclassProperty> libraryclassProperty) {
		this.libraryclassProperty = libraryclassProperty;
	}
	
	@JsonIgnore
	public List<LibraryclassRequesttypeUser> getLibraryclassRequesttypeUser() {
		return libraryclassRequesttypeUser;
	}

	@JsonIgnore
	public void setLibraryclassRequesttypeUser(List<LibraryclassRequesttypeUser> libraryclassRequesttypeUser) {
		this.libraryclassRequesttypeUser = libraryclassRequesttypeUser;
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
	
    public void addTargetvolume(Targetvolume targetvolume) {
    	// linking the join table entry to this owning object
    	LibraryclassTargetvolume libraryclassTargetvolume = new LibraryclassTargetvolume(this, targetvolume);
    	this.libraryclassTargetvolume.add(libraryclassTargetvolume);

    	// inversing linking the join table entry to the target object
        targetvolume.getLibraryclassTargetvolume().add(libraryclassTargetvolume);
    }
    
    public void addTapeset(Tapeset tapeset, Task task, int copyNumber, boolean encrypted) {
    	// linking the join table entry to this owning object
    	LibraryclassTapeset libraryclassTapeset = new LibraryclassTapeset(this, tapeset);
    	libraryclassTapeset.setTask(task);
    	libraryclassTapeset.setCopyNumber(copyNumber);
    	libraryclassTapeset.setEncrypted(encrypted);
    	this.libraryclassTapeset.add(libraryclassTapeset);

    	// inversing linking the join table entry to the target object
        tapeset.getLibraryclassTapeset().add(libraryclassTapeset);
    }    
    
    public void addProperty(Property property, int position, boolean optional) {
    	// linking the join table entry to this owning object
    	LibraryclassProperty libraryclassProperty = new LibraryclassProperty(this, property);
    	libraryclassProperty.setPosition(position);
    	libraryclassProperty.setOptional(optional);
    	this.libraryclassProperty.add(libraryclassProperty);

    	// inversing linking the join table entry to the target object
        property.getLibraryclassProperty().add(libraryclassProperty);
    }
    
    public void addRequesttypeUser(Requesttype requesttype, User user) {
    	// linking the join table entry to this owning object
    	LibraryclassRequesttypeUser libraryclassRequesttypeUser = new LibraryclassRequesttypeUser(this, requesttype, user);
    	this.libraryclassRequesttypeUser.add(libraryclassRequesttypeUser);

    	// inversing linking the join table entry to the target object
    	requesttype.getLibraryclassRequesttypeUser().add(libraryclassRequesttypeUser);
    	user.getLibraryclassRequesttypeUser().add(libraryclassRequesttypeUser);
    }
    
    // TODO remove utility methods(){}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libraryclass )) return false;
        return id == ((Libraryclass) o).getId() ? true : false;
    }
 
    @Override
    public int hashCode() {
        return 32;
    }}