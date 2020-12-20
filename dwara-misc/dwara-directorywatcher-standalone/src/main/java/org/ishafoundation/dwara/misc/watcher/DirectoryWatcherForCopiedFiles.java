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
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ishafoundation.dwaraapi.enumreferences.Checksumtype;
import org.ishafoundation.dwaraapi.utils.ChecksumUtil;


/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcherForCopiedFiles {

	private final WatchService watchService;
	private final Path watchedDir;
	private final Map<WatchKey,Path> keys;
	private boolean trace = true;
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	private Long newFileWait = 10000L;
	private static String csvFilepathname = "prasad-transfer.csv";
	private static File csvFile = new File(csvFilepathname);  
	private static List<String> csvEntries = null;
	
	static {
		try {
			if(!csvFile.exists())
				FileUtils.write(csvFile, "artifact name, datetime copied, status", false);
			
			csvEntries = FileUtils.readLines(csvFile);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
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
								createCSVEntry(child);
								if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
									registerAll(child);
								}
							} catch (IOException x) {
								// ignore to keep sample readbale
							}
						}
						else if (kind == ENTRY_MODIFY) {
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
						else if (kind == ENTRY_DELETE) {
							updateStatus(child, Status.moved);
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
	
	private void handleExpiredWaitTimes(Long currentTime) {
		// Start import for files for which the expirationtime has passed

		Iterator<Entry<Path, Long>> it = expirationTimes.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Path, Long> item = (Entry<Path, Long>) it.next();
			Long expiryTime = (Long) item.getValue();
			if(expiryTime <= currentTime) {
				action(item.getKey());
				// System.out.println("expired " + item.getKey());
				// do something with the file
				it.remove();
			}
		}
	}

    private void action(Path child){
		System.out.println(child + " copy completed");
		//System.out.println("Method assumes Prasad supplies a \"MD5.txt\" and the video file ending with \".mxf\" in the supplied folder");
		String expectedMd5 = null;
		String actualMd5 = null;
		
		try {
			Collection<File> files = FileUtils.listFiles(child.toFile(), null, true);
			for (File file : files) {
				String fileName = file.getName();
				//System.out.println(fileName);
				if(fileName.endsWith(".md5")) {
					// TODO : What will be the md5 file content sample looking like ? // d44f11c6df36d8be680db44e969e7ff0  sample-ntsc.mxf
					expectedMd5 = StringUtils.substringBefore(FileUtils.readFileToString(file), "  ").trim().toUpperCase();
					System.out.println("expectedMd5 " + expectedMd5);
				}
				else if(fileName.endsWith(".mxf")) {
					updateStatus(child, Status.verifying);
				    byte[] digest = ChecksumUtil.getChecksum(file, Checksumtype.md5);
				    actualMd5 = DatatypeConverter.printHexBinary(digest).toUpperCase();
				    System.out.println("actualMd5 " + actualMd5);
				}
			}
			if(expectedMd5.equals(actualMd5)) {
				System.out.println("MD5 expected = actual, call dwara api for " + child);
				updateStatus(child, Status.done);
			}else {
				System.out.println(child + "MD5 expected != actual, Now what??? ");
				updateStatus(child, Status.md5_mismatch);
			}
		}catch (Exception e) {
			
			e.printStackTrace();
		}
    }
    
	private void createCSVEntry(Path child) throws Exception {
		String artifactName = getArtifactName(child);
		// Add only when artifact not there. For QC failure scenarios the file would come again...
		 List<String> csvEntriesToBeRemoved = new ArrayList<String>();
		for (String nthCSVEntry : csvEntries) {
			if(nthCSVEntry.startsWith(artifactName + ", ")) {
				csvEntriesToBeRemoved.add(nthCSVEntry);
				//break;
			}
		}
		csvEntries.removeAll(csvEntriesToBeRemoved);
		
		csvEntries.add(artifactName + ", " + LocalDateTime.now() + ", " + Status.copying);//.contains(o)
		FileUtils.writeLines(csvFile, csvEntries);
	}
	

	private void updateStatus(Path child, Status status) throws Exception {
		String artifactName = getArtifactName(child);
		// Add only when artifact not there. For QC failure scenarios the file would come again...
		for (String nthCSVEntry : csvEntries) {
			if(nthCSVEntry.startsWith(artifactName + ", ")) {
		        int index = csvEntries.indexOf(nthCSVEntry);

		        System.out.println("b4 " + nthCSVEntry);
				nthCSVEntry = nthCSVEntry.replaceAll(", [^,]*$", ", " + status.name());
				System.out.println("after " + nthCSVEntry);
				csvEntries.set(index, nthCSVEntry);
				break;
			}
		}
		FileUtils.writeLines(csvFile, csvEntries);
	}
    
    
	static void usage() {
		System.err.println("usage: java DirectoryWatcher [waitTimeInSecs] <dirToBeWatched(\"/data/user/pgurumurthy/ingest\")>");
		System.exit(-1);
	}

    private enum Status {
    	copying,
    	verifying,
    	done,
    	md5_mismatch,
    	moved;
    }
    
	public static void main(String[] args) throws Exception {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();

		int dirArg = 0;
		long waitTimes = 0L;
		if (args.length == 2) {
			waitTimes = Long.parseLong(args[0]) * 1000;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		

		new DirectoryWatcherForCopiedFiles(dir, waitTimes).processEvents();
	}
}
