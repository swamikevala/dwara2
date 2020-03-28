package org.ishafoundation.dwaraapi.db.cacheutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LibraryclassCacheUtil {
	@Autowired
	private LibraryclassDao libraryclassDao;
	
	private List<Libraryclass> libraryclassList = null;
	private Map<Integer, Libraryclass> libraryclassId_libraryclass_Map = new HashMap<Integer, Libraryclass>();
	private Map<String, Libraryclass> libraryclassName_libraryclass_Map = new HashMap<String, Libraryclass>();

	@PostConstruct
	private void loadLibraryclassList() {
    	libraryclassList = (List<Libraryclass>) libraryclassDao.findAll();
		for (Libraryclass libraryclass : libraryclassList) {
			libraryclassId_libraryclass_Map.put(libraryclass.getId(), libraryclass);
			libraryclassName_libraryclass_Map.put(libraryclass.getName(), libraryclass);
		}
	}
	
	public List<Libraryclass> getLibraryclassList(){
		return libraryclassList;
	}
    
	public Libraryclass getLibraryclass(int libraryclassId) {
    	return libraryclassId_libraryclass_Map.get(libraryclassId);
    }
    
    public Libraryclass getLibraryclass(String libraryclassName) {
    	return libraryclassName_libraryclass_Map.get(libraryclassName);
    }
    
    public Set<Integer> getAllLibraryclassIds() {
    	return libraryclassId_libraryclass_Map.keySet();
    }	
}
