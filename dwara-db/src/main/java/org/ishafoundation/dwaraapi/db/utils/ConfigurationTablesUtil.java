package org.ishafoundation.dwaraapi.db.utils;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.enumreferences.Action;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


//public enum CacheableTablesList {
//	
//	// reference tables
//	action,
//	
//	// configuration tables
//	archiveformat,
//	artifactclass,
//	destination,
//	device,
//	//domain,
//	extension,
//	location
//	//sequence
//}

@Component
public class ConfigurationTablesUtil {
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@Autowired
	private ActionAttributeConverter actionAttributeConverter;

	public org.ishafoundation.dwaraapi.db.model.master.reference.Action getAction(Action requestedBusinessAction){
		return (org.ishafoundation.dwaraapi.db.model.master.reference.Action) dBMasterTablesCacheManager.getRecord(CacheableTablesList.action.name(), actionAttributeConverter.convertToDatabaseColumn(requestedBusinessAction));
	}
	
	public Archiveformat getArchiveformat(String archiveformatId) {
		return (Archiveformat) dBMasterTablesCacheManager.getRecord(CacheableTablesList.archiveformat.name(), archiveformatId);
	}

	public List<Artifactclass> getAllArtifactclasses() {
		return (List<Artifactclass>) dBMasterTablesCacheManager.getAllRecords(CacheableTablesList.artifactclass.name());
	}
	
	public Artifactclass getArtifactclass(String artifactclassId) {
		return (Artifactclass) dBMasterTablesCacheManager.getRecord(CacheableTablesList.artifactclass.name(), artifactclassId);
	}
	
	public Destination getDestination(String destinationId) {
		return (Destination) dBMasterTablesCacheManager.getRecord(CacheableTablesList.destination.name(), destinationId);
	}

	public List<Device> getAllDevices() { 
		return (List<Device>) dBMasterTablesCacheManager.getAllRecords(CacheableTablesList.device.name());
	}
	
	public Device getDevice(String deviceId) {
		return (Device) dBMasterTablesCacheManager.getRecord(CacheableTablesList.device.name(), deviceId);
	}
	
	
	public List<Device> getAllConfiguredAutoloaderDevices() throws Exception{
		List<Device> autoloaders = new ArrayList<Device>();
		List<Device> allDeviceList = getAllDevices();
		for (Device device : allDeviceList) {
			if(device.getDevicetype() == Devicetype.tape_autoloader) {
				autoloaders.add(device);
			}
		}
		return autoloaders;
	}
	
	public List<Device> getAllConfiguredDriveDevices() throws Exception{
		List<Device> drives = new ArrayList<Device>();
		List<Device> allDeviceList = getAllDevices();
		for (Device device : allDeviceList) {
			if(device.getDevicetype() == Devicetype.tape_drive) {
				drives.add(device);
			}
		}
		return drives;
	}
	
	public Location getLocation(String requestedLocation) {
		return (Location) dBMasterTablesCacheManager.getRecord(CacheableTablesList.location.name(), requestedLocation);
	}
	
	
	
}
