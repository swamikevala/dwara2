package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

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
     * args[0] - actionId - the request type
     * args[1] - userId - filter on who created the request
     * args[2,3] - fromDate, toDate - the date range within which the request is requested
     * args[4] - pageNumber - the nth pagenumber that need to be shown...
     * args[5] - pageSize - no. or rows that should be returned in the resultset
     */
    
	@Override
	public WrappedRequestList findAllByActionAndUserIdAndRequestedAtOrderByLatest(Integer actionId, Integer userId, LocalDateTime fromDate, LocalDateTime toDate, int pageNumber, int pageSize) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		Long count = null;
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<Request> requestRootForCount = countQuery.from(Request.class);
		
		List<Predicate> predicates = getFramedPredicates(requestRootForCount, cb, actionId, userId, fromDate, toDate);
		countQuery.select(cb.count(requestRootForCount)).where(cb.and(predicates.toArray(new Predicate[0])));
		
		count = entityManager.createQuery(countQuery).getSingleResult();
		
        CriteriaQuery<Request> query = cb.createQuery(Request.class);
        Root<Request> requestRoot = query.from(Request.class);
        
        predicates = getFramedPredicates(requestRoot, cb, actionId, userId, fromDate, toDate);
       	query.select(requestRoot).where(cb.and(predicates.toArray(new Predicate[0])));
       	query.orderBy(cb.desc(requestRoot.get("id"))); // default orderby most recent first
        List<Request> requestList = entityManager.createQuery(query).setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
        
        WrappedRequestList wrappedRequestList = new WrappedRequestList();
        wrappedRequestList.setPageNumber(pageNumber);
        wrappedRequestList.setTotalNoOfRecords(count);
        wrappedRequestList.setRequestList(requestList);
        return wrappedRequestList;
	}

	private List<Predicate> getFramedPredicates(Root<Request> requestRoot, CriteriaBuilder cb, Integer actionId, Integer userId,
			LocalDateTime fromDate, LocalDateTime toDate) {
        
        
	    List<Predicate> predicates = new ArrayList<>();
		if(actionId != null) {
			predicates.add(cb.equal(requestRoot.get("action"), actionId));
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
