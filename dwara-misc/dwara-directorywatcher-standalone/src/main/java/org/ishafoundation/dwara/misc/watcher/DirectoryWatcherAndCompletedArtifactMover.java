package org.ishafoundation.dwara.misc.watcher;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for watching for files copied in prasadcorp workspace and when completed moves the file to dwara's user specific artifactclass location and calls the ingest 
 */

public class DirectoryWatcherAndCompletedArtifactMover extends DirectoryWatcher {

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcherAndCompletedArtifactMover.class);

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcherAndCompletedArtifactMover(Path dir, Long waitTime) throws IOException {
		super(dir, waitTime);
	}

	protected void executeOnCopyCompletion(WatchKey watchKey, Path artifactPath, Collection<File> files) throws Exception{
		
		String artifactName = getArtifactName(artifactPath);
		// move it to failed folder
		Path destPath = Paths.get(watchedDir.toString(), "COMPLETED" , artifactName);
		try {
			move(artifactPath, destPath);
			updateStatus(artifactPath, Status.moved);
		}catch (Exception e) {
			logger.error("Unable to move " + artifactPath + " to " + destPath, e);
			try {
				updateStatus(artifactPath, Status.move_failed);
				return;
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	static void usage() {
		System.err.println("usage: java DirectoryWatcherAndCompletedArtifactMover [waitTimeInSecs] <dirToBeWatched(\"/data/prasad-staging\")> noOfThreadsForParallelProcessing");
		System.exit(-1);
	}
	
	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length == 0 || args.length > 3)
			usage();

		int dirArg = 0;
		long waitTimes = 0L;
		if (args.length == 3) {
			waitTimes = Long.parseLong(args[0]) * 1000;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
	
		int noOfThreads = Integer.parseInt(args[dirArg + 1]);
		executor = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		new DirectoryWatcherAndCompletedArtifactMover(dir, waitTimes).processEvents();
	}
}
