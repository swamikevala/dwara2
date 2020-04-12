package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.springframework.data.repository.CrudRepository;

public interface TargetvolumeDao extends CrudRepository<Targetvolume, Integer> {
	
	List<Targetvolume> findAllByLibraryclassTargetvolumeLibraryclassId(int libraryclassId);

	Targetvolume findByName(String targetvolumeName);
}