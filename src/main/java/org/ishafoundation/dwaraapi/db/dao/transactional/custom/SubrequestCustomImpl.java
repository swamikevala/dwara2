package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.ishafoundation.dwaraapi.model.WrappedSubrequestList;

public class SubrequestCustomImpl implements SubrequestCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    // Should we use specifications or querydsl?
    // Just going with JPA out right evven though it has challenges - https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/

	@Override
	public WrappedSubrequestList findAllByRequesttypeIdAndStatusIds(Integer requesttypeId, Set<Integer> statusIds, int pageNumber, int pageSize) {
		//Framing the query --> select * from `subrequest` `s` join `request` `r` on `s`.`request_id`=`r`.`request_id` and `r`.`requesttype_id`=9008 and (`s`.`status_id`=1 or `s`.`status_id`=2);
        
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		Long count = null;
		if(pageNumber == 1) { // TODO - Check with Dong anna calling only the first time...
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Subrequest> subrequestRoot2 = countQuery.from(Subrequest.class);
			Join<Subrequest, Request> requestJoin2 = frameJoin(subrequestRoot2, cb, requesttypeId, statusIds);
			countQuery.select(cb.count(requestJoin2.getParent()));
			
			count = entityManager.createQuery(countQuery).getSingleResult();
			System.out.println("count - " + count);
		}
		
        CriteriaQuery<Subrequest> query = cb.createQuery(Subrequest.class);
        Root<Subrequest> subrequestRoot = query.from(Subrequest.class);
        Join<Subrequest, Request> requestJoin = frameJoin(subrequestRoot, cb, requesttypeId, statusIds);

		query.select(requestJoin.getParent());	
		
        List<Subrequest> subrequestList = entityManager.createQuery(query).setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
        
        WrappedSubrequestList wrappedSubrequestList = new WrappedSubrequestList();
        wrappedSubrequestList.setPageNumber(pageNumber);
        wrappedSubrequestList.setTotalNoOfRecords(count);
        wrappedSubrequestList.setSubrequestList(subrequestList);
        return wrappedSubrequestList;
	}

	// One of the main problem with JPA is that the predicates are not easy to externalize and reuse because we need to set up the CriteriaBuilder, CriteriaQuery and Root first
	// - https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/ 
	// so we have this method called by different CriteriaQuery's as many times
	
	private Join<Subrequest, Request> frameJoin(Root<Subrequest> subrequestRoot, CriteriaBuilder cb, Integer requesttypeId, Set<Integer> statusIdSet) {
	    Join<Subrequest, Request> requestJoin = null;
	    try {
	    	requestJoin = subrequestRoot.join("request", JoinType.INNER);
	    
	    	List<Predicate> predicates = new ArrayList<>();
	    	
		    if(statusIdSet != null) {
			    Path<String> statusIdPath = subrequestRoot.get("status");
			    List<Predicate> statusPredicates = new ArrayList<>();
				for (Integer statusId : statusIdSet) {
					statusPredicates.add(cb.equal(statusIdPath, statusId));
				}
				predicates.add(cb.or(statusPredicates.toArray(new Predicate[statusPredicates.size()])));
		    } 
		    
		    if(requesttypeId != null) {
		    	predicates.add(cb.equal(requestJoin.get("requesttype"), requesttypeId));
		    }

		    requestJoin.on(cb.and(predicates.toArray(new Predicate[predicates.size()])));

	    }catch (Exception e) {
			e.printStackTrace();
		}
	    return requestJoin;
	}
	
//	private Join<Subrequest, Request> frameJoin(Root<Subrequest> subrequestRoot, CriteriaBuilder cb, int requesttypeId, Set<Integer> statusIdSet) {
//	    Join<Subrequest, Request> requestJoin = null;
//	    try {
//	    	requestJoin = subrequestRoot.join("request", JoinType.INNER);
//	    
//		    if(statusIdSet != null) {
//			    Path<String> statusIdPath = subrequestRoot.get("status");
//			    List<Predicate> predicates = new ArrayList<>();
//				for (Integer statusId : statusIdSet) {
//					predicates.add(cb.equal(statusIdPath, statusId));
//				}
//				requestJoin.on(cb.equal(requestJoin.get("requesttype"), requesttypeId), cb.or(predicates.toArray(new Predicate[predicates.size()])));
//		    } else {
//		    	requestJoin.on(cb.equal(requestJoin.get("requesttype"), requesttypeId));
//		    }
//	    }catch (Exception e) {
//			e.printStackTrace();
//		}
//	    return requestJoin;
//	}
	
	@Override
	public WrappedSubrequestList findAllLatestByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusIdSet, int pageNumber, int pageSize) {
		// TODO need to get the latest... ? How?
		return findAllByRequesttypeIdAndStatusIds(requesttypeId, statusIdSet, pageNumber, pageSize);
	}

}
