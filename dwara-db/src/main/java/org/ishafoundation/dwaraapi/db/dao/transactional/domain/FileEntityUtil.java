package org.ishafoundation.dwaraapi.db.dao.transactional.domain;

import java.lang.reflect.Method;

import org.ishafoundation.dwaraapi.db.model.transactional.domain.Artifact;
import org.ishafoundation.dwaraapi.db.model.transactional.domain.File;
import org.ishafoundation.dwaraapi.db.utils.DomainUtil;
import org.ishafoundation.dwaraapi.enumreferences.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileEntityUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileEntityUtil.class);
	
	@Autowired
	private DomainUtil domainUtil;

    public Artifact getArtifact(File file, Domain domain) throws Exception {
		Method getArtifact = file.getClass().getMethod("getArtifact"+domainUtil.getDomainId(domain));
		return (Artifact) getArtifact.invoke(file);
    }
	
    public File getFileRef(File file, Domain domain) throws Exception {
		Method getFileRef = file.getClass().getMethod("getFile"+domainUtil.getDomainId(domain)+"Ref");
		return (File) getFileRef.invoke(file);
    }
    
    public void setDomainSpecificFileRef(File file, File fileRef) throws Exception {
	    Method fileRefSetter = file.getClass().getMethod("set" + fileRef.getClass().getSimpleName() + "Ref", fileRef.getClass());
	    fileRefSetter.invoke(file, fileRef);
    }
    
    public void setDomainSpecificFileArtifact(File file, Artifact artifact) throws Exception {
        Method fileArtifactSetter = file.getClass().getMethod("set" + artifact.getClass().getSimpleName(), artifact.getClass());
    	fileArtifactSetter.invoke(file, artifact);
    }
}
