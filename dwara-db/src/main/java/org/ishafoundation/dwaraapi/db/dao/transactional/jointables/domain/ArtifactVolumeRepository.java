package org.ishafoundation.dwaraapi.db.dao.transactional.jointables.domain;

import org.ishafoundation.dwaraapi.db.model.transactional.jointables.domain.ArtifactVolume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ArtifactVolumeRepository<T extends ArtifactVolume> extends CrudRepository<T,Integer> {

	//SELECT sum(library.total_size) FROM library_volume join library on library_volume.library_id = library.id where volume_id=12002;
//	@Query("select sum(l.totalSize) from #{#entityName} lt join Library l on lt.library.id=l.id where lt.volume.id = ?1")
//	long findUsedSpaceOnVolume(int volumeId);
	
	ArtifactVolume findTopByVolumeIdOrderByIdDesc(int volumeId);
}