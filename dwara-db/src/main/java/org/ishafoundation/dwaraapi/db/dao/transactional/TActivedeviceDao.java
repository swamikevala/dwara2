package org.ishafoundation.dwaraapi.db.dao.transactional;

import java.util.List;

import org.ishafoundation.dwaraapi.db.model.transactional.TActivedevice;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.springframework.data.repository.CrudRepository;

public interface TActivedeviceDao extends CrudRepository<TActivedevice,Integer> {
	
	List<TActivedevice> findAllByDeviceDevicetypeAndDeviceStatus(Devicetype devicetype, DeviceStatus deviceStatus);
}