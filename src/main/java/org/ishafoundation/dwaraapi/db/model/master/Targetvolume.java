package org.ishafoundation.dwaraapi.db.model.master;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTargetvolume;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="Targetvolume")
@Table(name="targetvolume")
public class Targetvolume {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique=true)
	private String name;
	
	@Column(name="path")
	private String path;	

    @OneToMany(mappedBy = "targetvolume",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    List<LibraryclassTargetvolume> libraryclassTargetvolume = new ArrayList<>();
	

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
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonIgnore
	public List<LibraryclassTargetvolume> getLibraryclassTargetvolume() {
		return libraryclassTargetvolume;
	}
	
	@JsonIgnore
	public void setLibraryclassTargetvolume(List<LibraryclassTargetvolume> libraryclassTargetvolume) {
		this.libraryclassTargetvolume = libraryclassTargetvolume;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Targetvolume targetvolume = (Targetvolume) o;
        return Objects.equals(name, targetvolume.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }	
}
