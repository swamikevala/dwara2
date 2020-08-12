package org.ishafoundation.dwaraapi.resource.admin;

import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Priorityband;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAdminController {
	
	@Autowired
	UserDao userDao;

	@Autowired
	PrioritybandDao prioritybandDao;
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> processRegister(@RequestParam String username, @RequestParam String password) {
		password = new BCryptPasswordEncoder().encode(password);
		
		User user = new User();
		
		User lastUser = userDao.findTopByOrderByIdDesc();
		
		user.setId(lastUser.getId() + 1); // TODO Arrive at it..
		user.setName(username);
		user.setHash(password);

		Priorityband priorityband = prioritybandDao.findById(1).get(); // TODO hardcoded to 1st PriorityBand
		user.setPriorityband(priorityband);
		
		userDao.save(user);
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
