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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.commandline.local.CommandLineExecuterImpl;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcherForCopiedFiles {

	private static Logger logger = LoggerFactory.getLogger(DirectoryWatcherForCopiedFiles.class);
	
	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private boolean trace = true;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static Path opsDirLoc = null;
	private static List<String> csvEntries = null;
	private static Executor executor = null;
	private static File csvFile = null;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	DirectoryWatcherForCopiedFiles(Path dir, Long waitTime) throws IOException {
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
		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
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
							try {
//								createCSVEntry(child);
								updateStatus(child, Status.copying);
//								Path prev = keys.get(key);
//								if (prev != null && !dir.equals(prev)) {
//									updateStatus(child, Status.done);
//									deleteCSVEntry(prev);
//									System.out.format("update: %s -> %s\n", prev, dir);
//								}

								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerAll(child);
								}
								
//								Path newDir = keys.get(key);
//								//System.out.format("test :: %s -> %s\n", newDir, child);
//								if (newDir != null && !dir.equals(newDir)) { // if its renamed
//									renameEntry(newDir, child);
//									System.out.format("update: %s -> %s\n", newDir, child);
//								}
//								else {
//									createCSVEntry(child);
//								}

							} catch (IOException x) {
								// ignore to keep sample readbale
							}
						}
						else if (kind == ENTRY_MODIFY) {
//							Path artifactPath = getArtifactPath(child);
//							if (trace)
//								System.out.println("artifactPath " + artifactPath);
							
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
								System.out.println(child + " lastModified " + lastModified);
							expirationTimes.put(child, lastModified.toMillis()+newFileWait);
						}
//						else if (kind == ENTRY_DELETE) {
//							updateStatus(child, Status.moved);
//						}
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

				handleExpiredWaitTimes(key, currentTime);

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
		String artifactName = getArtifactName(child);
		if (trace)
			System.out.println("artifactName " + artifactName);
		return Paths.get(watchedDir.toString(), artifactName); 
	}

	private String getArtifactName(Path child){
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
				if(filePath.getFileName().toString().endsWith(".mxf"))
					verifyChecksumAndMoveToOpsArea(watchKey, filePath);
				
//				Integer copiedFileCountOfArtifact = artifactPath_CopiedFileCount.get(artifactPath);
//				//System.out.println(key + ":" + copiedFileCountOfArtifact);
//				if(copiedFileCountOfArtifact != null)
//					copiedFileCountOfArtifact = copiedFileCountOfArtifact + 1;
//				else 
//					copiedFileCountOfArtifact = 1;
//				
//				artifactPath_CopiedFileCount.put(artifactPath, copiedFileCountOfArtifact);
//				System.out.println(artifactPath + ":" + copiedFileCountOfArtifact);
//				if(copiedFileCountOfArtifact == 1) {
//					System.out.println(artifactPath + " copy completed");
//					verifyChecksumAndMoveToOpsArea(artifactPath);
//				}
				// System.out.println("expired " + item.getKey());

				it.remove();
			}
		}
	}

    private void verifyChecksumAndMoveToOpsArea(WatchKey watchKey, Path path) throws Exception{
		VerifyChecksumAndMoveToOpsAreaTask checksumTask = new VerifyChecksumAndMoveToOpsAreaTask();//applicationContext.getBean(TapeTask.class); 
		checksumTask.setWatchKey(watchKey);
		checksumTask.setMxfFilePath(path);
		executor.execute(checksumTask);
    }
    
	public class VerifyChecksumAndMoveToOpsAreaTask implements Runnable{
		
		private Path mxfFilePathname;
		
		private WatchKey watchKey;
		
		public void setMxfFilePath(Path mxfFilePathname) {
			this.mxfFilePathname = mxfFilePathname;
		}
		
		public void setWatchKey(WatchKey watchKey) {
			this.watchKey = watchKey;
		}
		
		@Override
		public void run() {
			String expectedMd5 = null;
			String actualMd5 = null;
			Path artifactPath = mxfFilePathname.getParent();
			File artifactFileObj = artifactPath.toFile();
			while(true) {
				//logger.debug(mxfFilePathname + ":" + artifactPath);
				if(!artifactFileObj.isDirectory())
					return;
				Collection<File> files = FileUtils.listFiles(artifactFileObj, null, false);
				if(files.size() == 4) {
					try {
						updateStatus(artifactPath, Status.copy_complete);
						for (File file : files) {
							String fileName = file.getName();
							//System.out.println(fileName);
							if(fileName.endsWith(".md5")) {
								// TODO : What will be the md5 file content sample looking like ? // d44f11c6df36d8be680db44e969e7ff0  sample-ntsc.mxf
								expectedMd5 = StringUtils.substringBefore(FileUtils.readFileToString(file), "  ").trim().toUpperCase();
							}
							else if(fileName.endsWith(".mxf")) {
								updateStatus(artifactPath, Status.verifying);
								
							    byte[] digest =  ChecksumUtil.getChecksum(file, Checksumtype.md5);
							    actualMd5 = DatatypeConverter.printHexBinary(digest).toUpperCase();
							}
						}
						logger.debug(artifactPath + " expectedMd5 " + expectedMd5 + " actualMd5 " + actualMd5);
						if(expectedMd5.equals(actualMd5)) {
							updateStatus(artifactPath, Status.verified);
							moveFolderToOpsAreaAndOrganise(watchKey, artifactPath);
							updateStatus(artifactPath, Status.moved);
						}else {
							logger.error(artifactPath + "MD5 expected != actual, Now what??? ");
							updateStatus(artifactPath, Status.md5_mismatch);
						}
						break;
					}catch (Exception e) {
						e.printStackTrace();
						try {
							updateStatus(artifactPath, Status.move_failed);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				else {
					try {
						//System.out.println("waiting for all files");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		private void moveFolderToOpsAreaAndOrganise(WatchKey watchKey, Path srcPath) throws Exception {
			try {
				if(watchKey!=null)
					watchKey.cancel();
//				// reset key and remove from set if directory no longer accessible
//				boolean valid = key.reset();
//				if (!valid) {
//					if (trace) {
//						System.out.println(key + " not valid");
//						System.out.println("Will be removing " + keys.get(key) + " from key list");
//					}
//					keys.remove(key);
	//
//					// all directories are inaccessible
//					if (keys.isEmpty()) {
//						break;
//					}
//				}
	//
//				Collection<File> files = FileUtils.listFiles(artifactPath.toFile(), null, false);
//				System.out.println(key + ":" + files.size());
//				if(files.size() == 4) {
//					System.out.println(artifactPath + " copy completed");
//					
//				}


				String artifactName = getArtifactName(srcPath);
				final Path dest = Paths.get(opsDirLoc.toString(), artifactName, "mxf");
				Files.createDirectories(dest);
				Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.move(file, Paths.get(dest.toString(), file.getFileName().toString()), StandardCopyOption.ATOMIC_MOVE);
						return FileVisitResult.CONTINUE;
					}
				});
				CommandLineExecuterImpl clei = new CommandLineExecuterImpl();
				clei.executeCommand("chmod -R 777 " + dest.getParent().toString(), false);
				
				FileUtils.deleteDirectory(srcPath.toFile());
			} catch (IOException e) {
				e.printStackTrace();
				updateStatus(srcPath, Status.move_failed);
			}
		}
	}

    
	
	private void createCSVEntry(Path child) throws Exception {
		String artifactName = getArtifactName(child);

		List<String> csvEntriesToBeRemoved = findEntries(artifactName);
		csvEntries.removeAll(csvEntriesToBeRemoved);

		csvEntries.add(artifactName + ", " + LocalDateTime.now() + ", " + Status.copying);//.contains(o)
		FileUtils.writeLines(csvFile, csvEntries);
	}
	

	private void deleteCSVEntry(Path prev) throws Exception {
		String artifactName = getArtifactName(prev);
		List<String> csvEntriesToBeRemoved = findEntries(artifactName);
		csvEntries.removeAll(csvEntriesToBeRemoved);
		FileUtils.writeLines(csvFile, csvEntries);
	}
	
	private List<String> findEntries(String artifactName) {
		// Add only when artifact not there. For QC failure scenarios the file would come again...
		List<String> csvEntriesInvolved = new ArrayList<String>();
		for (String nthCSVEntry : csvEntries) {
			if(nthCSVEntry.startsWith(artifactName + ", ")) {
				csvEntriesInvolved.add(nthCSVEntry);
				//break; // Dont break so even if there are dupe entries they can be updated...
			}
		}
		return csvEntriesInvolved;
	}
	
	private void renameEntry(Path prev, Path child) {
		String oldArtifactName = getArtifactName(prev);
		String newArtifactName = getArtifactName(child);
		for (String nthCSVEntry : csvEntries) {
			String searchString = oldArtifactName + ", ";
			String replaceString = newArtifactName + ", ";
			if(nthCSVEntry.startsWith(searchString)) {
		        int index = csvEntries.indexOf(nthCSVEntry);

				nthCSVEntry = nthCSVEntry.replaceAll(searchString, replaceString);
				System.out.println(oldArtifactName + " --> " + newArtifactName);
				csvEntries.set(index, nthCSVEntry);

				//break; // Dont break so even if there are dupe entries they are updated...
			}
		}
	}
    
    private enum Status {
    	copying,
    	copy_complete,
    	verifying,
    	verified,
    	md5_mismatch,
    	moved,
    	move_failed,
    	failed;
    }
    
	private void updateStatus(Path child, Status status) throws Exception {
		logger.debug(String.format("%s %s", child, status.name()));
		//updateLog(child + ":" + status.name());
		
//
//		String artifactName = getArtifactName(child);
//		// Add only when artifact not there. For QC failure scenarios the file would come again...
//		for (String nthCSVEntry : csvEntries) {
//			if(nthCSVEntry.startsWith(artifactName + ", ")) {
//		        int index = csvEntries.indexOf(nthCSVEntry);
//
//				nthCSVEntry = nthCSVEntry.replaceAll(", [^,]*$", ", " + status.name());
//				//System.out.println(artifactName + " status " + status.name());
//				csvEntries.set(index, nthCSVEntry);
//				// break; // Dont break so even if there are dupe entries they are updated
//			}
//		}
//		FileUtils.writeLines(csvFile, csvEntries);
	}

	static void usage() {
		System.err.println("usage: java DirectoryWatcher [waitTimeInSecs] <dirToBeWatched(\"/data/user/pgurumurthy/ingest\")> <opsDirLocation(\"/data/preprod/ops\")> noOfThreadsForChecksumVerification");
		System.exit(-1);
	}
	
	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length == 0 || args.length > 4)
			usage();

		int dirArg = 0;
		long waitTimes = 0L;
		if (args.length == 4) {
			waitTimes = Long.parseLong(args[0]) * 1000;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);

		opsDirLoc = Paths.get(args[dirArg + 1]);
		String csvFilepathname = opsDirLoc + File.separator + "prasad-transfer.csv";
		csvFile = new File(csvFilepathname);  
		if(!csvFile.exists())
			FileUtils.write(csvFile, "artifact name, datetime copied, status", false);
		
		csvEntries = FileUtils.readLines(csvFile);
		
		int noOfThreads = Integer.parseInt(args[dirArg + 2]);
		executor = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		new DirectoryWatcherForCopiedFiles(dir, waitTimes).processEvents();
	}
}
