package org.ishafoundation.dwaraapi.db.dao.view;

import java.util.List;

import org.ishafoundation.dwaraapi.constants.Requesttype;
import org.ishafoundation.dwaraapi.db.model.view.V_RestoreFile;
import org.springframework.data.repository.CrudRepository;

public interface V_RestoreFileDao extends CrudRepository<V_RestoreFile,Integer> {

	/*
We need to arrive at 
select file.id, file.name, file.size, library.libraryclass_id, tape.barcode, libraryclass_requesttype_user.requesttype_id, from v_restore_file 
where copy_number = 2 and file_id=123 and requesttype_id in (2,10) and user_id = 1234;
	 */
	List<V_RestoreFile> findAllByCopyNumberAndFileIdAndRequesttypeInAndUserId(int copyNumber, int fileId, List<Requesttype> requesttype, int userId);
}