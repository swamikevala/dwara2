package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.springframework.data.repository.CrudRepository;

public interface TActivedeviceDao extends CrudRepository<TActivedevice,Integer> {
	
	TActivedevice findByDeviceId(String deviceId);
}