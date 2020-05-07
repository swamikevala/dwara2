package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.IngestFile;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SingleReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MiscObjectMapper {

	@Mappings({
        @Mapping(source = "name", target = "libraryName")
    })
	IngestFile libraryParamsToIngestFile(LibraryParams libraryParams);
	
	List<SingleReference> statusListToSingleReferenceList(List<Status> statusList);
	
	List<SingleReference> actionListToSingleReferenceList(List<Action> actionList);
	
}
