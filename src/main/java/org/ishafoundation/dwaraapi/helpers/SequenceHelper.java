package org.ishafoundation.dwaraapi.helpers;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.ingest.SequenceDao;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.ingest.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequenceHelper {
	
    @Autowired
    private LibraryclassDao libraryclassDao; 
    
    @Autowired
    private SequenceDao sequenceDao; 
    
	public String getPrefixedLastSequenceId(int libraryclassId){
		
        Sequence sequence = getLastSequence(libraryclassId);
		return getSeqId(sequence);
	}
	
	public Sequence getLastSequence(int libraryclassId){
		
        Libraryclass libraryclass = libraryclassDao.findById(libraryclassId).get();
        return getLastSequence(libraryclass);
	}

	public Sequence getLastSequence(Libraryclass libraryclass){
        int sequenceId = libraryclass.getSequenceId(); // getting the primary key of the Sequence table which holds the lastsequencenumber for this group...
        Sequence sequence = null;
        try {
        	sequence = sequenceDao.findById(sequenceId).get();
        }catch (Exception e) {
			// TODO: handle exception
        	System.out.println("Im here" + e.getMessage());
		}
        return sequence;
	}	
	
    public String getSeqId(Sequence sequence){
    	return (StringUtils.isNotBlank(sequence.getPrefix()) ? sequence.getPrefix() : "") + sequence.getLastNumber();
    }
    
    public String getNewSeqId(int libraryclassId){
	    Sequence sequence = getLastSequence(libraryclassId);
	    sequence.incrementLastNumber();
	    sequence = sequenceDao.save(sequence); // This would have incremented the counter
	    return getSeqId(sequence);
    }

}
