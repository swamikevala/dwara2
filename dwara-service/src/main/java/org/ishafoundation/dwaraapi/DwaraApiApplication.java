package org.ishafoundation.dwaraapi;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.VersionDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
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
@ComponentScan({"org.ishafoundation.dwaraapi","org.ishafoundation.videopub"})
@SpringBootApplication
public class DwaraApiApplication {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private ProcessingtaskDao processingtaskDao;		

	@Autowired
	private VersionDao versionDao;	
	
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void validateDbVersion() throws Exception {
		Version version = versionDao.findTopByVersion();
		if(!version.getVersion().equals("2.0.05"))
			throw new Exception("DB version mismatch. Upgrade DB to " + "2.0.05");
	}
	/*
	 * On bootstraping the app we need to create as many thread pools for as many tasks configured...
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void createThreadPoolsForTask() {
		
		Iterable<Processingtask> processingtaskList = processingtaskDao.findAll();
		for (Processingtask processingtask : processingtaskList) {
			String processingtaskName = processingtask.getId().toLowerCase();
			
			String propertyNamePrefix = "threadpoolexecutor."+processingtaskName;	
			String corePoolSizePropName = propertyNamePrefix + ".corePoolSize";
			String maxPoolSizePropName = propertyNamePrefix + ".maxPoolSize";
			
			String configuredCorePoolSize = env.getProperty(corePoolSizePropName);
			if(StringUtils.isBlank(configuredCorePoolSize))
				configuredCorePoolSize = "1";
			int corePoolSize = Integer.parseInt(configuredCorePoolSize);
			
			String configuredMaxPoolSize = env.getProperty(maxPoolSizePropName);
			if(StringUtils.isBlank(configuredMaxPoolSize))
				configuredMaxPoolSize = "1";
			int maxPoolSize = Integer.parseInt(configuredMaxPoolSize);
			
			Executor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			IProcessingTask.taskName_executor_map.put(processingtaskName, executor);
		}
	}
}
