package org.ishafoundation.dwaraapi.db.utils;

import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DomainDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactVolumeFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileVolumeFactory;
import org.ishafoundation.dwaraapi.db.model.transactional.Request;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.FileVolume;
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

	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, ArtifactVolumeRepository> artifactVolumeDaoMap;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, FileRepository> fileDaoMap;

	@SuppressWarnings("rawtypes")
	@Autowired
	private Map<String, FileVolumeRepository> fileVolumeDaoMap;
	
	private String volumeSuffix = "Volume";
	private String daoSuffix = "Dao";
	
	/*** Artifact ***/
	public Artifact getDomainSpecificArtifactInstance(Domain domain) {
		return DomainSpecificArtifactFactory.getInstance(domain);
	}

	public Artifact getDomainSpecificArtifact(Domain domain, int artifactId) {
		Artifact artifact = (Artifact) getDomainSpecificArtifactRepository(domain).findById(artifactId).get();
		return artifact;
	}
	
	@SuppressWarnings("rawtypes")
	public ArtifactRepository getDomainSpecificArtifactRepository(Domain domain) {
		int domainId = getDomainId(domain);

		
		String domainSpecificArtifactName = Artifact.TABLE_NAME_PREFIX + domainId;
		return (ArtifactRepository) artifactDaoMap.get(domainSpecificArtifactName + daoSuffix);
	}
	
	/*** File ***/
	public File getDomainSpecificFileInstance(Domain domain) {
		return DomainSpecificFileFactory.getInstance(domain);
	}

	public File getDomainSpecificFile(Domain domain, int fileId) {
		File file = (File) getDomainSpecificFileRepository(domain).findById(fileId).get();
		return file;
	}

	@SuppressWarnings("rawtypes")
	public FileRepository getDomainSpecificFileRepository(Domain domain) {
		int domainId = getDomainId(domain);
		
		String domainSpecificFileName = File.TABLE_NAME_PREFIX + domainId;
		return (FileRepository) fileDaoMap.get(domainSpecificFileName + daoSuffix);
	}

	/*** ArtifactVolume ***/
	public ArtifactVolume getDomainSpecificArtifactVolumeInstance(int artifactId, Volume volume, Domain domain) { // Domain is 3rd param in method signature so that getDomainSpecificArtifactVolume and getDomainSpecificArtifactVolumeInstance are not confused for 
		return DomainSpecificArtifactVolumeFactory.getInstance(domain, artifactId, volume);	
	}

	public ArtifactVolume getDomainSpecificArtifactVolume(Domain domain, int artifactId, String volumeId) {
		ArtifactVolume artifactVolume = (ArtifactVolume) getDomainSpecificArtifactVolumeRepository(domain).findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);
		return artifactVolume;
	}

	@SuppressWarnings("rawtypes")
	public ArtifactVolumeRepository getDomainSpecificArtifactVolumeRepository(Domain domain) {
		int domainId = getDomainId(domain);

		
		String domainSpecificArtifactVolumeName = Artifact.TABLE_NAME_PREFIX + domainId + volumeSuffix;
		return (ArtifactVolumeRepository) artifactVolumeDaoMap.get(domainSpecificArtifactVolumeName + daoSuffix);
	}
	
	/*** FileVolume ***/
	public FileVolume getDomainSpecificFileVolumeInstance(int fileId, Volume volume, Domain domain) { // Domain is 3rd param in method signature so that getDomainSpecificFileVolume and getDomainSpecificFileVolumeInstance are not confused for
		return DomainSpecificFileVolumeFactory.getInstance(domain, fileId, volume);
	}

	public FileVolume getDomainSpecificFileVolume(Domain domain, int fileId, String volumeId) {
		FileVolume fileVolume = (FileVolume) getDomainSpecificFileVolumeRepository(domain).findByIdFileIdAndIdVolumeId(fileId, volumeId);
		return fileVolume;
	}

	@SuppressWarnings("rawtypes")
	public FileVolumeRepository getDomainSpecificFileVolumeRepository(Domain domain) {
		int domainId = getDomainId(domain);
		
		String domainSpecificFileVolumeName = File.TABLE_NAME_PREFIX + domainId + volumeSuffix;
		return (FileVolumeRepository) fileVolumeDaoMap.get(domainSpecificFileVolumeName + daoSuffix);
	}
	

	public int getDomainId(Domain domain) {
		int domainId;
		if(domain == null) { // If domain is not available default it
			org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaultTrue();
			domainId = domainFromDB.getId();
		}
		else {
			domainId = domainAttributeConverter.convertToDatabaseColumn(domain);
		}
		return domainId;
	}
	
	public Domain getDefaultDomain() {
		org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaultTrue();
		return domainAttributeConverter.convertToEntityAttribute(domainFromDB.getId());
	}
	
	public Domain getDomain(Integer domainId) {
		Domain domain = null;
		if(domainId != null)
			domain = domainAttributeConverter.convertToEntityAttribute(domainId);
		else {
			domain = getDefaultDomain();
		}
		return domain;
	}
	
	public Domain getDomain(Request request) {
		Integer domainId = request.getDetails().getDomainId();
		return getDomain(domainId);
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
