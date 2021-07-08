package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestCustomImpl implements RequestCustom {
	
	private static final Logger logger = LoggerFactory.getLogger(RequestCustomImpl.class);

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
	public List<Request> findAllDynamicallyBasedOnParamsOrderByLatest(RequestType requestType, List<Action> actionList, List<Status> statusList, List<User> requestedByList, LocalDateTime requestedAtStart, LocalDateTime requestedAtEnd, LocalDateTime completedAtStart, LocalDateTime completedAtEnd, String artifactName, List<String> artifactclassList, int pageNumber, int pageSize) {

		//select * from request join artifact1 on artifact1.write_request_id = request.id join artifactclass on artifact1.artifactclass_id = artifactclass.id where artifactclass.id like 'video%' and requested_at>'2021-06';

		/*
		select request.* from request join artifact1 on artifact1.write_request_id = request.id join artifactclass on artifact1.artifactclass_id = artifactclass.id 
				where 
				request.type = 'system' 
				and 
				request.action_id in ('ingest','restore')
				and
				artifactclass.id in ('video-pub','video-digi-2020-pub') 
				and 
				requested_at>'2021-06';
		 */

		StringBuffer query = new StringBuffer();
		query.append("select request.* from request join artifact1 on artifact1.write_request_id = request.id join artifactclass on artifact1.artifactclass_id = artifactclass.id where ");
		boolean toBeAnded = false;
		if(requestType != null) {
			query.append("request.type = '" + requestType + "'");
			toBeAnded = true;
		}

		if(actionList != null) {
			if(toBeAnded)
				query.append(" and ");

			query.append("request.action_id in (");	
			int cnt = 1;

			for (Action action : actionList) {
				if(cnt > 1)
					query.append(",");

				query.append("'" + action + "'");
				cnt = cnt + 1;
			}
			query.append(")");
			toBeAnded = true;
		} 

		if(statusList != null) {
			if(toBeAnded)
				query.append(" and ");

			query.append("request.status in (");	
			int cnt = 1;

			for (Status status : statusList) {
				if(cnt > 1)
					query.append(",");

				query.append("'" + status + "'");
				cnt = cnt + 1;
			}
			query.append(")");
			toBeAnded = true;
		} 

		if(requestedByList != null) {
			if(toBeAnded)
				query.append(" and ");

			query.append("request.requested_by_id in (");	
			int cnt = 1;

			for (User requestedBy : requestedByList) {
				if(cnt > 1)
					query.append(",");

				query.append("'" + requestedBy.getId() + "'");
				cnt = cnt + 1;
			}
			query.append(")");
			toBeAnded = true;
		}

		if(requestedAtStart != null) {
			if(requestedAtEnd == null)
				requestedAtEnd = LocalDateTime.now();

			if(toBeAnded)
				query.append(" and ");

			query.append("(request.requested_at between '" + requestedAtStart + "' and '" + requestedAtEnd + "')");	
			toBeAnded = true;
		}

		if(completedAtStart != null) {
			if(completedAtEnd == null)
				completedAtEnd = LocalDateTime.now();

			if(toBeAnded)
				query.append(" and ");

			query.append("(request.completed_at between '" + completedAtStart + "' and '" + completedAtEnd + "')");	
			toBeAnded = true;
		}

		if(artifactclassList != null) {
			if(toBeAnded)
				query.append(" and ");

			query.append("artifactclass.id in (");	
			int cnt = 1;

			for (String artifactclass : artifactclassList) {
				if(cnt > 1)
					query.append(",");

				query.append("'" + artifactclass + "'");
				cnt = cnt + 1;
			}
			query.append(")");
			toBeAnded = true;
		}
		logger.info("mysql query: " + query);
		Query q = entityManager.createNativeQuery(query.toString());
		
		return q.getResultList();


		/*
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

        List<Predicate> predicates = getFramedPredicates(requestRoot, cb, requestType, action, statusList, requestedByList, requestedAtStart, requestedAtEnd, completedAtStart, completedAtEnd, artifactName, artifactclassList);
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
		 */
	}



	private List<Predicate> getFramedPredicates(Root<Request> requestRoot, CriteriaBuilder cb, RequestType requestType, List<Action> actionList, List<Status> statusList, List<User> requestedByList,
			LocalDateTime requestedAtStart, LocalDateTime requestedAtEnd, LocalDateTime completedAtStart, LocalDateTime completedAtEnd, String artifactName, List<String> artifactclassList) {


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
			Path<String> statusPath = requestRoot.get("status");
			List<Predicate> statusPredicates = new ArrayList<>();
			for (Status status : statusList) {
				statusPredicates.add(cb.equal(statusPath, status));
			}
			predicates.add(cb.or(statusPredicates.toArray(new Predicate[statusPredicates.size()])));
		} 

		if(requestedByList != null) {
			Path<String> requestedByPath = requestRoot.get("requestedBy");
			List<Predicate> requestedByPredicates = new ArrayList<>();
			for (User requestedBy : requestedByList) {

				requestedByPredicates.add(cb.equal(requestedByPath, requestedBy));
			}
			predicates.add(cb.or(requestedByPredicates.toArray(new Predicate[requestedByPredicates.size()])));
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

		// TODO - This wont reflect renamed artifacts. Need to join artifact table 
		if(artifactName != null) {
			predicates.add(cb.equal(cb.function("JSON_EXTRACT", String.class, requestRoot.get("details"), cb.literal("$.staged_filename")), artifactName));
		}
		return predicates;
	}
}
