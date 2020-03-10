package org.ishafoundation.dwaraapi.db.dao.transactional.custom;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;

public class SubrequestCustomImpl implements SubrequestCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
	@Override
	public List<Subrequest> findSubrequestByTypeAndStatus(int requesttypeId, Set<String> status) {
		return null;
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Subrequest> query = cb.createQuery(Subrequest.class);
//        Root<Subrequest> user = query.from(Subrequest.class);
// 
//        Path<String> emailPath = user.get("email");
// 
//        List<Predicate> predicates = new ArrayList<>();
//        for (String email : emails) {
//            predicates.add(cb.like(emailPath, email));
//        }
//        query.select(user)
//            .where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
// 
//        return entityManager.createQuery(query)
//            .getResultList();

	}

	@Override
	public List<Subrequest> findSubrequestByTypeAndStatusId(int requesttypeId, Set<Integer> statusId) {
		// TODO Auto-generated method stub
		return null;
	}

}
