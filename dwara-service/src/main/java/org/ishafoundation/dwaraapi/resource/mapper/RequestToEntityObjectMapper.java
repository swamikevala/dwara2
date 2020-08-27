package org.ishafoundation.dwaraapi.resource.mapper;

import org.ishafoundation.dwaraapi.api.req.initialize.InitializeUserRequest;
import org.ishafoundation.dwaraapi.api.req.staged.ingest.StagedFile;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RequestToEntityObjectMapper {
	
	@Mappings({
        @Mapping(source = "path", target = "stagedFilepath"),
        @Mapping(source = "name", target = "stagedFilename")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetailsForIngest(StagedFile stagedFile);
	
	@Mappings({
        @Mapping(source = "volume", target = "volume_id"),
        @Mapping(source = "volumeGroup", target = "volume_group_id"),
        @Mapping(source = "volumeBlocksize", target = "volume_blocksize")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetailsForInitialize(InitializeUserRequest initializeUserRequest);
	
}

