package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Subrequest_EntityToWithJobDetailsResource_Mapper {
	
	 org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails entityToResource(org.ishafoundation.dwaraapi.db.model.transactional.Subrequest entity);
	
	 org.ishafoundation.dwaraapi.db.model.transactional.Subrequest resourceToEntity(org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SubrequestWithJobDetails resource);
}
