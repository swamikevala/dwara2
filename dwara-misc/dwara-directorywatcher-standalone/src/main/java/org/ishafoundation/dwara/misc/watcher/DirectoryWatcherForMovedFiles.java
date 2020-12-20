package org.ishafoundation.dwara.misc.watcher;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.utils.HttpClientUtil;


/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcherForMovedFiles {

	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private boolean trace = true;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static String ingestEndpointUrl = null;
	
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

		System.out.format("Scannnning %s ...\n", dir);
		registerAll(dir);
		System.out.println("Done.");

		// enable trace after initial registration
		this.trace = true;
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
							expirationTimes.put(artifactPath, lastModified.toMillis()+newFileWait);
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
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
		if (trace)
			System.out.println("artifactName " + artifactName);
		return Paths.get(watchedDir.toString(), artifactName); 
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
					invokeIngest(item.getKey());
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
    
    private void invokeIngest(Path child) throws Exception {

    	if(child.getNameCount() > 6) { // Expecting child = /data/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1
    		throw new Exception("File Path with more than 6 elements is not supported. Expected something like /data/user/pgurumurthy/ingest/prasad-pub/prasad-artifact-1 but actual is " + child);
    	}
    	String path = child.getParent().toString();
    	String artifactName = child.getFileName().toString();
    	String artifactclass = child.getName(child.getNameCount() - 2).toString();

    	
    	String payload = "{\"artifactclass\":\"<<Artifactclass>>\",\"stagedFiles\":[{\"path\":\"<<Path>>\",\"name\":\"<<ArtifactName>>\"}]}";
    	payload = payload.replace("<<Artifactclass>>", artifactclass);
    	payload = payload.replace("<<Path>>", path);
    	payload = payload.replace("<<ArtifactName>>", artifactName);
    	System.out.println("payload "+ payload);

		String response = null;
		try {
			response = HttpClientUtil.postIt(ingestEndpointUrl, null, payload);
			System.out.println("resp " + response);
		}catch (Exception e) {
			System.out.println("Error on invoking ingest endpoint - " + e.getMessage());
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

		new DirectoryWatcherForMovedFiles(dir, waitTimes).processEvents();
	}
}
