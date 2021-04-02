package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.DwaraConstants;
import org.springframework.stereotype.Component;

@Component("disk" + DwaraConstants.STORAGETYPE_THREADPOOLEXECUTOR_SUFFIX)
public class DiskThreadPoolExecutor implements IStoragetypeThreadPoolExecutor{

	private ThreadPoolExecutor executor;
	
	//need access to queue and hence not using the Executor/Executorservice interfaces
	@PostConstruct
	public void init() {
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public ThreadPoolExecutor getExecutor(){
        return executor;
	}
}
