package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.api.resp.ingest.IngestFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MiscObjectMapper {

	IngestFile libraryParamsToIngestFile(LibraryParams libraryParams);
	
}
