package org.ishafoundation.dwaraapi.db.dao.view;

import java.util.List;

import org.ishafoundation.dwaraapi.constants.Requesttype;
import org.ishafoundation.dwaraapi.db.keys.V_RestoreFileKey;
import org.ishafoundation.dwaraapi.db.model.view.V_RestoreFile;
import org.springframework.data.repository.CrudRepository;

public interface V_RestoreFileDao extends CrudRepository<V_RestoreFile,V_RestoreFileKey> {

	/*
We need to arrive at 
select * from v_restore_file 
where tapeset_copy_number = 2 and file_id=255 and requesttype_id in (8002,8004) and user_id = 21001;
	 */
	List<V_RestoreFile> findAllByTapesetCopyNumberAndIdFileIdAndIdRequesttypeInAndIdUserId(int copyNumber, int fileId, List<Requesttype> requesttype, int userId);
	
	V_RestoreFile findByTapesetCopyNumberAndIdFileIdAndIdRequesttypeAndIdUserId(int copyNumber, int fileId, Requesttype requesttype, int userId);
}