package org.ishafoundation.dwaraapi.db.dao.master;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.springframework.data.repository.CrudRepository;

public interface DeviceDao extends CrudRepository<Device,String>{//CacheableRepository<Device>{//CrudRepository<Device,String>{
	
	Device findByWwnId(String deviceName);
	
	List<Device> findAllByTypeAndStatusAndDefectiveIsFalse(Devicetype devicetype, DeviceStatus status);
	
	List<Device> findAllByType(Devicetype devicetype);
	
}