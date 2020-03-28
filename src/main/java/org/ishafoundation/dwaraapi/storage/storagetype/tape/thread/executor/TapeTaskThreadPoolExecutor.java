package org.ishafoundation.dwaraapi.storage.storagetype.tape.thread.executor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.ishafoundation.dwaraapi.db.dao.master.TapedriveDao;
import org.ishafoundation.dwaraapi.db.model.Tapedrive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TapeTaskThreadPoolExecutor {

	@Autowired
	private TapedriveDao tapedriveDao;
	
	private Executor executor;

	/*
	org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'tapeTaskThreadPoolExecutor': Invocation of init method failed; nested exception is java.lang.IllegalArgumentException
	then execute this query
	INSERT INTO `tapedrive` VALUES (13001,'somw qqi',0,'1234','AVAILABLE',null,null,null);
	*/
	@PostConstruct
	public void init() {
		List<Tapedrive> tapedriveList = (List<Tapedrive>) tapedriveDao.findAll();
		int corePoolSize = tapedriveList.size();
		int maxPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public Executor getExecutor(){
        return executor;
	}
}
