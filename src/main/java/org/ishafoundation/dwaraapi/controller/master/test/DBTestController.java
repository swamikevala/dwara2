package org.ishafoundation.dwaraapi.controller.master.test;

import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.TaskfiletypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.LibraryclassDao;
import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.PropertyDao;
import org.ishafoundation.dwaraapi.db.dao.master.RequesttypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.SequenceDao;
import org.ishafoundation.dwaraapi.db.dao.master.StorageformatDao;
import org.ishafoundation.dwaraapi.db.dao.master.TapesetDao;
import org.ishafoundation.dwaraapi.db.dao.master.TargetvolumeDao;
import org.ishafoundation.dwaraapi.db.dao.master.TaskDao;
import org.ishafoundation.dwaraapi.db.dao.master.TasksetDao;
import org.ishafoundation.dwaraapi.db.dao.master.TasktypeDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.RequesttypeUserDao;
import org.ishafoundation.dwaraapi.db.keys.RequesttypeUserKey;
import org.ishafoundation.dwaraapi.db.model.master.Taskfiletype;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Priorityband;
import org.ishafoundation.dwaraapi.db.model.master.Property;
import org.ishafoundation.dwaraapi.db.model.master.Sequence;
import org.ishafoundation.dwaraapi.db.model.master.Storageformat;
import org.ishafoundation.dwaraapi.db.model.master.Tapeset;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.master.Taskset;
import org.ishafoundation.dwaraapi.db.model.master.Tasktype;
import org.ishafoundation.dwaraapi.db.model.master.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RequesttypeUser;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
@RequestMapping("dbTest")
public class DBTestController extends SetupExtenstionFiletypeController{


	@Autowired
	private TaskfiletypeDao filetypeDao;
	
	@Autowired
	private TargetvolumeDao targetvolumeDao;
	
	@Autowired
	private PropertyDao propertyDao;	

	@Autowired
	private LibraryclassDao libraryclassDao;

	@Autowired
	private SequenceDao sequenceDao;
	
	@Autowired
	private TasktypeDao tasktypeDao;
	
	@Autowired
	private TaskDao taskDao;
	
	@Autowired
	private TasksetDao tasksetDao;
	
	@Autowired
	private TapesetDao tapesetDao;	
	
	@Autowired
	private StorageformatDao storageformatDao;		

	@Autowired
	private UserDao userDao;

	@Autowired
	private RequesttypeDao requesttypeDao;
	
	@Autowired
	private PrioritybandDao prioritybandDao;

	@Autowired
	private RequesttypeUserDao requesttypeUserDao;
    
    
	@PostMapping("/insertLibraryclass")
	public ResponseEntity<Libraryclass> insertLibraryclass() throws Exception{
		Taskfiletype filetype = filetypeDao.findById(4001).get();
		
		
		Libraryclass libraryclass = createLibraryclass(filetype);
		
		Targetvolume targetvolume = createTargetvolume();
		libraryclass.addTargetvolume(targetvolume);
		
		Tapeset tapeset = createTapeset();
    	libraryclass.addTapeset(tapeset, null, 1, true);
    	
    	Property property = createProperty();
    	libraryclass.addProperty(property, 1, true);
    	
    	Requesttype ingest = createIngestRequesttype();
    	Requesttype cancel = createCancelRequesttype();
    	Requesttype delete = createDeleteRequesttype();
    	Priorityband pb = createPriorityband();
    	User user1 = createUser1();
		user1.setPriorityband(pb);
		user1.addRequesttype(cancel, 2);
		user1.addRequesttype(delete, 2);
		user1 = userDao.save(user1);
		
		User user2 = createUser2();
		user2.setPriorityband(pb);
		user2.addRequesttype(cancel, 1);
		user2 = userDao.save(user2);
		
    	libraryclass.addRequesttypeUser(ingest, user1);
    	libraryclass.addRequesttypeUser(ingest, user2);
    	libraryclass = libraryclassDao.save(libraryclass);
		
		//LibraryclassTargetvolume libraryclassTargetvolume = new LibraryclassTargetvolume();
		return ResponseEntity.status(HttpStatus.OK).body(libraryclass); 
	}

	private Tapeset createTapeset() {
		Storageformat storageformat = new Storageformat(11001, "Bru");
		storageformatDao.save(storageformat);
		
		Tapeset tapeset = new Tapeset(15001, "pub-video" , "V5A");
		tapeset.setStorageformat(storageformat);
		return tapeset = tapesetDao.save(tapeset);
	}
	
	private Targetvolume createTargetvolume() {
		Targetvolume targetvolume = new Targetvolume();
		targetvolume.setId(17001);
		targetvolume.setName("Some Name");
		targetvolume.setPath("/data/RESTored");
		targetvolume = targetvolumeDao.save(targetvolume);
		return targetvolume;
	}
	
	private Property createProperty() {
		Property property = new Property();
		property.setId(7001);
		property.setName("Some properyt name");
		property.setRegex("sme regex");
		property.setReplaceCharSpace("some replaceCharSpace");
		property = propertyDao.save(property);
		return property;
	}

	private Sequence createSequence() {
		Sequence sequence = new Sequence();
		sequence.setId(9001);
		sequence.setPrefix("");
		sequence.setLastNumber(1);
		sequence.setKeepExtractedCode(false);
		sequence.setExtractionRegex("");
		sequence = sequenceDao.save(sequence);
		return sequence;
		//return 
	}
	
	private Task createTask(Taskfiletype filetype) {
		Task task = new Task();
		task.setTaskfiletype(filetype);
		task.setId(18001);
		task.setMaxErrors(2);
		task.setName("Task 1");
		
		Tasktype tasktype = createTasktype();
		
		task.setTasktype(tasktype);
		task = taskDao.save(task);
		
		return task;
		//return taskDao.save(task);
	}
	
	private Task createDependentTask(Taskfiletype filetype) {
		Task task = new Task();
		task.setTaskfiletype(filetype);
		task.setId(18002);
		task.setMaxErrors(2);
		task.setName("Dependent Task on Task 1");
		
		Tasktype tasktype = createDependentTasktype();
		
		task.setTasktype(tasktype);
		task = taskDao.save(task);
		return task;
		//return taskDao.save(task);
	}
	
	private Tasktype createTasktype() {
		Tasktype tasktype = new Tasktype();
		tasktype.setId(20001);
		tasktype.setName("Tasktype name 1");
		tasktype = tasktypeDao.save(tasktype);
		return tasktype;
		//return tasktypeDao.save(tasktype);
	}
	
	private Tasktype createDependentTasktype() {
		Tasktype tasktype = new Tasktype();
		tasktype.setId(20002);
		tasktype.setName("Tasktype name 2");
		tasktype = tasktypeDao.save(tasktype);
		return tasktype;
		//return tasktypeDao.save(tasktype);
	}	
	private Taskset createTaskset(){
		Taskset taskset = new Taskset();
		taskset.setId(19001);
		taskset.setName("Ts 1");
		taskset = tasksetDao.save(taskset);
		return taskset;
	}

	protected Libraryclass createLibraryclass(Taskfiletype filetype){
		Libraryclass libraryclass = new Libraryclass();
		libraryclass.setId(5001);
		//libraryclass.setLibraryclassTargetvolume(libraryclassTargetvolume);
		libraryclass.setName("Pib vdoep");
		libraryclass.setPathPrefix("some pp");
		
		
		Sequence sequence = createSequence();
		
		libraryclass.setSequence(sequence);
		
		libraryclass.setSource(true);

		libraryclass.setTaskfiletype(filetype);
		Task task = createTask(filetype);
		Task dependentTask = createDependentTask(filetype);
		
		//libraryclass.setTask(task);
		libraryclass.setGeneratorTask(task);
	
		Taskset taskset = createTaskset();
		taskset.addTask(task, null);
		taskset.addTask(dependentTask, task);
		
		taskset = tasksetDao.save(taskset);
		
		libraryclass.setTaskset(taskset);
		

		
		
		libraryclass.setDisplayOrder(1);
		libraryclass.setConcurrentCopies(false);
		
		//libraryclass = libraryclassDao.save(libraryclass);
		return libraryclass;
	}
	
	protected Requesttype createIngestRequesttype(){
    	Requesttype ingest = new Requesttype(8001, "Ingest");
    	return ingest = requesttypeDao.save(ingest);
    	
	}
	private Requesttype createCancelRequesttype(){
		Requesttype cancel = new Requesttype(8003, "Cancel");
		return cancel = requesttypeDao.save(cancel);
	}
	
	private Requesttype createDeleteRequesttype(){
		Requesttype delete = new Requesttype(8004, "Delete");
		delete = requesttypeDao.save(delete);
		return delete;
	}
	
	private Priorityband createPriorityband(){
		Priorityband pb = new Priorityband();
		pb.setId(6001);
		pb.setName("Band 1");
		pb.setStart(5);
		pb.setEnd(10);
		pb.setOptimizeTapeAccess(true);
		pb = prioritybandDao.save(pb);
		return pb;
	}
	
	private User createUser1(){
		User user1 = new User();
		user1.setId(21001);
		user1.setName("swami.kevala");
		String hash = "";
		user1.setHash(hash);
		return user1;
	}
	
	private User createUser2(){	
		User user2 = new User();
		user2.setId(21002);
		user2.setName("maa.jeevapushpa");
		String hash2 = "";
		user2.setHash(hash2);
		return user2;
	}
	

	@GetMapping("/retrieveTests")
	public ResponseEntity<String> retrieveTests() throws Exception{
		int libraryClassId = 5001;
		// test 1 - check if all associated objects on libraryclass like sequence etc are retrieved to get the seq Id etc....
		Libraryclass libraryclass2 = libraryclassDao.findById(libraryClassId).get();
		System.out.println("Are the associated objects retrieved? " + libraryclass2.getSequence().getId());
		
		ObjectMapper mapper = new ObjectMapper(); 
		mapper.enable(SerializationFeature.INDENT_OUTPUT); 
		System.out.println("resp json - " + mapper.writeValueAsString(libraryclass2));
		
		// test 2 - retrieve the list of target volumes for a library class
//		List<Targetvolume> targetvolumeList = targetvolumeDao.findAllByLibraryclassTargetvolumeLibraryclassId(libraryClassId);
//		for (Targetvolume targetvolume2 : targetvolumeList) {
//			System.out.println("resp - " + targetvolume2.getName());
//			System.out.println("resp json - " + mapper.writeValueAsString(targetvolume2));
//		}
		
		// test 3 - retrieve the list of library classes for a target volume
		// TODO
		
		//LibraryclassTargetvolume libraryclassTargetvolume = new LibraryclassTargetvolume();
		return ResponseEntity.status(HttpStatus.OK).body("Done"); 
	}
	
    @GetMapping("/permissions")
    public boolean getPermissionLevel(){
    	int forUser = 21001;
    	int requesttypeId = 8003;
    	
    	RequesttypeUser requesttypeUser = requesttypeUserDao.findByRequesttypeIdAndUserId(requesttypeId, forUser);
    	System.out.println("Response returned with unnecessary joins - " + requesttypeUser.getPermissionLevel());

    	RequesttypeUserKey requesttypeUserKey =  new RequesttypeUserKey(requesttypeId, forUser);
    	RequesttypeUser requesttypeUser2 = requesttypeUserDao.findById(requesttypeUserKey).get();
    	System.out.println("with no joins - " + requesttypeUser2.getPermissionLevel());
    	/*
    	 * Wrong query
    	RequesttypeUser requesttypeUser2 = requesttypeUserDao.findByRequesttypeRequesttypeIdAndUserUserId(requesttypeId, forUser);
    	System.out.println("with no joins - " + requesttypeUser2.getPermissionLevel());
    	*/
		return true;
    	
    }	
	 
//	@PostMapping("/insertTaskTaskset")
//	public ResponseEntity<String> insertTaskTaskset() throws Exception{
//		// libraryclass --> tasksetId
//		int taskSetId = 19001;
//		List<Task> taskList = taskDao.findAllByTasksetsTasksetId(taskSetId);
//		for (Task nthTask : taskList) {
//			System.out.println(nthTask.getName());
//		}
//		return null;
//	}
	 
	@GetMapping("/retrieveTasks")
	public ResponseEntity<String> retrieveTasks() throws Exception{
		// libraryclass --> tasksetId
		int taskSetId = 19001;
		List<Task> taskList = taskDao.findAllByTasksetsTasksetId(taskSetId);
		for (Task nthTask : taskList) {
			System.out.println(nthTask.getName());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Done"); 
	}
	
	
//    @GetMapping("/testExtensionFiletypeRelationshipBehaviour")
//    public ResponseEntity<ExtensionFiletype> testExtensionFiletypeRelationshipBehaviour(){
//    	Extension extension1 = new Extension();
//    	extension1.setId((long) 1);
//    	extension1.setName("MP4");
//    	extension1.setDescription("Some MP4 Description");
//    	extensionDao.save(extension1);
//    	
//    	Extension extension2 = new Extension();
//    	extension2.setId((long) 2);
//    	extension2.setName("MOV");
//    	extension2.setDescription("Some MOV Description");
//    	extensionDao.save(extension2);
//    	
//    	Extension extension3 = new Extension();
//    	extension3.setId((long) 3);
//    	extension3.setName("MP3");
//    	extension3.setDescription("Some MP3 Description");
//    	extensionDao.save(extension3);    	
//    	
//    	Filetype filetype1 = new Filetype();
//    	filetype1.setId((long) 1);
//    	filetype1.setName("Video");
//    	filetypeDao.save(filetype1);
//
//    	Filetype filetype2 = new Filetype();
//    	filetype2.setId((long) 2);
//    	filetype2.setName("Audio");
//    	filetypeDao.save(filetype2);
//    	
//    	ExtensionFiletypeKey extensionFiletypeKey1 = new ExtensionFiletypeKey((long) 1, (long) 1);
//    	ExtensionFiletype extensionFiletype1 = new ExtensionFiletype();
//    	extensionFiletype1.setId(extensionFiletypeKey1);
//    	extensionFiletype1.setSidecar(true);
//    	extensionFiletype1.setExtension(extension1);
//    	extensionFiletype1.setTaskfiletype(filetype1);
//    	extensionFiletypeDao.save(extensionFiletype1);
//    	
//    	
//		Filetype filetype = filetypeDao.findById((long) 1).get();
//		
//		ObjectMapper mapper = new ObjectMapper(); 
//		mapper.enable(SerializationFeature.INDENT_OUTPUT); 
//		try {
//			System.out.println("resp - " + mapper.writeValueAsString(filetype));
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//    	return ResponseEntity.status(HttpStatus.OK).body(extensionFiletype1); 
//    }
}
