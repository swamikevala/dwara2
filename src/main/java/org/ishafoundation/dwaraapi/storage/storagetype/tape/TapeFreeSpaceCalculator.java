package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.jointables.LibraryTapeDao;
import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.LibraryTape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeFreeSpaceCalculator {

	@Autowired
	private LibraryTapeDao libraryTapeDao; 
	
	public long getTapeFreeSpace(Tape tape) {
		double originalTapeCapacity = tape.getTapetype().getCapacity();
		int tapeId = tape.getId();
		
		long usedSpace = libraryTapeDao.findUsedSpaceOnTape(tapeId);

//		float filesize_increase_rate = 12.5/100;
//		
//		filesize_increase_1
//		need to factor in these
//		filesize_increase_1 = tape used size * filesize_increase_rate
//		filesize_increase_2 = number of file_tape * filesize_increase_const

				
		return 0;
		
	}
}
