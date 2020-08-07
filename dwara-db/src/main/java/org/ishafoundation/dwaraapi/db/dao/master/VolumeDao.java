package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.springframework.data.repository.CrudRepository;

public interface VolumeDao extends CrudRepository<Volume,Integer> {
	
	List<Volume> findAllByVolumeRefIdAndFinalizedIsFalseOrderByIdAsc(String volumerefId);

//	Volume findTopByVolumesetIdAndFinalizedIsFalseOrderByIdAsc(int volumesetId);
//	
//	List<Volume> findAllByVolumesetIdAndFinalizedIsFalse(int volumesetId);
////	@Query(value="SELECT * FROM volume where volumesetId=?1 and finalized=false", nativeQuery=true)
////	List<Volume> getWritableVolumes(int volumesetId);
//	
//	List<Volume> findAllByVolumesetId(int volumesetId);
//	
	Volume findById(String id);
}