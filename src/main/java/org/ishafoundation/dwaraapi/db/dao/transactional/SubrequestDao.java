package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Subrequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SubrequestDao extends CrudRepository<Subrequest,Integer> {
	
	List<Subrequest> findAllByRequestId(int requestId);
	
	//List<Subrequest> findAllByRequesttypeId(int requesttypeId);
	
	@Query("SELECT sr FROM Subrequest sr WHERE sr.statusId != 3 AND sr.statusId != 4 order by sr.subrequestId desc")
	List<Subrequest> findProcessing();

    // PARTIAL_COMPLETED or COMPLETED
	@Query("SELECT sr FROM Subrequest sr WHERE sr.statusId = 3 OR sr.statusId = 4 order by sr.subrequestId desc")
	List<Subrequest> findCompleted();
}