package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;

public class JobCustomImpl implements JobCustom {

    @PersistenceContext
    private EntityManager entityManager;

	@Override
	public List<Job> findAllDynamicallyBasedOnParamsOrderByLatest(Integer systemRequestId, List<Status> statusList) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
        CriteriaQuery<Job> query = cb.createQuery(Job.class);
        Root<Job> jobRoot = query.from(Job.class);
        
        List<Predicate> predicates = getFramedPredicates(jobRoot, cb, systemRequestId, statusList);
       	query.select(jobRoot).where(cb.and(predicates.toArray(new Predicate[0])));
       	query.orderBy(cb.desc(jobRoot.get("id"))); // default orderby most recent first
        //List<Request> requestList = entityManager.createQuery(query).setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
       	List<Job> jobList = entityManager.createQuery(query).getResultList();

        return jobList;
	}
	
	
	private List<Predicate> getFramedPredicates(Root<Job> jobRoot, CriteriaBuilder cb, Integer requestId, List<Status> statusList) {
	    List<Predicate> predicates = new ArrayList<>();
	    
		if(requestId != null) {
			predicates.add(cb.equal(jobRoot.get("request"), requestId));
		}
		
	    if(statusList != null) {
		    Path<String> statusIdPath = jobRoot.get("status");
		    List<Predicate> statusPredicates = new ArrayList<>();
		    for (Status status : statusList) {
				statusPredicates.add(cb.equal(statusIdPath, status));
			}
			predicates.add(cb.or(statusPredicates.toArray(new Predicate[statusPredicates.size()])));
	    } 

	    return predicates;
	}
}
