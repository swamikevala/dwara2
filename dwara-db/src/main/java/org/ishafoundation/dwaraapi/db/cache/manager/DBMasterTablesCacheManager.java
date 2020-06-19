package org.ishafoundation.dwaraapi.db.cache.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.cache.CacheableRepository;
import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.cache.CacheableTablesList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBMasterTablesCacheManager<T> {
	
	@Autowired
	private Map<String, CacheableRepository> cacheableReposMap;
	
	//caches all listed reference and config tables 
	private Map<String, List<Cacheable>> tables_table_List = new HashMap<String, List<Cacheable>>();
	private Map<String, Map<Integer, Cacheable>> tables_id_record_Map = new HashMap<String, Map<Integer, Cacheable>>();
	private Map<String, Map<String, Cacheable>> tables_name_record_Map = new HashMap<String, Map<String, Cacheable>>();


	@PostConstruct
	public void loadAll() {
		System.out.println("Now loading all Master tables for caching...");
		CacheableTablesList[] configTables = CacheableTablesList.values();
		for (int i = 0; i < configTables.length; i++) {
			CacheableTablesList configurationTables = configTables[i];
			String tableName = configurationTables.name();
			System.out.println("caching... " + tableName);
			
			// TODO : need to do the validation here...
			List<Cacheable> list = (List<Cacheable>) cacheableReposMap.get(tableName+"Dao").findAll();
			Map<Integer, Cacheable> id_record_Map = new HashMap<Integer, Cacheable>();
			Map<String, Cacheable> name_record_Map = new HashMap<String, Cacheable>();
			
			for (Cacheable cacheable : list) {
				id_record_Map.put(cacheable.getId(), cacheable);
				if(cacheable.getName() != null)
					name_record_Map.put(cacheable.getName(), cacheable);
			}
			
			tables_table_List.put(tableName, list);
			tables_id_record_Map.put(tableName,id_record_Map);
			tables_name_record_Map.put(tableName,name_record_Map);
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

	public Cacheable getRecord(String tableName, int id){
		Cacheable s = tables_id_record_Map.get(tableName).get(id);
		return s;
	}
	
	public Cacheable getRecord(String tableName, String name){
		Cacheable s = tables_name_record_Map.get(tableName).get(name);
		return s;
	}
	
	public void clearAll() {
		System.out.println("Now clear all");
		
		tables_table_List.clear();
		tables_id_record_Map.clear();
		tables_name_record_Map.clear();
		
		//loadAll();
	}
	
	// loading a specific table. Table name need to be passed as a parameter...
	public void load(String tableName) {
		
	}
	
	public void clear(String tableName) {
		tables_table_List.get(tableName).clear();
		tables_id_record_Map.get(tableName).clear();
		tables_name_record_Map.get(tableName).clear();
		
		load(tableName);
	}
}
