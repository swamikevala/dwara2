package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;

public class RequestCustomImpl implements RequestCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    /*
     * args[0] - type - the request type
     * args[1] - action - the requested action
     * args[2] - status - status List
     * args[3] - userId - filter on who created the request
     * args[4,5] - fromDate, toDate - the date range within which the request is requested
     * args[6,7] - fromDate, toDate - the date range within which the request is completed
     * args[8] - pageNumber - the nth pagenumber that need to be shown...
     * args[9] - pageSize - no. or rows that should be returned in the resultset
     */
    
	@Override
	public List<Request> findAllDynamicallyBasedOnParamsOrderByLatest(RequestType requestType, List<Action> action, List<Status> statusList, String user, LocalDateTime requestedAtStart, LocalDateTime requestedAtEnd, LocalDateTime completedAtStart, LocalDateTime completedAtEnd, String artifactName, int pageNumber, int pageSize) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
//		Long count = null;
//		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//		Root<Request> requestRootForCount = countQuery.from(Request.class);
//		
//		List<Predicate> predicates = getFramedPredicates(requestRootForCount, cb, actionId, userId, fromDate, toDate);
//		countQuery.select(cb.count(requestRootForCount)).where(cb.and(predicates.toArray(new Predicate[0])));
//		
//		count = entityManager.createQuery(countQuery).getSingleResult();
		
        CriteriaQuery<Request> query = cb.createQuery(Request.class);
        Root<Request> requestRoot = query.from(Request.class);
        
        List<Predicate> predicates = getFramedPredicates(requestRoot, cb, requestType, action, statusList, user, requestedAtStart, requestedAtEnd, completedAtStart, completedAtEnd, artifactName);
       	query.select(requestRoot).where(cb.and(predicates.toArray(new Predicate[0])));
       	query.orderBy(cb.desc(requestRoot.get("id"))); // default orderby most recent first
        //List<Request> requestList = entityManager.createQuery(query).setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
       	List<Request> requestList = entityManager.createQuery(query).getResultList();
        
//        WrappedRequestList wrappedRequestList = new WrappedRequestList();
//        wrappedRequestList.setPage(pageNumber);
//        wrappedRequestList.setPageSize(pageSize);
//        wrappedRequestList.setTotal(count);
//        wrappedRequestList.setRequestList(requestList);
        return requestList;
	}


	
	private List<Predicate> getFramedPredicates(Root<Request> requestRoot, CriteriaBuilder cb, RequestType requestType, List<Action> actionList, List<Status> statusList, String user,
			LocalDateTime requestedAtStart, LocalDateTime requestedAtEnd, LocalDateTime completedAtStart, LocalDateTime completedAtEnd, String artifactName) {
        
        
	    List<Predicate> predicates = new ArrayList<>();
		if(requestType != null) {
			predicates.add(cb.equal(requestRoot.get("type"), requestType));
		}
		
	    if(actionList != null) {
		    Path<String> actionIdPath = requestRoot.get("actionId");
		    List<Predicate> actionPredicates = new ArrayList<>();
		    for (Action action : actionList) {
				actionPredicates.add(cb.equal(actionIdPath, action));
			}
			predicates.add(cb.or(actionPredicates.toArray(new Predicate[actionPredicates.size()])));
	    } 

	    if(statusList != null) {
		    Path<String> statusIdPath = requestRoot.get("status");
		    List<Predicate> statusPredicates = new ArrayList<>();
		    for (Status status : statusList) {
				statusPredicates.add(cb.equal(statusIdPath, status));
			}
			predicates.add(cb.or(statusPredicates.toArray(new Predicate[statusPredicates.size()])));
	    } 
	    
		if(user != null) {
			predicates.add(cb.equal(requestRoot.get("requestedBy"), user));
		}

		if(requestedAtStart != null) {
			if(requestedAtEnd == null)
				requestedAtEnd = LocalDateTime.now();
			predicates.add(cb.between(requestRoot.get("requestedAt"), requestedAtStart, requestedAtEnd));
		}
		
		if(completedAtStart != null) {
			if(completedAtEnd == null)
				completedAtEnd = LocalDateTime.now();
			predicates.add(cb.between(requestRoot.get("completedAt"), completedAtStart, completedAtEnd));
		}
		
		if(artifactName != null) {
			predicates.add(cb.like(cb.function("JSON_EXTRACT", String.class, requestRoot.get("details"), cb.literal("$.staged_filename")), artifactName));
		}
		return predicates;
	}
}
