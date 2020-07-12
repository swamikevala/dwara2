package org.ishafoundation.dwaraapi.db.utils;

import java.util.Map;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.DomainAttributeConverter;
import org.ishafoundation.dwaraapi.db.dao.master.DomainDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.ArtifactRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.domain.FileRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.ArtifactVolumeRepository;
import org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain.FileVolumeRepository;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificArtifactVolumeFactory;
import org.ishafoundation.dwaraapi.db.domain.factory.DomainSpecificFileVolumeFactory;
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
	
	@SuppressWarnings("rawtypes")
	public ArtifactRepository getDomainSpecificArtifactRepository(Domain domain) {
		String domainName = getDomainName(domain);

		
		String domainSpecificArtifactName = Artifact.TABLE_NAME_PREFIX + domainName;
		return (ArtifactRepository) artifactDaoMap.get(domainSpecificArtifactName + daoSuffix);
	}
	
	public Artifact getDomainSpecificArtifact(Domain domain, int artifactId) {
		Artifact artifact = (Artifact) getDomainSpecificArtifactRepository(domain).findById(artifactId).get();
		return artifact;
	}
	
	@SuppressWarnings("rawtypes")
	public FileRepository getDomainSpecificFileRepository(Domain domain) {
		String domainName = getDomainName(domain);
		
		String domainSpecificFileName = File.TABLE_NAME_PREFIX + domainName;
		return (FileRepository) fileDaoMap.get(domainSpecificFileName + daoSuffix);
	}
	
	public File getDomainSpecificFile(Domain domain, int fileId) {
		File file = (File) getDomainSpecificFileRepository(domain).findById(fileId).get();
		return file;
	}
	
	@SuppressWarnings("rawtypes")
	public ArtifactVolumeRepository getDomainSpecificArtifactVolumeRepository(Domain domain) {
		String domainName = getDomainName(domain);

		
		String domainSpecificArtifactVolumeName = Artifact.TABLE_NAME_PREFIX + domainName + volumeSuffix;
		return (ArtifactVolumeRepository) artifactVolumeDaoMap.get(domainSpecificArtifactVolumeName + daoSuffix);
	}
	
	public ArtifactVolume getDomainSpecificArtifactVolume(Domain domain, int artifactVolumeId) {
		ArtifactVolume artifactVolume = (ArtifactVolume) getDomainSpecificArtifactVolumeRepository(domain).findById(artifactVolumeId).get();
		return artifactVolume;
	}

	public ArtifactVolume getDomainSpecificArtifactVolume(Domain domain, int artifactId, int volumeId) {
		ArtifactVolume artifactVolume = (ArtifactVolume) getDomainSpecificArtifactVolumeRepository(domain).findByIdArtifactIdAndIdVolumeId(artifactId, volumeId);
		return artifactVolume;
	}
	
	public ArtifactVolume getDomainSpecificArtifactVolumeInstance(Domain domain, int artifactId, Volume volume) {
		return DomainSpecificArtifactVolumeFactory.getInstance(domain, artifactId, volume);	
	}
	
	@SuppressWarnings("rawtypes")
	public FileVolumeRepository getDomainSpecificFileVolumeRepository(Domain domain) {
		String domainName = getDomainName(domain);
		
		String domainSpecificFileVolumeName = File.TABLE_NAME_PREFIX + domainName + volumeSuffix;
		return (FileVolumeRepository) fileVolumeDaoMap.get(domainSpecificFileVolumeName + daoSuffix);
	}
	
	public FileVolume getDomainSpecificFileVolume(Domain domain, int fileVolumeId) {
		FileVolume fileVolume = (FileVolume) getDomainSpecificFileVolumeRepository(domain).findById(fileVolumeId).get();
		return fileVolume;
	}
	
	public FileVolume getDomainSpecificFileVolume(Domain domain, int fileId, int volumeId) {
		FileVolume fileVolume = (FileVolume) getDomainSpecificFileVolumeRepository(domain).findByIdFileIdAndIdVolumeId(fileId, volumeId);
		return fileVolume;
	}
	
	public FileVolume getDomainSpecificFileVolumeInstance(Domain domain, int fileId, Volume volume) {
		return DomainSpecificFileVolumeFactory.getInstance(domain, fileId, volume);
	}
	
	private String getDomainName(Domain domain) {
		String domainName = null;
		if(domain == null) { // If domain is not available default it
			org.ishafoundation.dwaraapi.db.model.master.configuration.Domain domainFromDB = domainDao.findByDefaultTrue();
			domainName = domainFromDB.getName();
		}
		else {
			domainName = domainAttributeConverter.convertToDatabaseColumn(domain);
		}
		return domainName;
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
