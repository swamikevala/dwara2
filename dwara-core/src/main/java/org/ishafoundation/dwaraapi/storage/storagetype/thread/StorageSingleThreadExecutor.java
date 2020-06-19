package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class StorageSingleThreadExecutor{

	private Executor executor;

	@PostConstruct
	public void init() {
		executor = Executors.newSingleThreadExecutor();
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
