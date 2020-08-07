package org.ishafoundation.dwaraapi.db.utils;

import org.ishafoundation.dwaraapi.db.attributeconverter.enumreferences.ActionAttributeConverter;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Archiveformat;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Destination;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Location;
import org.ishafoundation.dwaraapi.enumreferences.Action;
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

	public Artifactclass getArtifactclass(String artifactclassId) {
		return (Artifactclass) dBMasterTablesCacheManager.getRecord(CacheableTablesList.artifactclass.name(), artifactclassId);
	}
	
	public Destination getDestination(String destinationId) {
		return (Destination) dBMasterTablesCacheManager.getRecord(CacheableTablesList.destination.name(), destinationId);
	}

	public Device getDevice(String deviceId) {
		return (Device) dBMasterTablesCacheManager.getRecord(CacheableTablesList.device.name(), deviceId);
	}
	
	public Location getLocation(String requestedLocation) {
		return (Location) dBMasterTablesCacheManager.getRecord(CacheableTablesList.location.name(), requestedLocation);
	}
}
