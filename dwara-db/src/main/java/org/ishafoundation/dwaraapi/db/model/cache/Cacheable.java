package org.ishafoundation.dwaraapi.db.model.cache;

import javax.persistence.MappedSuperclass;

// Marker interface for enabling configuration tables to be cached and retrieved using a factory
//@Getter
//@Setter
@MappedSuperclass
public interface Cacheable {
	
	public String getId();
}