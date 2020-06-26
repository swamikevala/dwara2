package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.enumreferences.Storagetype;
import org.springframework.stereotype.Component;

@Component
public class StoragetypeThreadPoolExecutor{

	private Executor executor;

	@PostConstruct
	public void init() {
		int corePoolSize = Storagetype.values().length;
		int maxPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public Executor getExecutor(){
        return executor;
	}
}
