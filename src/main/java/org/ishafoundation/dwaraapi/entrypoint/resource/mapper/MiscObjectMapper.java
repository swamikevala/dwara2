package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import java.util.List;

import org.ishafoundation.dwaraapi.api.req.ingest.LibraryParams;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.model.master.reference.Status;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.IngestFile;
import org.ishafoundation.dwaraapi.entrypoint.resource.ingest.SingleReference;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MiscObjectMapper {

	IngestFile libraryParamsToIngestFile(LibraryParams libraryParams);
	
	List<SingleReference> statusListToSingleReferenceList(List<Status> statusList);
	
	List<SingleReference> actionListToSingleReferenceList(List<Action> actionList);
	
}
