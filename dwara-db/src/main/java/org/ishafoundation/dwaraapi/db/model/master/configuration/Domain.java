package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;

@Entity(name="Domain")
@Table(name="domain")
public class Domain implements Cacheable{
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;
	
	@Column(name="\"default\"")
//	private boolean defaulT;
//	private boolean defaultt;
	private boolean default_;

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

	public boolean isDefault_() {
		return default_;
	}

	public void setDefault_(boolean default_) {
		this.default_ = default_;
	}

//	public boolean isDefaultt() {
//		return defaultt;
//	}
//
//	public void setDefaultt(boolean defaultt) {
//		this.defaultt = defaultt;
//	}

//	public boolean isDefaulT() {
//		return defaulT;
//	}
//
//	public void setDefaulT(boolean defaulT) {
//		this.defaulT = defaulT;
//	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domain domain = (Domain) o;
        return Objects.equals(name, domain.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
