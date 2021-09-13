package org.ishafoundation.dwaraapi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.ishafoundation.dwaraapi.db.dao.master.VersionDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDao;
import org.ishafoundation.dwaraapi.db.dao.transactional.JobDaoQueryProps;
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
		String dbVersion = "2.1.1";
		if(version == null || !version.getVersion().equals(dbVersion))
			throw new Exception("DB version mismatch. Upgrade DB to " + dbVersion);
	}
	
	/*
	 * On bootstraping the app we need to create as many thread pools for as many tasks configured...
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void createThreadPoolsForTask() throws Exception {
		String globalProcessingtaskDefault = IProcessingTask.GLOBAL_THREADPOOL_IDENTIFIER;
		Executor globalExecutorDefault = createExecutor(globalProcessingtaskDefault, true);
		IProcessingTask.taskName_executor_map.put(globalProcessingtaskDefault, globalExecutorDefault);
		
		Set<String> processingtaskSet = processingtaskActionMap.keySet();
		for (String processingtaskName : processingtaskSet) {
			String identifier = null;
			Executor executor = createExecutor(processingtaskName, false);
			if(executor != null) {
				IProcessingTask.taskName_executor_map.put(processingtaskName, executor);
				identifier = processingtaskName;
			}
			else {
				identifier = IProcessingTask.GLOBAL_THREADPOOL_IDENTIFIER;
				executor = globalExecutorDefault;
			}
			
			
			JobDaoQueryProps jobDaoQueryProps = JobDao.executorName_queryProps_map.get(identifier);
			if(jobDaoQueryProps == null) {
				jobDaoQueryProps = new JobDaoQueryProps();
			}
			jobDaoQueryProps.getTaskNameList().add(processingtaskName);
			ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
			jobDaoQueryProps.setLimit(tpe.getCorePoolSize() + 2);

			JobDao.executorName_queryProps_map.put(identifier, jobDaoQueryProps);
		}
	}
	
	private Executor createExecutor(String processingtaskName, boolean isGlobal) throws Exception {
		String propertyNamePrefix = "threadpoolexecutor."+processingtaskName;	
		String corePoolSizePropName = propertyNamePrefix + ".corePoolSize";
		String maxPoolSizePropName = propertyNamePrefix + ".maxPoolSize";
		String priorityPropName = propertyNamePrefix + ".priority";
		
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

		
		String configuredPriority = env.getProperty(priorityPropName);
		if(StringUtils.isBlank(configuredPriority))
			configuredPriority = "0";
		int priority = Integer.parseInt(configuredPriority);
		
		 BasicThreadFactory factory = new BasicThreadFactory.Builder()
			     .namingPattern(processingtaskName + "-%d")
			     .daemon(false)
			     .priority(Thread.NORM_PRIORITY + priority)
			     .build();
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), factory);
	}

}
