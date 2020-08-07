package org.ishafoundation.dwaraapi.db.dao.master.cache;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CacheableRepository<T extends Cacheable> extends CrudRepository<T,String> {
	
}
