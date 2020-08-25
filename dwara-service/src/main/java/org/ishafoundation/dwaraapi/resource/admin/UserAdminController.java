package org.ishafoundation.dwaraapi.resource.admin;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.ishafoundation.dwaraapi.configuration.Configuration;
import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.ArtifactclassActionUserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Artifactclass;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Priorityband;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.ArtifactclassActionUser;
import org.ishafoundation.dwaraapi.db.model.master.reference.Action;
import org.ishafoundation.dwaraapi.db.utils.ConfigurationTablesUtil;
import org.ishafoundation.dwaraapi.utils.FilePermissionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class UserAdminController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserAdminController.class);
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private PrioritybandDao prioritybandDao;
	
	@Autowired
	private ArtifactclassActionUserDao artifactclassActionUserDao;
	
	@Autowired
	private ConfigurationTablesUtil configurationTablesUtil;
	
	@Autowired
	private Configuration configuration;
		
	@RequestMapping(value = "/createUserAndAddUserToArtifactclassAndCreateDir", method = RequestMethod.POST)
	public ResponseEntity<String> createUserAndAddUserToArtifactclassAndCreateDir(@RequestParam String username, @RequestParam String password, @RequestParam String artifactclassId, @RequestParam String action) {
		try {
			register(username, password);
			addUserToArtifactclassAndCreateDir(username, artifactclassId, action);
		}catch (Exception e) {
			logger.error("unable to createUserAndAddUserToArtifactclassAndCreateDir" ,e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	@RequestMapping(value = "/addUserToArtifactclassAndCreateDir", method = RequestMethod.POST)
	public ResponseEntity<String> addUserToArtifactclassAndCreateDir(@RequestParam String username, @RequestParam String artifactclassId, @RequestParam String action) {
		try {
			addUserToArtifactclass(username, artifactclassId, action);
			
			File userSpecificIngestContentGroupDirPath = FileUtils.getFile(configuration.getReadyToIngestSrcDirRoot(), username, action, artifactclassId);
			FileUtils.forceMkdir(userSpecificIngestContentGroupDirPath);
			logger.trace(userSpecificIngestContentGroupDirPath.getAbsolutePath() + " created");
			
			FilePermissionsUtil.changePermissions(FileUtils.getFile(configuration.getReadyToIngestSrcDirRoot(), username), "rwxrwxrwx", "rwxrwxrwx");
			logger.trace(userSpecificIngestContentGroupDirPath.getAbsolutePath() + " permissions set");
		}catch (Exception e) {
			logger.error("unable to addUserToArtifactclassAndCreateDir" ,e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}

	@RequestMapping(value = "/addUserToArtifactclass", method = RequestMethod.POST)
	public ResponseEntity<String> addUserToArtifactclass(@RequestParam String username, @RequestParam String artifactclassId, @RequestParam String action) {
		try {
			Artifactclass artifactclass = configurationTablesUtil.getArtifactclass(artifactclassId);
			Action actionObj = configurationTablesUtil.getAction(org.ishafoundation.dwaraapi.enumreferences.Action.valueOf(action));
			User user = userDao.findByName(username);
			ArtifactclassActionUser artifactclassActionUser = new ArtifactclassActionUser(artifactclass, actionObj, user);
			artifactclassActionUserDao.save(artifactclassActionUser);
			logger.trace("artifactclassActionUser created");
		}catch (Exception e) {
			logger.error("unable to addUserToArtifactclass" ,e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
		password = new BCryptPasswordEncoder().encode(password);
		
		User user = new User();
		
		User lastUser = userDao.findTopByOrderByIdDesc();
		
		user.setId(lastUser.getId() + 1); // TODO Arrive at it..
		user.setName(username);
		user.setHash(password);

		Priorityband priorityband = prioritybandDao.findById(1).get(); // TODO hardcoded to 1st PriorityBand
		user.setPriorityband(priorityband);
		
		userDao.save(user);
		logger.trace("User created");
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	@RequestMapping(value = "/setpassword", method = RequestMethod.POST)
	public ResponseEntity<String> setpassword(@RequestParam String username, @RequestParam String password) {
		password = new BCryptPasswordEncoder().encode(password);
		
		User user = userDao.findByName(username);
		user.setHash(password);

		userDao.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}

	
}
