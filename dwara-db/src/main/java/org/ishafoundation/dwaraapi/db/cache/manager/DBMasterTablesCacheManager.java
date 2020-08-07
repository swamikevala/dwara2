package org.ishafoundation.dwaraapi.db.cache.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBMasterTablesCacheManager<T> {
	
	private static final String DAO_SUFFIX = "Dao";
	private static final Logger logger = LoggerFactory.getLogger(DBMasterTablesCacheManager.class);
	
	@Autowired
	private Map<String, CacheableRepository> cacheableReposMap;
	
	//caches all listed reference and config tables 
	private Map<String, List<Cacheable>> tables_table_List = new HashMap<String, List<Cacheable>>();
	private Map<String, Map<String, Cacheable>> tables_id_record_Map = new HashMap<String, Map<String, Cacheable>>();


	@PostConstruct
	public void loadAll() {
		logger.debug("Now loading configured Referece/Configuration Master tables for caching...");
		CacheableTablesList[] configTables = CacheableTablesList.values();
		for (int i = 0; i < configTables.length; i++) {
			CacheableTablesList configurationTables = configTables[i];
			String tableName = configurationTables.name();
			logger.debug("caching... " + tableName);
			
			// TODO : need to do the validation here...
			List<Cacheable> list = (List<Cacheable>) cacheableReposMap.get(tableName+DAO_SUFFIX).findAll();
			Map<String, Cacheable> id_record_Map = new HashMap<String, Cacheable>();
			
			for (Cacheable cacheable : list) {
				if(cacheable.getId() != null)
					id_record_Map.put(cacheable.getId(), cacheable);
			}
			
			tables_table_List.put(tableName, list);
			tables_id_record_Map.put(tableName,id_record_Map);
		}
		//validateConfiguration();
	}

	/*
	 * Conflicting configuration ideally should not let be saved in the first place. But we are not taking the API route to update our config tables and its manually edited in the DB straight
	 * 
	 * so we are validating after its entered and before its made available to the system
	 * 
	 * What should be the behaviour? 
	 * 
	 * If there is a validation failure 
	 * 1) at startup, we can make the app not come to life until the error is fixed... even for errors like volumeset being in colocation sort of errors?
	 * 2) while app is running and if the configuration is changed and app inmemory cache cleared and finds an error during load. What then? System exit? 
	 *  
	 */
	private void validateConfiguration() throws Exception{
		// 2 volumesets on the same libraryclass cannot be co located...
		
	}
	
	public List<Cacheable> getAllRecords(String tableName){
		return tables_table_List.get(tableName);
	}

	public Cacheable getRecord(String tableName, String id){
		Cacheable s = tables_id_record_Map.get(tableName).get(id);
		return s;
	}

	public void clearAll() {
		logger.debug("Now clear all");
		
		tables_table_List.clear();
		tables_id_record_Map.clear();
	}
	
	// loading a specific table. Table name need to be passed as a parameter...
	public void load(String tableName) {
		
	}
	
	public void clear(String tableName) {
		tables_table_List.get(tableName).clear();
		tables_id_record_Map.get(tableName).clear();
		load(tableName);
	}
}
