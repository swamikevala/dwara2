package org.ishafoundation.dwaraapi.entrypoint.resource.controller.admin;

import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.Priorityband;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAdminController {

	@Autowired
	JdbcUserDetailsManager jdbcUserDetailsManager;
	
	@Autowired
	UserDao userDao;

	@Autowired
	PrioritybandDao prioritybandDao;
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> processRegister(@RequestParam String username, @RequestParam String password) {
		password = new BCryptPasswordEncoder().encode(password);
		
		org.ishafoundation.dwaraapi.db.model.master.User user = new org.ishafoundation.dwaraapi.db.model.master.User();
		
		org.ishafoundation.dwaraapi.db.model.master.User lastUser = userDao.findTopByOrderByIdDesc();
		
		user.setId(lastUser.getId() + 1); // TODO Arrive at it..
		user.setName(username);
		user.setHash(password);

		Priorityband priorityband = new Priorityband(); // TODO : hardcoded
		priorityband.setId(12001);
		priorityband.setName("some prin name");
		priorityband.setStart(4);
		priorityband.setEnd(5);
		priorityband.setOptimizeTapeAccess(true);
		//priorityband = prioritybandDao.save(priorityband);
		
		priorityband = null;
		user.setPriorityband(priorityband); // TODO : setting to null
		
		userDao.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}
	
	@RequestMapping(value = "/setpassword", method = RequestMethod.POST)
	public ResponseEntity<String> setpassword(@RequestParam String username, @RequestParam String password) {
		password = new BCryptPasswordEncoder().encode(password);
		
		org.ishafoundation.dwaraapi.db.model.master.User user = userDao.findByName(username);
		user.setHash(password);

		userDao.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("Done");
	}

	
}
