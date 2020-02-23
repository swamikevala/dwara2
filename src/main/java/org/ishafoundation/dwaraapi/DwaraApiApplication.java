package org.ishafoundation.dwaraapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ishafoundation.dwaraapi.db.cacheutil.ProcessCacheUtil;
import org.ishafoundation.dwaraapi.db.model.master.workflow.Process;
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
	private ProcessCacheUtil processCacheUtil;		
	
	public static HashMap<String, Executor> processName_executor_map = new HashMap<String, Executor>();
			
	public static void main(String[] args) {
		SpringApplication.run(DwaraApiApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		
		List<Process> processList = processCacheUtil.getProcessList();
		for (Iterator<Process> iterator = processList.iterator(); iterator.hasNext();) {
			Process process = (Process) iterator.next();
			
			String processName = process.getName().toLowerCase();
			
			String propertyNamePrefix = "threadpoolexecutor."+processName;	
			String corePoolSizePropName = propertyNamePrefix + ".corePoolSize";
			String maxPoolSizePropName = propertyNamePrefix + ".maxPoolSize";
			
			int corePoolSize = Integer.parseInt(env.getProperty(corePoolSizePropName));
			int maxPoolSize = Integer.parseInt(env.getProperty(maxPoolSizePropName));
			
			Executor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			processName_executor_map.put(processName, executor);
		}
	}
}
