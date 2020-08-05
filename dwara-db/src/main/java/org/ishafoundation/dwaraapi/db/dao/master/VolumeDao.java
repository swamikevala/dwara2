package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.springframework.data.repository.CrudRepository;

public interface VolumeDao extends CrudRepository<Volume,Integer> {
	
	List<Volume> findAllByVolumeRefIdAndFinalizedIsFalseOrderByUidAsc(int volumerefId);

//	Volume findTopByVolumesetIdAndFinalizedIsFalseOrderByIdAsc(int volumesetId);
//	
//	List<Volume> findAllByVolumesetIdAndFinalizedIsFalse(int volumesetId);
////	@Query(value="SELECT * FROM volume where volumesetId=?1 and finalized=false", nativeQuery=true)
////	List<Volume> getWritableVolumes(int volumesetId);
//	
//	List<Volume> findAllByVolumesetId(int volumesetId);
//	
	Volume findTopByOrderByIdDesc(); // when a format_volume action is triggered we need to add the formatted volume to our system with the most last volume's Id + 1...
	
	Volume findByUid(String uId);
}