package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.RequestApproval;
import org.ishafoundation.dwaraapi.db.model.transactional.TRestoreBucket;
import org.springframework.data.repository.CrudRepository;

public interface RequestApprovalDao extends CrudRepository<RequestApproval,String> {
}
