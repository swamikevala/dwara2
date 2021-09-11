package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.model.transactional.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileRepositoryUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileRepositoryUtil.class);
	
	@Autowired
	private FileRepository fileRepository;
	
    public List<org.ishafoundation.dwaraapi.db.model.transactional.File> getAllArtifactFileList(Artifact artifact) throws Exception {
		return getArtifactFileList(artifact,  true);
    }
    
    public List<org.ishafoundation.dwaraapi.db.model.transactional.File> getArtifactFileList(Artifact artifact) throws Exception {
    	return getArtifactFileList(artifact,  false);
    }
    
    private List<org.ishafoundation.dwaraapi.db.model.transactional.File> getArtifactFileList(Artifact artifact,  boolean includeDeleted) throws Exception {
		String domainSpecificArtifactTableName = artifact.getClass().getSimpleName();
		String separator = "$";
		if(domainSpecificArtifactTableName.contains(separator)) {
			logger.warn("class name has $ separator : " + domainSpecificArtifactTableName);
			domainSpecificArtifactTableName = StringUtils.substringBefore(domainSpecificArtifactTableName, separator);
		}
		//FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		//Method fileDaoFindAllBy = null;
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList;
		
		if(includeDeleted)
			fileList = fileRepository.findAllByArtifactId(artifact.getId());
		else
			fileList = fileRepository.findAllByArtifactIdAndDeletedFalse(artifact.getId());
		//List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(fileRepository, artifact.getId());
		return fileList;
    }
    public List<org.ishafoundation.dwaraapi.db.model.transactional.File> getAllDerivedFiles(org.ishafoundation.dwaraapi.db.model.transactional.File file) throws Exception {
		String domainSpecificFileTableName = file.getClass().getSimpleName();
		String separator = "$";
		if(domainSpecificFileTableName.contains(separator)) {
			logger.warn("class name has $ separator : " + domainSpecificFileTableName);
			domainSpecificFileTableName = StringUtils.substringBefore(domainSpecificFileTableName, separator);
		}
		//FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		//Method fileDaoFindAllBy = fileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_FILE_REF_ID.replace("<<DOMAIN_SPECIFIC_FILE>>", domainSpecificFileTableName), int.class);
		//List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.File>) fileDaoFindAllBy.invoke(fileRepository, file.getId());
		List<org.ishafoundation.dwaraapi.db.model.transactional.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.File>) fileRepository.findAllByFileRefId(file.getId());

		return fileList;
    }
    
}
