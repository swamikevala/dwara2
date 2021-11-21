package org.ishafoundation.dwaraapi.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.ishafoundation.dwaraapi.db.dao.master.ArtifactclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.LocationDao;
import org.ishafoundation.dwaraapi.db.dao.master.VolumeDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.RequestDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ArtifactCatalog;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.ProxyData;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TapeCatalog;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.RequestType;
import org.ishafoundation.dwaraapi.enumreferences.Status;
import org.ishafoundation.dwaraapi.enumreferences.VolumeHealthStatus;
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

    @Autowired
    LocationDao locationDao;

    @Autowired
    private ArtifactclassDao artifactclassDao;

    public List<Artifactclass> getAllArtifactclass() {
        List<Artifactclass> list = new ArrayList<Artifactclass>();
        artifactclassDao.findAll().forEach(list::add);
        return list;
    }

    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<Location>();
        locationDao.findAll().forEach(list::add);
        return list;
    }


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
        String query = "select volume_id, max(json_extract(details, '$.end_volume_block')) from dwara.artifact_volume group by volume_id;";
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

    public List<TapeCatalog> findTapesCatalog(String volumeId, String[] volumeGroup, String[] copyNumber, String[] format, String[] location, String startDate, String endDate, String[] healthStatus) {
        /* Query q2 = entityManager.createNativeQuery("select artifactclass_id, volume_id from artifactclass_volume where active=1");
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
        }); */

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
        if(healthStatus != null && healthStatus.length >=1 && !healthStatus[0].equals("all")){
            condition += " and a.healthStatus in (";
            for(String a: healthStatus) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(startDate != "")
            condition += " and a.finalized_at >= '" + startDate + "'";
        if(endDate != "")
            condition += " and a.finalized_at <= '" + endDate + "'";
        String query = "select group_concat(distinct c.artifactclass_id order by c.artifactclass_id separator ','), a.group_ref_id, a.id, a.archiveformat_id, a.location_id, a.initialized_at, a.capacity, a.imported, a.finalized, a.healthstatus, a.lifecyclestage, a.finalized_at, a.used_capacity" 
        + " from volume a join artifact_volume b join artifact c"
        + " where a.id = b.volume_id and b.artifact_id = c.id"
        + condition
        + " group by a.id"
        + " order by a.finalized_at desc";
        Query q = entityManager.createNativeQuery(query);
        // logger.info("mysql query: " + query);
        List<Object[]> results = q.getResultList();
        List<TapeCatalog> list = new ArrayList<TapeCatalog>();
        results.stream().forEach((record) -> {
            int i = 0;
            String _artifactClass = (String) record[i++];
            String _volumeGroup = (String) record[i++];
            String _volumeId = (String) record[i++];
            String _format = (String) record[i++];
            String _location = (String) record[i++];
            String _initializedAt = "";
            if(record[i] != null)
                _initializedAt = ((Timestamp) record[i]).toLocalDateTime().toString();
            i++;
            long _capacity = 0;
            if(record[i] != null)
                _capacity = ((BigInteger)record[i]).longValue();
            i++;
            boolean _isImported = (boolean)record[i++];
            boolean _isFinalized = (boolean)record[i++];
            String _volumeHealthStatus = (String) record[i++];
            String _volumeLifecycleStage = (String) record[i++];
            String _finalizedAt = "";
            if(record[i] != null)
                _finalizedAt = ((Timestamp) record[i]).toLocalDateTime().toString();
            i++;
            long _usedCapacity = 0;
            if(record[i] != null)
                _usedCapacity = ((BigInteger)record[i]).longValue();
            i++;
            List<String> artifactClass = Arrays.asList(_artifactClass.split(","));

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
                _finalizedAt, _usedCapacity, _capacity, artifactClass, _volumeHealthStatus, _volumeLifecycleStage));
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

        String query = "select a.id, d.id as requestId, a.artifact_ref_id, a.artifactclass_id, a.name, a.total_size, b.volume_id, d.status, c.imported, c.finalized_at, d.completed_at, e.name as ingestedBy, b.name as oldName" 
        + " from artifact a join artifact_volume b join volume c join request d join user e"
        + " where a.id=b.artifact_id and b.volume_id=c.id and ((a.write_request_id is not null and a.write_request_id=d.id) or (a.write_request_id is null and a.q_latest_request_id=d.id)) and d.requested_by_id=e.id and a.deleted=0"
        + condition
        + "order by b.volume_id asc, d.completed_at desc";

        Query q = entityManager.createNativeQuery(query);
        // logger.info("artifact query: " + query);
        List<Object[]> results = q.getResultList();
        List<ArtifactCatalog> list = new ArrayList<ArtifactCatalog>();
        results.stream().forEach((record) -> {
            list.add(handleArtifactCatalogData(record));
        });
        // logger.info("list size: " + list.size());

        return list;
    }

    public List<ArtifactCatalog> findArtifactsCatalog(String[] artifactClass, String[] volumeGroup, String[] copyNumber, String volumeId, String startDate, String endDate, String artifactName, boolean deleted, boolean softRenamed, String[] status) {
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
        /* if(copyNumber != null && copyNumber.length >= 1 && !copyNumber[0].equals("all")) {
            condition += " and substr(c.group_ref_id, 2, 1) in (";
            for(String a: copyNumber) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        } */
        if(status != null && status.length >=1 && !status[0].equals("all")){
            condition += " and d.status in (";
            for(String a: status) {
                condition += "'" + a + "',";
            }
            condition = condition.substring(0, condition.length() -1);
            condition += ")";
        }
        if(volumeId != "")
            condition += " and b.volume_id like '%" + volumeId + "%'";
        if(startDate != "")
            condition += " and d.requested_at >= '" + startDate + "'";
        if(endDate != "")
            condition += " and d.requested_at <= '" + endDate + "'";
        if(artifactName != "") {
            String name = artifactName.replaceAll(" ", "%");
            condition += " and a.name like '%" + name + "%'";
        }

        if(softRenamed) {
            condition += " and a.name != b.name";
        }
        String query = "select distinct a.id" 
        + " from artifact a join artifact_volume b join volume c join request d join user e"
        + " where a.id=b.artifact_id and b.volume_id=c.id and ((a.write_request_id is not null and a.write_request_id=d.id) or (a.write_request_id is null and a.q_latest_request_id=d.id)) and d.requested_by_id=e.id and a.artifact_ref_id is null and a.deleted=" + deleted
        + condition;

        String query2 = "select a.id, d.id as requestId, a.artifactclass_id, a.name, a.total_size, group_concat(b.volume_id order by b.volume_id separator ','), d.status, c.imported, c.finalized_at, d.requested_at, e.name as ingestedBy, group_concat(distinct b.name order by b.volume_id separator ',') as oldName" 
        + " from artifact a join artifact_volume b join volume c join request d join user e"
        + " where a.id=b.artifact_id and b.volume_id=c.id and ((a.write_request_id is not null and a.write_request_id=d.id) or (a.write_request_id is null and a.q_latest_request_id=d.id)) and d.requested_by_id=e.id"
        + " and a.id in (" + query + ")"
        + " group by a.id order by requested_at desc";
        Query q = entityManager.createNativeQuery(query2);
        logger.info("artifact query: " + query2);
        List<Object[]> results = q.getResultList();
        HashMap<Integer, ArtifactCatalog> mapArtifact = new HashMap<Integer, ArtifactCatalog>();
        results.stream().forEach((record) -> {
            ArtifactCatalog data = handleArtifactCatalogData(record);
            mapArtifact.put(data.artifactId, data);
        });
        // logger.info("list size: " + list.size());

        //Query proxy
        String query3 = "select a.artifact_ref_id, group_concat(b.volume_id order by b.volume_id separator ',') as volumeId, group_concat(f.status order by b.volume_id separator ',') as proxyStatus" 
        + " from artifact a join artifact_volume b on a.id=b.artifact_id join job f on f.input_artifact_id=a.artifact_ref_id"
        + " where f.processingtask_id='video-proxy-low-gen'"
        + " and a.artifact_ref_id in (" + query + ")"
        + " group by a.artifact_ref_id";
        Query qProxy = entityManager.createNativeQuery(query3);
        // logger.info("proxy query: " + query3);
        List<Object[]> results2 = qProxy.getResultList();
        HashMap<Integer, ProxyData> mapProxy = new HashMap<Integer, ProxyData>();
        results2.stream().forEach((record) -> {
            ProxyData data = handleProxyData(record);
            mapProxy.put(data.artifactId, data);
        });

        //Query mamupdate status
        String query4 = "select a.artifact_ref_id, status FROM job f join artifact a on f.input_artifact_id=a.id where  processingtask_id='video-mam-update' and a.artifact_ref_id in (" + query + ")";
        // logger.info("mam status query: " + query4);
        Query qMamUpdateStatus = entityManager.createNativeQuery(query4);
        List<Object[]> results3 = qMamUpdateStatus.getResultList();
        HashMap<Integer, String> mapMamStatus = new HashMap<Integer, String>();
        results3.forEach((record) -> {
            int _artifactId = ((Integer) record[0]).intValue();
            String _mamStatus = (String) record[1];
            mapMamStatus.put(_artifactId, _mamStatus);
        });

        return combineArtifactData(mapArtifact, mapProxy, mapMamStatus);
    }

    private ProxyData handleProxyData(Object[] record) {
        int i = 0;
        int _artifactId = ((Integer) record[i++]).intValue();
        String _volumeId = (String) record[i++];
        String _proxyStatus = (String) record[i++];

        return new ProxyData(_artifactId, _volumeId, _proxyStatus);
    }

    private ArtifactCatalog handleArtifactCatalogData(Object[] record) {
        int i = 0;
        int _artifactId = ((Integer) record[i++]).intValue();
        int _requestId = ((Integer) record[i++]).intValue();
        String _artifactClass = (String) record[i++];
        String _artifactName = (String) record[i++];
        long _size = ((BigInteger)record[i++]).longValue();
        String _volumeId = (String) record[i++];
        String _requestStatus = (String) record[i++];
        boolean _isImported = (boolean)record[i++];
        
        String _finalizedAt = "";
        if(record[i] != null)
            _finalizedAt = ((Timestamp) record[i]).toLocalDateTime().toString();
        i++;
        
        String _requestDate = ""; // could be either request.requested_at or request.completed_at depeding on context of the method call
        if(record[i] != null)
        	_requestDate = ((Timestamp) record[i]).toLocalDateTime().toString();
        i++;
        
        String _ingestedDate = "";
        if(_isImported)
        	_ingestedDate = _finalizedAt;
        else
        	_ingestedDate = _requestDate;
        
        String _ingestedBy = (String) record[i++];
        String _oldName = (String) record[i++];
        String _proxyStatus = "";
        if(record.length > i) {
            _proxyStatus = (String)record[i];
        }

        ArtifactCatalog ac = new ArtifactCatalog(_artifactId, _requestId, _artifactClass, _artifactName, _size, _volumeId, _requestStatus, _ingestedDate, _ingestedBy, _oldName);
        ac.proxyStatus = _proxyStatus;
        return ac;
    }

    private List<ArtifactCatalog> combineArtifactData(HashMap<Integer, ArtifactCatalog> mapArtifact, HashMap<Integer, ProxyData> mapProxy, HashMap<Integer, String> mapMamStatus) {
        List<ArtifactCatalog> result = new ArrayList<ArtifactCatalog>();

        for(Map.Entry<Integer, ArtifactCatalog> entry : mapArtifact.entrySet()) {
            Integer id = entry.getKey();
            if(mapProxy.containsKey(id)) {
                entry.getValue().proxyVolumeId = mapProxy.get(id).proxyVolumeId;
                entry.getValue().proxyStatus = mapProxy.get(id).proxyStatus;
            }
            if(mapMamStatus.containsKey(id)) {
                entry.getValue().mamUpdateStatus = mapMamStatus.get(id);
            }
            result.add(entry.getValue());
        }
        
        return result;
    }
}