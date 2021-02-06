package org.ishafoundation.dwaraapi.resource.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.ishafoundation.dwaraapi.ApplicationStatus;
import org.ishafoundation.dwaraapi.api.req.admin.FfmpegPropertyElements;
import org.ishafoundation.dwaraapi.api.req.admin.ThreadpoolexecutorPropertyElements;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.configuration.FfmpegThreadConfiguration;
import org.ishafoundation.dwaraapi.configuration.FfmpegThreadProps;
import org.ishafoundation.dwaraapi.db.cache.manager.DBMasterTablesCacheManager;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.ishafoundation.dwaraapi.utils.JsonPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private Configuration configuration;
		
	@Autowired
	private FfmpegThreadConfiguration ffmpegThreadConfiguration;
	
	@Autowired
	private Environment env;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private DBMasterTablesCacheManager dBMasterTablesCacheManager;
	
	@RequestMapping(value = "/admin/clearAndReloadDBCache", method = RequestMethod.POST) 
	public ResponseEntity<String> clearAndReload() {
		dBMasterTablesCacheManager.clearAll();
		dBMasterTablesCacheManager.loadAll();
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	// Application - MODE/Status
	@RequestMapping(value = "/admin/application/mode/{mode}", method = RequestMethod.POST)
	public ResponseEntity<String> setMode(@PathVariable("mode") String mode) {
		try {
			ApplicationStatus.valueOf(mode);
			configuration.setAppMode(mode);
		}catch (Exception e) {
			String errorMsg = mode + " - not supported";
		
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(mode);
	}
	
	@RequestMapping(value = "/admin/application/mode", method = RequestMethod.GET)
	public ResponseEntity<String> getMode() {
		return ResponseEntity.status(HttpStatus.OK).body(configuration.getAppMode());
	}
	
	@RequestMapping(value = "/admin/application/getThreadpoolexecutorProps", method = RequestMethod.GET)
	public ResponseEntity<List<ThreadpoolexecutorPropertyElements>> getThreadpoolexecutorPropDetails() {
		List<ThreadpoolexecutorPropertyElements> threadpoolexecutorPropertyElementsList = new ArrayList<ThreadpoolexecutorPropertyElements>();
		HashMap<String, Executor> taskName_executor_map = IProcessingTask.taskName_executor_map;
		for (String taskName : taskName_executor_map.keySet()) {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) taskName_executor_map.get(taskName);
			ThreadpoolexecutorPropertyElements threadpoolexecutorPropertyElements = new ThreadpoolexecutorPropertyElements();
			threadpoolexecutorPropertyElements.setTask(taskName);
			threadpoolexecutorPropertyElements.setCorePoolSize(executor.getCorePoolSize());
			threadpoolexecutorPropertyElements.setMaxPoolSize(executor.getMaximumPoolSize());
			
			threadpoolexecutorPropertyElementsList.add(threadpoolexecutorPropertyElements);
		}
		return ResponseEntity.status(HttpStatus.OK).body(threadpoolexecutorPropertyElementsList);
	}	
	
	// [{"task": "processingtask", "corePoolSize" : 5, "maxPoolSize" : 5},{"task": "video-digi-2020-preservation-gen", "corePoolSize" : 3, "maxPoolSize" : 3}]
	@RequestMapping(value = "/admin/application/updateThreadpoolexecutorProps", method = RequestMethod.POST)
	public ResponseEntity<String> updateThreadpoolexecutorProps(@RequestBody List<ThreadpoolexecutorPropertyElements> threadpoolexecutorConfig) {
		String key = null;
		try {
			for (ThreadpoolexecutorPropertyElements nthThreadpoolexecutorConfigObj : threadpoolexecutorConfig) {
				key = nthThreadpoolexecutorConfigObj.getTask();
				
				ThreadPoolExecutor executor = (ThreadPoolExecutor) IProcessingTask.taskName_executor_map.get(key);
				executor.setCorePoolSize(nthThreadpoolexecutorConfigObj.getCorePoolSize());
				executor.setMaximumPoolSize(nthThreadpoolexecutorConfigObj.getMaxPoolSize());
			}
		}catch (Exception e) {
			String errorMsg = key + " - not supported";
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}		
		return ResponseEntity.status(HttpStatus.OK).body("Done. Check out getThreadpoolexecutorProps");
	}
	
	@RequestMapping(value = "/admin/application/getFfmpegThreads", method = RequestMethod.GET)
	public ResponseEntity<List<FfmpegPropertyElements>> getFfmpegThreads() {
		List<FfmpegPropertyElements> threadpoolexecutorPropertyElementsList = new ArrayList<FfmpegPropertyElements>();
		
		int threads = 0;
		for (int i = 1; i <= 3; i++) {
			FfmpegPropertyElements ffmpegPropertyElements = new FfmpegPropertyElements();
			if(i == 1) {
				ffmpegPropertyElements.setTask("video-proxy-low-gen");
				threads = ffmpegThreadConfiguration.getVideoProxyLowGen().getThreads();
			} else if(i == 2) {
				ffmpegPropertyElements.setTask("video-digi-2020-preservation-gen");
				threads = ffmpegThreadConfiguration.getVideoDigi2020PreservationGen().getThreads();
			} else if(i == 3) {
				ffmpegPropertyElements.setTask("video-digi-2020-qc-gen");
				threads = ffmpegThreadConfiguration.getVideoDigi2020QcGen().getThreads();
			} 
			ffmpegPropertyElements.setThreads(threads);
			threadpoolexecutorPropertyElementsList.add(ffmpegPropertyElements);
		}
		return ResponseEntity.status(HttpStatus.OK).body(threadpoolexecutorPropertyElementsList);
	}
	
	// [{"task": "processingtask", "threads" : 5},{"task": "video-digi-2020-preservation-gen", "threads" : 3}]
	@RequestMapping(value = "/admin/application/updateFfmpegThreads", method = RequestMethod.POST)
	public ResponseEntity<String> updateFfmpegThreads(@RequestBody List<FfmpegPropertyElements> ffmpegPropertyElements) {
		String key = null;
		int threads = 0;
		try {
			for (FfmpegPropertyElements ffmpegPropertyElementsObj : ffmpegPropertyElements) {
				key = ffmpegPropertyElementsObj.getTask();
				threads = ffmpegPropertyElementsObj.getThreads();
				if(key.equals("video-proxy-low-gen")) {
					ffmpegThreadConfiguration.getVideoProxyLowGen().setThreads(threads);
				}else if(key.equals("video-digi-2020-preservation-gen")) {
					ffmpegThreadConfiguration.getVideoDigi2020PreservationGen().setThreads(threads);
				}else if(key.equals("video-digi-2020-qc-gen")) {
					ffmpegThreadConfiguration.getVideoDigi2020QcGen().setThreads(threads);
				}
			}
		}catch (Exception e) {
			String errorMsg = key + " - not supported";
			logger.error(errorMsg, e);
			
			if(e instanceof DwaraException)
				throw (DwaraException) e;
			else
				throw new DwaraException(errorMsg, null);
		}		
		return ResponseEntity.status(HttpStatus.OK).body("Done. Check out getFfmpegThreads");
	}
}
