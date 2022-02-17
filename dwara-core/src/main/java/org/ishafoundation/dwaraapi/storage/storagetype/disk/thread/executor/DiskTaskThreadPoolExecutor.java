package org.ishafoundation.dwaraapi.storage.storagetype.disk.thread.executor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.DeviceDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiskTaskThreadPoolExecutor {
	
	private Executor executor;

	@PostConstruct
	public void init() throws Exception {
		int corePoolSize = 3; // extra buffer 2, so even when we bump extra drives(assumption - not expecting to add more than 2) we dont need to restart app
		int maxPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
