package org.ishafoundation.dwaraapi.db.utils;

import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DomainDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DomainUtil {

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private DomainAttributeConverter domainAttributeConverter;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, ArtifactRepository> artifactDaoMap;

	
	public Artifact getDomainSpecificArtifact(Domain domain, int artifactId) {
		String domainName = null;
		if(domain == null) { // If domain is not available default it
			org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaulttTrue();
			domainName = domainFromDB.getName();

		}
		else {
			domainName = domainAttributeConverter.convertToDatabaseColumn(domain);
		}
		
		String domainSpecificArtifactName = "artifact" + domainName;
		Artifact artifact = (Artifact) artifactDaoMap.get(domainSpecificArtifactName + "Dao").findById(artifactId).get();

		return artifact;
	}
	
//	public List<File> getDomainSpecificLibraryFiles(Domain domain, int libraryId) {
//		List<File> libraryFileList = null;
//		if(domain == Domain.one)
//			libraryFileList = fileDao.findAllByLibraryId(libraryId);
//		else
//			libraryFileList = file2Dao.findAllByLibraryId(libraryId);
//		
//		return 
//	}
}
