package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor;

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
public class TapeTaskThreadPoolExecutor {

	@Autowired
	private DeviceDao deviceDao;
	
	private Executor executor;

	@PostConstruct
	public void init() throws Exception {
		List<Device> tapedriveList = (List<Device>) deviceDao.findAllByType(Devicetype.tape_drive);
		if(tapedriveList.size() ==  0)
			return;// throw new Exception("Please configure devices table properly");
		int corePoolSize = tapedriveList.size() + 2; // extra buffer 2, so even when we bump extra drives(assumption - not expecting to add more than 2) we dont need to restart app
		int maxPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
