package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileRepositoryUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileRepositoryUtil.class);
	
	@Autowired
	private DomainUtil domainUtil;
	
    public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getAllArtifactFileList(Artifact artifact, Domain domain) throws Exception {
		return getArtifactFileList(artifact, domain, true);
    }
    
    public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getArtifactFileList(Artifact artifact, Domain domain) throws Exception {
    	return getArtifactFileList(artifact, domain, false);
    }
    
    private List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getArtifactFileList(Artifact artifact, Domain domain, boolean includeDeleted) throws Exception {
		String domainSpecificArtifactTableName = artifact.getClass().getSimpleName();
		String separator = "$";
		if(domainSpecificArtifactTableName.contains(separator)) {
			logger.warn("class name has $ separator : " + domainSpecificArtifactTableName);
			domainSpecificArtifactTableName = StringUtils.substringBefore(domainSpecificArtifactTableName, separator);
		}
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		Method fileDaoFindAllBy = null;
		if(includeDeleted)
			fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", domainSpecificArtifactTableName), int.class);
		else
			fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID_AND_DELETED_FALSE.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", domainSpecificArtifactTableName), int.class);
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifact.getId());
		return fileList;
    }
    
    public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getAllDerivedFiles(org.ishafoundation.dwaraapi.db.model.transactional.domain.File file, Domain domain) throws Exception {
		String domainSpecificFileTableName = file.getClass().getSimpleName();
		String separator = "$";
		if(domainSpecificFileTableName.contains(separator)) {
			logger.warn("class name has $ separator : " + domainSpecificFileTableName);
			domainSpecificFileTableName = StringUtils.substringBefore(domainSpecificFileTableName, separator);
		}
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_FILE_REF_ID.replace("<<DOMAIN_SPECIFIC_FILE>>", domainSpecificFileTableName), int.class);
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, file.getId());
		return fileList;
    }
}
