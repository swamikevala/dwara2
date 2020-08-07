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
	
    public List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> getArtifactFileList(Artifact artifact, Domain domain) throws Exception {
		String domainSpecificArtifactTableName = artifact.getClass().getSimpleName();
		String separator = "$";
		if(domainSpecificArtifactTableName.contains(separator)) {
			logger.warn("class name has $ separator : " + domainSpecificArtifactTableName);
			domainSpecificArtifactTableName = StringUtils.substringBefore(domainSpecificArtifactTableName, separator);
		}
		FileRepository<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> domainSpecificFileRepository = domainUtil.getDomainSpecificFileRepository(domain);
		Method fileDaoFindAllBy = domainSpecificFileRepository.getClass().getMethod(FileRepository.FIND_ALL_BY_ARTIFACT_ID.replace("<<DOMAIN_SPECIFIC_ARTIFACT>>", domainSpecificArtifactTableName), int.class);
		List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File> fileList = (List<org.ishafoundation.dwaraapi.db.model.transactional.domain.File>) fileDaoFindAllBy.invoke(domainSpecificFileRepository, artifact.getId());
		return fileList;
    }
}
