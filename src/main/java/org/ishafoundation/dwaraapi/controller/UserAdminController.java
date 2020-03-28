package org.ishafoundation.dwaraapi.controller;

import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.Priorityband;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
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
		user.setId(21003); // TODO Arrive at it..
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
	
	// use it for login... only when 
	@PostMapping(produces = "application/json")
	@RequestMapping({ "/login" })
	public org.ishafoundation.dwaraapi.db.model.master.User login() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return userDao.findByName(name);
	}
}
