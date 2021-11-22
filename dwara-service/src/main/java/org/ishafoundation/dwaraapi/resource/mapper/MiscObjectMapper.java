package org.ishafoundation.dwaraapi.resource.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MiscObjectMapper {
    
	@Mappings({
        @Mapping(source = "id", target = "artifactId"),
        @Mapping(target = "artifactclass", ignore=true),
        @Mapping(source = "fileStructureMd5", target = "md5")
    })	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	org.ishafoundation.dwaraapi.api.resp.staged.ingest.Artifact getArtifactForIngestResponse(org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact);

	@Mappings({
        @Mapping(source = "id", target = "artifactId"),
        @Mapping(target = "artifactclass", ignore=true),
        @Mapping(source = "fileStructureMd5", target = "md5")
    })
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	org.ishafoundation.dwaraapi.api.resp.artifact.Artifact getArtifactForArtifactResponse(org.ishafoundation.dwaraapi.db.model.transactional.Artifact artifact);

}
