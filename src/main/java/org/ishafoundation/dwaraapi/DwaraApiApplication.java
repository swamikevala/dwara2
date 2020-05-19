package org.ishafoundation.dwaraapi;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ishafoundation.dwaraapi.db.dao.master.ProcessingtaskDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Processingtask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@SpringBootApplication
public class DwaraApiApplication {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private ProcessingtaskDao taskDao;		
	
	public static HashMap<String, Executor> taskName_executor_map = new HashMap<String, Executor>();
			
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}

	/*
	 * On bootstraping the app we need to create as many thread pools for as many tasks configured...
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void createThreadPoolsForTask() {
		
		Iterable<Processingtask> taskList = taskDao.findAll();
		for (Processingtask task : taskList) {
			String taskName = task.getName().toLowerCase();
			
			String propertyNamePrefix = "threadpoolexecutor."+taskName;	
			String corePoolSizePropName = propertyNamePrefix + ".corePoolSize";
			String maxPoolSizePropName = propertyNamePrefix + ".maxPoolSize";
			
			int corePoolSize = Integer.parseInt(env.getProperty(corePoolSizePropName));
			int maxPoolSize = Integer.parseInt(env.getProperty(maxPoolSizePropName));
			
			Executor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			taskName_executor_map.put(taskName, executor);
		}
	}
}
