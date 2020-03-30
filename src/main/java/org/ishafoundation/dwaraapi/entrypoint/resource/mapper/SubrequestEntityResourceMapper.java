package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubrequestEntityResourceMapper {
	 org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest entityToResource(org.ishafoundation.dwaraapi.db.model.transactional.Subrequest entity);
	
	 org.ishafoundation.dwaraapi.db.model.transactional.Subrequest resourceToEntity(org.ishafoundation.dwaraapi.entrypoint.resource.Subrequest resource);
}
