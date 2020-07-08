package org.ishafoundation.dwaraapi.api.req.ingest.mapper;

import org.ishafoundation.dwaraapi.api.req.ingest.RequestParams;
import org.ishafoundation.dwaraapi.api.req.restore.FileParams;
import org.ishafoundation.dwaraapi.db.model.transactional.json.RequestDetails;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RequestToEntityObjectMapper {
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetails(RequestParams requestParams);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	RequestDetails getRequestDetails(FileParams fileParams);
	
	
}

