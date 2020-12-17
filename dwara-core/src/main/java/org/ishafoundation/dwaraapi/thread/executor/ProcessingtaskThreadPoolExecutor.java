package org.ishafoundation.dwaraapi.thread.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessingtaskThreadPoolExecutor {

	@Value("${threadpoolexecutor.processingtask.corePoolSize}")
	private String configuredCorePoolSize;
	
	@Value("${threadpoolexecutor.processingtask.maxPoolSize}")
	private String configuredMaxPoolSize;
	
	private Executor executor;

	@PostConstruct
	public void init() throws Exception {
		if(StringUtils.isBlank(configuredCorePoolSize))
			configuredCorePoolSize = "1";
		int corePoolSize = Integer.parseInt(configuredCorePoolSize);

		if(StringUtils.isBlank(configuredMaxPoolSize))
			configuredMaxPoolSize = "1";
		int maxPoolSize = Integer.parseInt(configuredMaxPoolSize);

        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
