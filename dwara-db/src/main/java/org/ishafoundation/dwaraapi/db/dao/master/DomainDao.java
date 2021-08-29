/*
 * package org.ishafoundation.dwaraapi.db.dao.master;
 * 
 * import org.ishafoundation.dwaraapi.db.model.master.configuration.Domain;
 * import org.springframework.data.jpa.repository.Query; import
 * org.springframework.data.repository.CrudRepository;
 * 
 * public interface DomainDao extends CrudRepository<Domain,Integer> {//extends
 * CacheableRepository<Domain> {
 * 
 * @Query("select dom from Domain dom where dom.default_ = true") Domain
 * findByDefaultTrue(); // this wont work as hibernate replace _ with camel case
 * field names Domain findByDefault_True(); }
 */