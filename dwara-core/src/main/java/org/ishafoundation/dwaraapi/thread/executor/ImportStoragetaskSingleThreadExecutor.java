package org.ishafoundation.dwaraapi.thread.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class ImportStoragetaskSingleThreadExecutor{

	private Executor executor;

//	@PostConstruct
//	public void init() {
//		executor = Executors.newSingleThreadExecutor();
//	}
	
	@PostConstruct
	public void init() {
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	
	public Executor getExecutor(){
        return executor;
	}
}
