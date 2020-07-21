package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;

public interface DeviceDao extends CacheableRepository<Device> {
	
	Device findByUid(String deviceName);
	
	List<Device> findAllByDevicetype(Devicetype devicetype);
	
}