package org.ishafoundation.dwaraapi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.VersionDao;
import org.ishafoundation.dwaraapi.db.model.master.reference.Version;
import org.ishafoundation.dwaraapi.process.IProcessingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@ComponentScan({"org.ishafoundation.dwaraapi","org.ishafoundation.videopub","org.ishafoundation.digitization"})
@SpringBootApplication
public class DwaraApiApplication {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Map<String, IProcessingTask> processingtaskActionMap;
	
	@Autowired
	private VersionDao versionDao;	
	
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void validateDbVersion() throws Exception {
		Version version = versionDao.findTopByOrderByVersion();
		String dbVersion = "2.0.3";
		if(version == null || !version.getVersion().equals(dbVersion))
			throw new Exception("DB version mismatch. Upgrade DB to " + dbVersion);
	}
	
	/*
	 * On bootstraping the app we need to create as many thread pools for as many tasks configured...
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void createThreadPoolsForTask() throws Exception {
		String globalProcessingtaskDefault = IProcessingTask.GLOBAL_THREADPOOL_IDENTIFIER;
		IProcessingTask.taskName_executor_map.put(globalProcessingtaskDefault, createExecutor(globalProcessingtaskDefault, true));
		
		Set<String> processingtaskSet = processingtaskActionMap.keySet();
		for (String processingtaskName : processingtaskSet) {
			Executor executor = createExecutor(processingtaskName, false);
			if(executor != null)
				IProcessingTask.taskName_executor_map.put(processingtaskName, executor);
		}
	}
	
	private Executor createExecutor(String processingtaskName, boolean isGlobal) throws Exception {
		String propertyNamePrefix = "threadpoolexecutor."+processingtaskName;	
		String corePoolSizePropName = propertyNamePrefix + ".corePoolSize";
		String maxPoolSizePropName = propertyNamePrefix + ".maxPoolSize";
		
		String configuredCorePoolSize = env.getProperty(corePoolSizePropName);
		if(StringUtils.isBlank(configuredCorePoolSize)) {
			if(isGlobal)
				throw new Exception("Please configure the processing task thread pool size properly");
			else
				return null;
		}
		
		int corePoolSize = Integer.parseInt(configuredCorePoolSize);
		
		String configuredMaxPoolSize = env.getProperty(maxPoolSizePropName);
		if(StringUtils.isBlank(configuredMaxPoolSize))
			configuredMaxPoolSize = configuredCorePoolSize;
		int maxPoolSize = Integer.parseInt(configuredMaxPoolSize);
		
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

}
