package org.ishafoundation.dwara.misc.watcher;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ishafoundation.dwaraapi.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class moves an artifact from loc A to loc B and ingest.
 */

public class DirectoryWatcherForMovedFiles {

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcherForCopiedFiles.class);
	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private boolean trace = true;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static String ingestEndpointUrl = null;
	private static Executor executor = null;
	
	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcherForMovedFiles(Path dir, Long waitTime) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.watchedDir = dir;
		this.newFileWait = waitTime;
		this.keys = new HashMap<WatchKey,Path>();

		System.out.format("Scanning %s ...\n", dir);
		registerAll(dir);
		System.out.println("Done.");
		// enable trace after initial registration
		this.trace = false;
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, ENTRY_CREATE);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Process all events for keys queued to the watchService
	 */
	void processEvents() throws Exception{
		for (;;) { //infinite loop

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException x) {
				return;
			}

			for(;;) { // infinite loop for timed polling to find if any pending events are not and check on expiry...
				long currentTime = System.currentTimeMillis();

				if(key != null) { // if events are still pending...

					Path dir = keys.get(key);
					if (dir == null) {
						System.err.println("WatchKey not recognized!!");
						continue;
					}

					for (WatchEvent<?> event: key.pollEvents()) {
						Kind<?> kind = event.kind();

						// TBD - provide example of how OVERFLOW event is handled
						if (kind == OVERFLOW) {
							continue;
						}

						// Context for directory entry event is the file name of entry
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();
						Path child = dir.resolve(name);

						// print out event
						if (trace)
							System.out.format("%s: %s\n", event.kind().name(), child);

						// if directory is created, and watching recursively, then
						// register it and its sub-directories
						if (kind == ENTRY_CREATE) {
							logger.info("Now will be ingesting " + child);
							Path artifactPath = getArtifactPath(child);
							if (trace)
								System.out.println("artifactPath " + artifactPath);
							
							// Update modified time
							BasicFileAttributes attrs = null;
							try {
								attrs = Files.readAttributes(child, BasicFileAttributes.class, NOFOLLOW_LINKS);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
								continue; // Happens when copy complete, but permissions set by stagedservice triggered this event but by then the stagedservice thread moved this file to /data/staged...
							}
							FileTime lastModified = attrs.lastModifiedTime();
							if (trace)
								System.out.println("lastModified " + lastModified);
							expirationTimes.put(child, lastModified.toMillis()+newFileWait);
						}
						else if (kind == ENTRY_MODIFY) {
							System.out.println("No modification expected as its a move, check out this" + child);
						}
					}

					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						if (trace) {
							System.out.println(key + " not valid");
							System.out.println("Will be removing " + keys.get(key) + " from key list");
						}
						keys.remove(key);

						// all directories are inaccessible
						if (keys.isEmpty()) {
							break;
						}
					}
				}
				else {
					if (trace)
						System.out.println("No pending events");
				}

				handleExpiredWaitTimes(currentTime);

				// If there are no files left stop polling and block on .take()
				if(expirationTimes.isEmpty())
					break;

				long minExpiration = Collections.min(expirationTimes.values());
				long timeout = minExpiration - currentTime;
				if (trace)
					System.out.println("timeout: "+timeout);
				key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	private Path getArtifactPath(Path child){
		// child path will be something like /data/dwara/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1/...
		return Paths.get(File.separator, child.subpath(0, 6).toString());

//		child.
//		child.subpath(beginIndex, endIndex)
//		String artifactName = child.getName(6).toString();
//		Strin
//    	String path = child.getParent().toString();
//    	
//    	String artifactclass = child.getName(5).toString();
//
//    	child.get
//		child.
//		ENTRY_CREATE
		
//		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
//		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
//		if (trace)
//			System.out.println("artifactName " + artifactName);
//		return Paths.get(watchedDir.toString(), artifactName); 
	}

	private void handleExpiredWaitTimes(Long currentTime) {
		// Start import for files for which the expirationtime has passed

		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			if(expiryTime <= currentTime) {
				try {
			    	InvokeTask invokeTask = new InvokeTask();//applicationContext.getBean(TapeTask.class); 
					invokeTask.setPath(item.getKey());
					executor.execute(invokeTask);
				}
				catch (Exception e) {
					System.err.println("Ingest failed for " + item.getKey());
					e.printStackTrace();
				}
			
				// System.out.println("expired " + item.getKey());
				// do something with the file
				it.remove();
			}
		}
	}
    
	
    
	public class InvokeTask implements Runnable{
		
		private Path path;
		
		public void setPath(Path path) {
			this.path = path;
		}

		@Override
		public void run() {
	    	if(path.getNameCount() > 7) { // Expecting path = /data/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1
	    		System.err.println("File Path with more than 7 elements is not supported. Expected something like /data/dwara/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1 but actual is " + path);
	    	}
	    	else {
		    	String artifactBasePath = path.getParent().toString();
		    	String artifactName = path.getFileName().toString();
		    	String artifactclass = path.getName(path.getNameCount() - 2).toString();
	
		    	
		    	String payload = "{\"artifactclass\":\"<<Artifactclass>>\",\"stagedFiles\":[{\"path\":\"<<Path>>\",\"name\":\"<<ArtifactName>>\"}]}";
		    	payload = payload.replace("<<Artifactclass>>", artifactclass);
		    	payload = payload.replace("<<Path>>", artifactBasePath);
		    	payload = payload.replace("<<ArtifactName>>", artifactName);
		    	logger.debug("payload "+ payload);
	
				String response = null;
				try {
					response = HttpClientUtil.postIt(ingestEndpointUrl, null, payload);
					logger.debug("resp " + response);
				}catch (Exception e) {
					logger.error("Error on invoking ingest endpoint - " + e.getMessage());
				}
	    	}			
		}
	}
    
	static void usage() {
		System.err.println("usage: java DirectoryWatcher [waitTimeInSecs] <dirToBeWatched(\"/data/user/pgurumurthy/ingest\")> <ingestEndpointUrl(\"http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest\")>");
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
		
		ingestEndpointUrl = args[dirArg + 1]; // "http://pgurumurthy:ShivaShambho@172.18.1.213:8080/api/staged/ingest";

		int noOfThreads = 1;
		executor = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		
		new DirectoryWatcherForMovedFiles(dir, waitTimes).processEvents();
	}
}
