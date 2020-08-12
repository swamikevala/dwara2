package org.ishafoundation.dwaraapi.storage.storagetype.thread;

import java.util.concurrent.ThreadPoolExecutor;

// A Marker interface helps identify the different Storagetype specific ThreadPoolExecutors
public interface IStoragetypeThreadPoolExecutor {

	// Code to interface not impl design principle is at a toss - because we need access to the queue, as it was decided having a job status inbetween queued and in-progress doesnt make sense from a user perspective.
	// So when the scheduler polls for db queued jobs, the db jobs previously sent to the executor's queue will also be in the resultset which we need to ensure is "delta"-ed...
	// Ideally the db resultset should have been filtered, but in this business usecase its not applicable so filtering in the executor queue...
	ThreadPoolExecutor getExecutor();

}
