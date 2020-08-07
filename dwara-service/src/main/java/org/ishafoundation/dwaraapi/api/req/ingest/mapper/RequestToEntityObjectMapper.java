package org.ishafoundation.dwaraapi.api.req.ingest.mapper;

import org.ishafoundation.dwaraapi.api.req.format.FormatRequest;
import org.ishafoundation.dwaraapi.api.req.ingest.RequestParams;
import org.ishafoundation.dwaraapi.api.req.restore.FileParams;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RequestToEntityObjectMapper {
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetails(RequestParams requestParams);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetails(FileParams fileParams);
	
	@Mappings({
        @Mapping(source = "volumeId", target = "volume_id"),
        @Mapping(source = "volumeGroupId", target = "volume_group_id"),
        @Mapping(source = "volumeBlocksize", target = "volume_blocksize")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetails(FormatRequest formatRequest);
	
	
	
}

