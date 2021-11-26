package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.RestoreBucketFile;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TRestoreBucketDao extends CrudRepository<TRestoreBucket,String> {
    List<TRestoreBucket> findByApprovalStatusNull();
    List<TRestoreBucket> findByApprovalStatusNullAndCreatedBy( int userName);
    List<TRestoreBucket> findByApprovalStatus(String status);
    List<TRestoreBucket> findByOrderByCreatedAtDesc();
}
