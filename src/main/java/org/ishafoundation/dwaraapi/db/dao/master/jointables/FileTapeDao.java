package org.ishafoundation.dwaraapi.db.dao.master.jointables;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.FileTape;
import org.springframework.data.repository.CrudRepository;

public interface FileTapeDao extends CrudRepository<FileTape,Integer> {
	
	// FileTape findByFileIdAndCopyNumber(int fileIdToBeRestored, int copyNumber);
	
	// TODO _ this might give multiple values - need copynumber to narrow it down
	//FileTape findByFileId(int fileIdToBeRestored);
	
	FileTape findByFileIdAndTapeId(int fileIdToBeRestored, int tapeId);
	
	List<FileTape> findAllByFileId(int fileIdToBeRestored);
	
	List<FileTape> findAllByTapeId(int tapeId);
}