package org.ishafoundation.dwara.misc.watcher;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for watching for files copied in prasadcorp workspace and when completed moves the file to dwara's user specific artifactclass location and calls the ingest 
 */

public abstract class DirectoryWatcher {

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);

	private final WatchService watchService;
	protected final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	protected static Executor executor = null;
	private String completedDirName = "COMPLETED";
	private Path completedDirPath = null; // Hardcoded - will be watchedDir + completedDirName;
	private String failedDirName = "FAILED";
	private Path failedDirPath = null; // Hardcoded - will be watchedDir + failedDirName;
	private int sideCarFileWaitLoopCount = 60; // Hardcoded - loops as many times waiting for the arrival of side car files...
	private long sideCarFileWaitLoopSleepTimeInMillis = 1000; // Hardcoded - will sleep for the configured time before it starts looping above loop 
	private Map<Path, Integer> artifact_Retrycount_Map = new HashMap<Path, Integer>();
	
	
	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcher(Path dir, Long waitTime) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.watchedDir = dir;
		this.newFileWait = waitTime;
		this.keys = new HashMap<WatchKey,Path>();
		this.failedDirPath =  Paths.get(watchedDir.toString(), failedDirName);
		this.completedDirPath = Paths.get(watchedDir.toString(), completedDirName);
		logger.info(String.format("Scanning %s ...\n", dir));
		registerAll(dir);
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
				if(!dir.toString().equals(watchedDir.toString())) {
					expirationTimes.put(dir, System.currentTimeMillis()+newFileWait);
				}
				
				if(!dir.toString().startsWith(failedDirPath.toString()) && !dir.toString().startsWith(completedDirPath.toString())) { // Dont register the failed and completed dir...
					register(dir);
				}
					
				if(!dir.toString().equals(watchedDir.toString())) {// if there were folders already copied to the watch folder trigger a modify event so that we handleexpiredtimes 
					CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
					try {
						if(!dir.toString().endsWith("mxf"))
							updateStatus(dir, Status.copying);
						clei.executeCommand("chmod -R 777 " + dir.toString(), false);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

		if (logger.isTraceEnabled()) {
			Path prev = keys.get(key);
			if (prev == null) {
				logger.info(String.format("register: %s\n", dir));
			} else {
				if (!dir.equals(prev)) {
					logger.info(String.format("update: %s -> %s\n", prev, dir));
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
						logger.error("WatchKey not recognized!!");
						continue;
					}

					for (WatchEvent<?> event: key.pollEvents()) {
						Kind<?> kind = event.kind();

						if (kind == OVERFLOW) {
							continue;
						}

						// Context for directory entry event is the file name of entry
						WatchEvent<Path> ev = cast(event);
						Path name = ev.context();
						Path child = dir.resolve(name);

						// print out event
						if (logger.isTraceEnabled())
							logger.trace(String.format("%s: %s\n", event.kind().name(), child));

						// if directory is created, and watching recursively, then
						// register it and its sub-directories
						if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
							if (kind == ENTRY_CREATE) {
								try {
									//updateStatus(child, Status.copying);

									if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
										registerAll(child);
									}

								} catch (IOException x) {
									// ignore to keep sample readbale
								}
							}

							// Update modified time
							BasicFileAttributes attrs = null;
							try {
								attrs = Files.readAttributes(child, BasicFileAttributes.class, NOFOLLOW_LINKS);
							} catch (IOException e) {
								continue; // Happens when copy complete, but permissions set by stagedservice triggered this event but by then the stagedservice thread moved this file to /data/staged...
							}
							FileTime lastModified = attrs.lastModifiedTime();
							if (logger.isTraceEnabled())
								System.out.println(child + " lastModified " + lastModified);
							expirationTimes.put(child, lastModified.toMillis()+newFileWait);
						}
					}

					// reset key and remove from set if directory no longer accessible
					boolean valid = key.reset();
					if (!valid) {
						if (logger.isTraceEnabled()) {
							logger.trace(key + " not valid");
							logger.trace("Will be removing " + keys.get(key) + " from key list");
						}
						keys.remove(key);

						// all directories are inaccessible
						if (keys.isEmpty()) {
							break;
						}
					}
				}
				else {
					if (logger.isTraceEnabled())
						logger.trace("No pending events");
				}

				handleExpiredWaitTimes(key, currentTime);

				// If there are no files left stop polling and block on .take()
				if(expirationTimes.isEmpty())
					break;

				long minExpiration = Collections.min(expirationTimes.values());
				long timeout = minExpiration - currentTime;
				if (logger.isTraceEnabled())
					logger.trace("timeout: "+timeout);
				key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	//	private Path getArtifactPath(Path child){
	//		String artifactName = getArtifactName(child);
	//		if (logger.isTraceEnabled())
	//			logger.trace("artifactName " + artifactName);
	//		return Paths.get(watchedDir.toString(), artifactName); 
	//	}

	protected String getArtifactName(Path child){
		String filePathMinusWatchDirPrefix = child.toString().replace(watchedDir.toString() + File.separator, "");
		String artifactName = StringUtils.substringBefore(filePathMinusWatchDirPrefix, File.separator);
		return artifactName;
	}

	private void handleExpiredWaitTimes(WatchKey watchKey, Long currentTime) throws Exception {
		// Start import for files for which the expirationtime has passed
		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			Path filePath = item.getKey();

			if(expiryTime <= currentTime) {
				if(filePath.getFileName().toString().endsWith(".mxf")) {
					updateStatus(filePath, Status.copy_complete);
					createTaskAndExecute(watchKey, filePath);
				}
				it.remove();
			}
		}
	}

	private void createTaskAndExecute(WatchKey watchKey, Path path) throws Exception{
		Task task = new Task();//applicationContext.getBean(TapeTask.class); 
		task.setWatchKey(watchKey);
		task.setMxfFilePath(path);
		executor.execute(task);
	}

	public class Task implements Runnable{

		private Path mxfFilePathname; // will have something like /data/prasad-staging/H122/H122.mxf

		private WatchKey watchKey;

		public void setMxfFilePath(Path mxfFilePathname) {
			this.mxfFilePathname = mxfFilePathname; 
		}

		public void setWatchKey(WatchKey watchKey) {
			this.watchKey = watchKey;
		}

		private String extns[] = {"qc","log", "md5", "mxf"};

		@Override
		public void run() {
			Path artifactPath = mxfFilePathname.getParent(); 
			File artifactFileObj = artifactPath.toFile();

			for (int i = 0; i < sideCarFileWaitLoopCount; i++) {
				//logger.debug(mxfFilePathname + ":" + artifactPath);
				if(!artifactFileObj.isDirectory())
					return;
				Collection<File> files = FileUtils.listFiles(artifactFileObj, extns, false);

				if(files.size() == 4) {
					try {
						//updateStatus(artifactPath, Status.copy_complete);
						executeOnCopyCompletion(watchKey, artifactPath, files);
						return;
					}catch (Exception e) {
						logger.error("Unable to process " + artifactPath, e);
						try {
							updateStatus(artifactPath, Status.move_failed);
							return;
						} catch (Exception e1) {
							logger.error(e1.getMessage(), e1);
						}
					}
				}
				else {
					try {
						logger.debug(artifactPath + " waiting for mxf sidecar files. Retry count " + i);
						Thread.sleep(sideCarFileWaitLoopSleepTimeInMillis);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
						return;
					}
				}
			}

			Integer retrycount = artifact_Retrycount_Map.get(artifactPath);
			if(retrycount == null) {
				retrycount = 1;
				artifact_Retrycount_Map.put(artifactPath, retrycount);
			}
			else
				artifact_Retrycount_Map.put(artifactPath, retrycount = retrycount + 1);

			if(retrycount <= 3) {
				try {
					logger.info("Could be because mxf is still not copied in full, but wait times expired[network latency etc.,]. Re-registering " + artifactPath + " - " + retrycount);
					registerAll(artifactPath);
				}catch (Exception e) {
					logger.error("Unable to re-register - " + e.getMessage(), e);
				}
			}else {// inspite of retries if side car files missing means source folder missing the mandatory files, so move it to "FAILED" folder  
				String artifactName = getArtifactName(artifactPath);
				// move it to failed folder
				Path destPath = Paths.get(watchedDir.toString(), "FAILED" , artifactName);
				try {
					move(artifactPath, destPath);
					updateStatus(artifactPath, Status.moved_to_failed_dir);
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
		}
	}

	protected void move(Path srcPath, final Path destPath) throws Exception{
		Files.createDirectories(destPath);
		Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.move(file, Paths.get(destPath.toString(), file.getFileName().toString()), StandardCopyOption.ATOMIC_MOVE);
				return FileVisitResult.CONTINUE;
			}
		});

		logger.info(srcPath + " moved succesfully to " + destPath.getParent());

		FileUtils.deleteDirectory(srcPath.toFile());
	}
	
	protected abstract void executeOnCopyCompletion(WatchKey watchKey, Path artifactPath, Collection<File> files) throws Exception;

	protected enum Status {
		copying,
		copy_complete,
		verifying,
		verified,
		md5_mismatch,
		moved,
		move_failed,
		moved_to_failed_dir,
		ingested,
		failed;
	}

	protected void updateStatus(Path child, Status status) throws Exception {
		logger.info(String.format("%s %s", child, status.name()));
	}
}

