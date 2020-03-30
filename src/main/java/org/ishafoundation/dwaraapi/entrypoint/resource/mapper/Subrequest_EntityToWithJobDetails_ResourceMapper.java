package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.api.resp.ingest.IngestFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Subrequest_EntityToWithJobDetails_ResourceMapper {
	IngestFile entityToResource(LibraryParams libraryParams);
	
	 org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails entityToResource(org.ishafoundation.dwaraapi.db.model.transactional.Subrequest entity);
	
	 org.ishafoundation.dwaraapi.db.model.transactional.Subrequest resourceToEntity(org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails resource);
}
