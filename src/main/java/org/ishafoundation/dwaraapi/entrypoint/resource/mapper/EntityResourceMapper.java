package org.ishafoundation.dwaraapi.entrypoint.resource.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EntityResourceMapper {
    @BeforeMapping
    default void setSecondaryActionRelatedIdFields(@MappingTarget org.ishafoundation.dwaraapi.entrypoint.resource.Request resourceRequest, org.ishafoundation.dwaraapi.db.model.transactional.Request modelRequest) {
    	Integer requestId = modelRequest.getRequestRef() != null ? modelRequest.getRequestRef().getId() : null;
    	Integer subrequestId = modelRequest.getSubrequest() != null ? modelRequest.getSubrequest().getId() : null;
    	Integer libraryId = modelRequest.getLibrary() != null ? modelRequest.getLibrary().getId() : null;
    	Integer jobId = modelRequest.getJob() != null ? modelRequest.getJob().getId() : null;
    	resourceRequest.setRequestId(requestId);
    	resourceRequest.setSubrequestId(subrequestId);
    	resourceRequest.setLibraryId(libraryId);
    	resourceRequest.setJobId(jobId);
    }
    
//	@Mappings({
//        @Mapping(source = "libraryclass.name", target = "libraryclassName"),
//        @Mapping(source = "user.name", target = "requestedBy"),
//        @Mapping(source = "targetvolume.name", target = "targetvolumeName")
//    })
//	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
//	org.ishafoundation.dwaraapi.entrypoint.resource.Request getRequestResource(org.ishafoundation.dwaraapi.db.model.transactional.Request entity);

	@Mappings({
        @Mapping(source = "libraryclass.name", target = "libraryclassName"),
        @Mapping(source = "user.name", target = "requestedBy"),
        @Mapping(source = "targetvolume.name", target = "targetvolumeName")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	org.ishafoundation.dwaraapi.entrypoint.resource.RequestWithSubrequestDetails getRequestWithSubrequestDetailsResource(org.ishafoundation.dwaraapi.db.model.transactional.Request entity);
	
//	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
//	org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithRequestDetails getSubrequestWithRequestDetailsResource(org.ishafoundation.dwaraapi.db.model.transactional.Subrequest entity);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	org.ishafoundation.dwaraapi.entrypoint.resource.SubrequestWithJobDetails getSubrequestWithJobDetailsResource(org.ishafoundation.dwaraapi.db.model.transactional.Subrequest entity);

	@Mappings({
        @Mapping(source = "task.name", target = "taskName")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	org.ishafoundation.dwaraapi.entrypoint.resource.Job getJobResource(org.ishafoundation.dwaraapi.db.model.transactional.Job entity);
}

