package org.ishafoundation.dwaraapi.storage.storagetype.tape;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class TapeJobSelectorTests {
//	
//	private static final Logger logger = LoggerFactory.getLogger(TapeJobSelectorTests.class);
//	
//	@Autowired
//	private StorageJobBuilder storageJobBuilder;
//	
//	@Autowired
//	private TapeJobSelector tapeJobSelector;
//	
//	@Autowired
//	private TapeLibraryManager tapeLibraryManager;	
//	
//	private List<Job> jobList = new ArrayList<Job>();
//
//	@Before
//	public void JobManager_processJobs() {
//    	Request request = new Request();
//    	request.setAction(Action.ingest);
//    	
//    	Subrequest subrequest = new Subrequest();
//    	subrequest.setRequest(request);
//    	
//    	Libraryclass libraryclass = new Libraryclass();
//    	libraryclass.setName("pub-video");
//    	libraryclass.setSource(true);
//    	libraryclass.setConcurrentCopies(false);
//    	libraryclass.setPathPrefix("C://data//ingested");
//    	
//    	Library library = new Library();
//    	library.setName("10058_Guru-Pooja-Offerings-Close-up-Shot_AYA-IYC_15-Dec-2019_X70_9");
//    	library.setLibraryclass(libraryclass);
//    	
////    	Task task = new Task();
////    	task.setId(18001);
////    	task.setCopyNumber(1);
//    	
//    	Tape tape = new Tape();
//    	tape.setBarcode("V5A003");
//
//    	Job job1 = new Job();
//    	job1.setId(1);
//    	job1.setSubrequest(subrequest);
//    	job1.setInputLibrary(library);
////    	TODO job1.setTasktype(tasktype);
//    	job1.setTaskId(18001);
//    	job1.setTape(tape);
//    	
//    	jobList.add(job1);
//	}
//	
//    @Test
//    public void StorageJobsManager_ThreadTask_run() throws Exception {
//
//    	
//    	List<StorageJob> tapeStorageJobsList = new ArrayList<StorageJob>();
//		
//		for (Iterator<Job> iterator = jobList.iterator(); iterator.hasNext();) {
//			Job job = (Job) iterator.next();
//			
//			tapeStorageJobsList.add(storageJobBuilder.buildStorageJob(job));
//		}
//    	
//		TapeJobsManager_Manage(tapeStorageJobsList);
//
//    }
//    
//    private void TapeJobsManager_Manage(List<StorageJob> tapeStorageJobsList) {
//
//		// STEP 1
//		// TODO : Should we use the DB to get the drive list or from mtx
//		// My bet is mtx as it would have the most latest status...
//		// Should we validate it against DB...
//		logger.trace("Getting Available Drives List");
//		List<DriveStatusDetails> availableDrivesList = tapeLibraryManager.getAvailableDrivesList();
//		if(availableDrivesList.size() > 0) { // means drive(s) available
//			logger.trace("No. of drives available "+ availableDrivesList.size());
//
//			// TODO - To load balance across drives based on their usage. The usage parameters is not retrieved...
////			Map<Integer, DriveStatusDetails> usage_driveStatusDetails = new TreeMap<Integer, DriveStatusDetails>(); 
////			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
////				DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
////				usage_driveStatusDetails.put(driveStatusDetails.getTotalUsageInHours(), driveStatusDetails); // TODO Need to decide based on what parameter the load has to be balanced...
////			}
////			Set<Integer> treeSet = new TreeSet<Integer>();
////			treeSet.addAll(usage_driveStatusDetails.keySet());
////			for (Integer usageHours : treeSet) {
////				DriveStatusDetails driveStatusDetails = usage_driveStatusDetails.get(usageHours);
////				// code goes here
////			}
//
//			// we need to allocate as many jobs for processing
//			for (Iterator<DriveStatusDetails> driveStatusDetailsIterator = availableDrivesList.iterator(); driveStatusDetailsIterator.hasNext();) {
//				DriveStatusDetails driveStatusDetails = (DriveStatusDetails) driveStatusDetailsIterator.next();
//				logger.debug("Now selecting job for drive - " + driveStatusDetails.getDriveSNo());
//
//				// STEP 2a
//				StorageJob storageJob = tapeJobSelector.getJob(tapeStorageJobsList, driveStatusDetails);
//				
//				// STEP 2b
//				if(storageJob == null) {
//					logger.debug("No tape jobs in queue are eligible to be processed for the drive");
//					//break;
//				}
//				else if(storageJob != null) {
//					logger.info("Job " + storageJob.getJob().getId() + " selected for " + driveStatusDetails.getDriveSNo());
//					
//					// STEP 3 filter the job from the next iteration
//					tapeStorageJobsList.remove(storageJob);
//				}
//				
//
//				if(tapeStorageJobsList.size() <= 0) {
//					logger.debug("No tape jobs in queue anymore. So skipping the loop");
//					break;
//				}
//			}
//		}
//		else {
//			logger.info("All drives busy");
//		}
//	
//    }
//
}
