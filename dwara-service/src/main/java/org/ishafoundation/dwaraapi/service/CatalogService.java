package org.ishafoundation.dwaraapi.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ishafoundation.dwaraapi.api.resp.catalog.CatalogRespond;
import org.ishafoundation.dwaraapi.db.dao.master.CatalogDao;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.Catalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.Artifact1Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CatalogService extends DwaraService{
    private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    @PersistenceContext
    private EntityManager entityManager;

    // @Autowired
    // private CatalogDao catalogDao;

    public List<Catalog> searchCatalogs(String artifactClass, String volumeGroup, String copyNumber, String tapeNumber, String startDate, String endDate, String artifactName, String tags) {
        // return catalogDao.findCatalogs();
        Query q = entityManager.createNativeQuery("select a.id, a.artifactclass_id, a.name, a.total_size, b.volume_id, c.group_ref_id, d.completed_at, e.name as ingestedBy " 
            + "from artifact1 a join artifact1_volume b join volume c join request d join user e "
            + "where a.id=b.artifact_id and b.volume_id=c.id and a.write_request_id=d.id and d.requested_by_id=e.id "
            + "order by completed_at desc");
        List<Object[]> results = q.getResultList();
        List<Catalog> list = new ArrayList<Catalog>();
        results.stream().forEach((record) -> {
            int _artifactId = ((Integer) record[0]).intValue();
            String _artifactClass = (String) record[1];
            String _artifactName = (String) record[2];
            long _size = ((BigInteger)record[3]).longValue();
            String _volumeId = (String) record[4];
            String _groupVolume = (String) record[5];
            String _ingestedDate = ((Timestamp) record[6]).toLocalDateTime().toString();
            String _ingestedBy = (String) record[7];

            /* Query q2 = entityManager.createNativeQuery("select tag from artifact1_tag where artifact1_id=" + _artifactId);
            List<Object[]> results2 = q2.getResultList();
            List<String> arrTags = new ArrayList<String>();
            results2.stream().forEach((r) -> {
                String t = (String) r[0];
                arrTags.add(t);
            });
            String[] arr = new String[arrTags.size()];
            arr = arrTags.toArray(arr); */
            list.add(new Catalog(_artifactId, _artifactClass, _artifactName, _size, _volumeId, _groupVolume, _ingestedDate, _ingestedBy));
        });
        logger.info("list size: " + list.size());
        return list;
    }

    /* public List<Artifact1> findCatalogs(String artifactClass, String volumeGroup, String copyNumber, String tapeNumber, String startDate, String endDate, String artifactName, String tags) {
        // List<CatalogRespond> list = new ArrayList<CatalogRespond>();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Artifact1> query = cb.createQuery(Artifact1.class);

        Root<Artifact1> artifactRoot = query.from(Artifact1.class);
        Join<Artifact1, Artifact1Volume>  join2 = artifactRoot.join("artifact1_volume", JoinType.INNER);
        // Join<Artifact1Volume, Volume> join2 = join1.join("volume", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if(artifactClass != "")
            predicates.add(cb.equal(join2.get("artifactclass_id"), artifactClass));
        // if(volumeGroup != "")
        //     predicates.add(cb.equal(join2.get("group_ref_id"), volumeGroup));
        if(tapeNumber != "")
            predicates.add(cb.equal(join2.get("volume_id"), tapeNumber));

        join2.on(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        query.select(join2.getParent());

        List<Artifact1> list = entityManager.createQuery(query).getResultList();
        return list;
    } */
}
