package org.ishafoundation.dwaraapi.utils;

import java.util.Set;
import java.util.TreeSet;

import org.ishafoundation.dwaraapi.db.dao.master.ExtensionDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtensionsUtil {

	@Autowired
	private ExtensionDao extensionDao;
	
	// gets all supported extensions in the system
	public Set<String> getAllSupportedExtensions(){
		Iterable<Extension> extensionList = extensionDao.findAll();
		Set<String> supportedExtns =  new TreeSet<String>();
		for (Extension extension : extensionList) {
			supportedExtns.add(extension.getId().toUpperCase());
			supportedExtns.add(extension.getId().toLowerCase());
		}
		return supportedExtns;
	}
}
