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

public class SubrequestCustomImpl implements SubrequestCustom {

    @PersistenceContext
    private EntityManager entityManager;
    

	@Override
	public List<Subrequest> findAllByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusIdSet) {
		//Framing the query --> select * from `subrequest` `s` join `request` `r` on `s`.`request_id`=`r`.`request_id` and `r`.`requesttype_id`=9008 and (`s`.`status_id`=1 or `s`.`status_id`=2);
        
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        CriteriaQuery<Subrequest> query = cb.createQuery(Subrequest.class);
        Root<Subrequest> subrequestRoot = query.from(Subrequest.class);
        
        
        Join<Subrequest, Request> requestJoin = null;
        try {
        	requestJoin = subrequestRoot.join("request", JoinType.INNER);
        }catch (Exception e) {
        	System.out.println("adsfasd");
			e.printStackTrace();
		}
        
        Path<String> statusIdPath = subrequestRoot.get("status");
        List<Predicate> predicates = new ArrayList<>();
		for (Integer statusId : statusIdSet) {
			predicates.add(cb.equal(statusIdPath, statusId));
		}
		
        requestJoin.on(cb.equal(requestJoin.get("requesttype"), requesttypeId), cb.or(predicates.toArray(new Predicate[predicates.size()])));
		query.select(requestJoin.getParent());
        return entityManager.createQuery(query)
            .getResultList();
	}


	@Override
	public List<Subrequest> findAllLatestByRequesttypeAndStatusIds(int requesttypeId, Set<Integer> statusIdSet) {
		// TODO need to get the latest... ? How?
		return findAllByRequesttypeAndStatusIds(requesttypeId, statusIdSet);
	}

}
