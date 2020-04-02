package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.model.WrappedRequestList;

public class RequestCustomImpl implements RequestCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    /*
     * args[0] - requesttypeId - the request type
     * args[1] - userId - filter on who created the request
     * args[2,3] - fromDate, toDate - the date range within which the request is requested
     * args[4] - pageNumber - the nth pagenumber that need to be shown...
     * args[5] - pageSize - no. or rows that should be returned in the resultset
     */
    
	@Override
	public WrappedRequestList findAllByRequesttypeAndUserIdAndRequestedAtOrderByLatest(Integer requesttypeId, Integer userId, LocalDateTime fromDate, LocalDateTime toDate, int pageNumber, int pageSize) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		//TODO - default orderby most recent first ??
		
		
		Long count = null;
		if(pageNumber == 1) { // calling only the first time...
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Request> requestRootForCount = countQuery.from(Request.class);
			
			List<Predicate> predicates = getFramedPredicates(requestRootForCount, cb, requesttypeId, userId, fromDate, toDate);
			//if(predicates != null && predicates.size() > 0)
				countQuery.select(cb.count(requestRootForCount)).where(cb.and(predicates.toArray(new Predicate[0])));
			
			count = entityManager.createQuery(countQuery).getSingleResult();
			System.out.println("count - " + count);
		}
		
        CriteriaQuery<Request> query = cb.createQuery(Request.class);
        Root<Request> requestRoot = query.from(Request.class);
        
        List<Predicate> predicates = getFramedPredicates(requestRoot, cb, requesttypeId, userId, fromDate, toDate);
       	query.select(requestRoot).where(cb.and(predicates.toArray(new Predicate[0])));
		
        List<Request> requestList = entityManager.createQuery(query).setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
        
        WrappedRequestList wrappedRequestList = new WrappedRequestList();
        wrappedRequestList.setPageNumber(pageNumber);
        wrappedRequestList.setTotalNoOfRecords(count);
        wrappedRequestList.setRequestList(requestList);
        return wrappedRequestList;
	}

	private List<Predicate> getFramedPredicates(Root<Request> requestRoot, CriteriaBuilder cb, Integer requesttypeId, Integer userId,
			LocalDateTime fromDate, LocalDateTime toDate) {
        
        
	    List<Predicate> predicates = new ArrayList<>();
		if(requesttypeId != null) {
			predicates.add(cb.equal(requestRoot.get("requesttype"), requesttypeId));
		}
		if(userId != null) {
			predicates.add(cb.equal(requestRoot.get("user"), userId));
		}
		if(fromDate != null) {
			if(toDate == null)
				toDate = LocalDateTime.now();
			predicates.add(cb.between(requestRoot.get("requestedAt"), fromDate, toDate));
		}
		return predicates;
	}
}
