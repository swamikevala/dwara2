package org.ishafoundation.dwaraapi.thread.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class ProcessingtaskSingleThreadExecutor{

	private Executor executor;

	@PostConstruct
	public void init() {
		executor = Executors.newSingleThreadExecutor();
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
