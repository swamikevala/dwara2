package org.ishafoundation.dwaraapi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.ishafoundation.dwaraapi.api.resp.autoloader.TapeStatus;
import org.ishafoundation.dwaraapi.db.dao.master.CatalogDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact1;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactCatalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TapeCatalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.Artifact1Volume;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CatalogService extends DwaraService{
    private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private VolumeDao volumeDao;

    @Autowired
    private RequestDao requestDao;

    public Location bulkChangeTapeLocation(String[] volumeIds, String newLocation) {
        if(volumeIds != null) {
            Location l = new Location();
            l.setId(newLocation);
            for(int i = 0; i < volumeIds.length; i++) {
                Volume volume = volumeDao.findById(volumeIds[i]).get();
                if(volume != null) {
                    volume.setLocation(l);
                    volumeDao.save(volume);
                }
            }
            return l;
        }
        return null;
    }

    public Location changeTapeLocation(String volumeId, String newLocation) {
        Volume volume = volumeDao.findById(volumeId).get();
        if(volume != null){
            Location l = new Location();
            l.setId(newLocation);
            volume.setLocation(l);
            volumeDao.save(volume);
            return l;
        }
        return null;
    }

    public void updateUsedSpace() {
        String query = "select volume_id, max(json_extract(details, '$.end_volume_block')) from dwara.artifact1_volume group by volume_id;";
        Query q = entityManager.createNativeQuery(query);
        List<Object[]> results = q.getResultList();
        results.forEach((record) -> {
            String volumeId = (String)record[0];
            Volume volume = volumeDao.findById(volumeId).get();
            String lastBlock = (String) record[1];
            // logger.info("volumeId: " + volumeId + ", lastBlock: " + lastBlock);
            Long usedCapacity = Long.parseLong(lastBlock)*volume.getDetails().getBlocksize();

            if(volume != null){
                volume.setUsedCapacity(usedCapacity);
                volumeDao.save(volume);
            }
        });
    }
    
    public void updateFinalizedDate() {
        List<Status> statusList = new ArrayList<Status>();
        statusList.add(Status.completed);
        
        List<Request> finalizedList = requestDao.findAllByActionIdAndStatusInAndType(Action.finalize, statusList, RequestType.system);
        finalizedList.forEach((r)-> {
            LocalDateTime finalizedDate = r.getCompletedAt();
            String volumeId = r.getDetails().getVolumeId();
            Volume volume = volumeDao.findById(volumeId).get();
            if(volume != null){
                volume.setFinalizedAt(finalizedDate);
                volumeDao.save(volume);
            }
        });
    }

    public List<TapeCatalog> findTapesCatalog(String volumeId, String[] volumeGroup, String[] copyNumber, String[] format, String[] location, String startDate, String endDate) {
        Query q2 = entityManager.createNativeQuery("select artifactclass_id, volume_id from artifactclass_volume where active=1");
        List<Object[]> results2 = q2.getResultList();
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        results2.stream().forEach((record) -> {
            String _artifactClassId = (String) record[0];
            String _groupId = (String) record[1];
            if(map.get(_groupId) == null) {
                map.put(_groupId, new ArrayList<String>());
            }
            else {
                List<String> l = map.get(_groupId);
                l.add(_artifactClassId);
                map.put(_groupId, l);
            }
        });

        String condition = "";
        if(volumeId != "") {
            String name = volumeId.replaceAll(" ", "%");
            condition += " and a.id like '%" + name + "%'";
        }
        if(volumeGroup != null && volumeGroup.length >=1 && !volumeGroup[0].equals("all")){
            condition += " and a.group_ref_id in (";
            for(String a: volumeGroup) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(copyNumber != null && copyNumber.length >= 1 && !copyNumber[0].equals("all")) {
            condition += " and substr(a.group_ref_id, 2, 1) in (";
            for(String a: copyNumber) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(format != null && format.length >= 1 && !format[0].equals("all")) {
            condition += " and a.archiveformat_id in (";
            for(String a: format) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(location != null && location.length >= 1 && !location[0].equals("all")) {
            condition += " and a.location_id in (";
            for(String a: location) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(startDate != "")
            condition += " and a.finalized_at >= '" + startDate + "'";
        if(endDate != "")
            condition += " and a.finalized_at <= '" + endDate + "'";
        String query = "select a.group_ref_id, a.id, a.archiveformat_id, a.location_id, a.initialized_at, a.capacity, a.imported, a.finalized, a.suspect, a.finalized_at, a.used_capacity, a.defective" 
        + " from volume a"
        + " where a.initialized_at is not null"
        + condition
        + " order by a.finalized_at desc";
        Query q = entityManager.createNativeQuery(query);
        // logger.info("mysql query: " + query);
        List<Object[]> results = q.getResultList();
        List<TapeCatalog> list = new ArrayList<TapeCatalog>();
        results.stream().forEach((record) -> {
            String _volumeGroup = (String) record[0];
            String _volumeId = (String) record[1];
            String _format = (String) record[2];
            String _location = (String) record[3];
            String _initializedAt = "";
            if(record[4] != null)
                _initializedAt = ((Timestamp) record[4]).toLocalDateTime().toString();
            long _capacity = 0;
            if(record[5] != null)
                _capacity = ((BigInteger)record[5]).longValue();
            boolean _isImported = (boolean)record[6];
            boolean _isFinalized = (boolean)record[7];
            boolean _isSuspect = (boolean)record[8];
            String _finalizedAt = "";
            if(record[9] != null)
                _finalizedAt = ((Timestamp) record[9]).toLocalDateTime().toString();
            long _usedCapacity = 0;
            if(record[10] != null)
                _usedCapacity = ((BigInteger)record[10]).longValue();
            boolean _isDefective = (boolean)record[11];
            List<String> _artifactClass = map.get(_volumeGroup);

            String status = "";
            if(_isImported)
                status = "Imported";
            else if(_isFinalized)
                status = "Finalized";
            else if(_initializedAt != "") {
                if(_usedCapacity > 1024*1024)
                    status = "Partially Written";
                else
                    status = "Initialized";
            }
                
            
            list.add(new TapeCatalog(_volumeId, _volumeGroup, _format, _location, status, _initializedAt,
                _finalizedAt, _usedCapacity, _capacity, _artifactClass, _isSuspect, _isDefective));
        });
        // logger.info("list size: " + list.size());
        return list;
    }

    public List<ArtifactCatalog> findArtifactsCatalogByVolumeIds(String[] volumeIds) {
        String condition = "";
        if(volumeIds != null && volumeIds.length > 0) {
            condition += " and b.volume_id in (";
            for(String a: volumeIds) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        String query = "select a.id, a.artifactclass_id, a.name, a.total_size, b.volume_id, c.group_ref_id, d.completed_at, e.name as ingestedBy, c.archiveformat_id" 
        + " from artifact1 a join artifact1_volume b join volume c join request d join user e"
        + " where a.id=b.artifact_id and b.volume_id=c.id and a.write_request_id=d.id and d.requested_by_id=e.id and d.completed_at is not null and a.deleted=0"
        + condition
        + " order by completed_at desc";
        Query q = entityManager.createNativeQuery(query);
        // logger.info("mysql query: " + query);
        List<Object[]> results = q.getResultList();
        List<ArtifactCatalog> list = new ArrayList<ArtifactCatalog>();
        results.stream().forEach((record) -> {
            int _artifactId = ((Integer) record[0]).intValue();
            String _artifactClass = (String) record[1];
            String _artifactName = (String) record[2];
            long _size = ((BigInteger)record[3]).longValue();
            String _volumeId = (String) record[4];
            String _groupVolume = (String) record[5];
            String _ingestedDate = "";
            if(record[6] != null)
                _ingestedDate = ((Timestamp) record[6]).toLocalDateTime().toString();
            String _ingestedBy = (String) record[7];
            String _format = (String) record[8];

            list.add(new ArtifactCatalog(_artifactId, _artifactClass, _artifactName, _size, _volumeId, _groupVolume, _ingestedDate, _ingestedBy, _format));
        });
        // logger.info("list size: " + list.size());
        return list;
    }

    public List<ArtifactCatalog> findArtifactsCatalog(String[] artifactClass, String[] volumeGroup, String[] copyNumber, String volumeId, String startDate, String endDate, String artifactName) {
        String condition = "";
        if(artifactClass != null && artifactClass.length >=1 && !artifactClass[0].equals("all")) {
            condition += " and a.artifactclass_id in (";
            for(String a: artifactClass) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(volumeGroup != null && volumeGroup.length >=1 && !volumeGroup[0].equals("all")){
            condition += " and c.group_ref_id in (";
            for(String a: volumeGroup) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(copyNumber != null && copyNumber.length >= 1 && !copyNumber[0].equals("all")) {
            condition += " and substr(c.group_ref_id, 2, 1) in (";
            for(String a: copyNumber) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(volumeId != "")
            condition += " and b.volume_id='" + volumeId + "'";
        if(startDate != "")
            condition += " and d.completed_at >= '" + startDate + "'";
        if(endDate != "")
            condition += " and d.completed_at <= '" + endDate + "'";
        if(artifactName != "") {
            String name = artifactName.replaceAll(" ", "%");
            condition += " and a.name like '%" + name + "%'";
        }
        String query = "select a.id, a.artifactclass_id, a.name, a.total_size, b.volume_id, c.group_ref_id, d.completed_at, e.name as ingestedBy, c.archiveformat_id" 
        + " from artifact1 a join artifact1_volume b join volume c join request d join user e"
        + " where a.id=b.artifact_id and b.volume_id=c.id and a.write_request_id=d.id and d.requested_by_id=e.id and d.completed_at is not null and a.deleted=0"
        + condition
        + " order by completed_at desc";
        Query q = entityManager.createNativeQuery(query);
        // logger.info("mysql query: " + query);
        List<Object[]> results = q.getResultList();
        List<ArtifactCatalog> list = new ArrayList<ArtifactCatalog>();
        results.stream().forEach((record) -> {
            int _artifactId = ((Integer) record[0]).intValue();
            String _artifactClass = (String) record[1];
            String _artifactName = (String) record[2];
            long _size = ((BigInteger)record[3]).longValue();
            String _volumeId = (String) record[4];
            String _groupVolume = (String) record[5];
            String _ingestedDate = "";
            if(record[6] != null)
                _ingestedDate = ((Timestamp) record[6]).toLocalDateTime().toString();
            String _ingestedBy = (String) record[7];
            String _format = (String) record[8];

            list.add(new ArtifactCatalog(_artifactId, _artifactClass, _artifactName, _size, _volumeId, _groupVolume, _ingestedDate, _ingestedBy, _format));
        });
        // logger.info("list size: " + list.size());
        return list;
    }
}
