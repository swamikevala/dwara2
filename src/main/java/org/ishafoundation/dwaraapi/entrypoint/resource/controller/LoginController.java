package org.ishafoundation.dwaraapi.entrypoint.resource.controller;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class LoginController {
	
	@Autowired
	UserDao userDao;
	
	// use it for login... only when the jsessionid cookie is either not set or expired...
	@PostMapping(produces = "application/json")
	@RequestMapping({ "/login" })
	public org.ishafoundation.dwaraapi.db.model.master.User login() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return userDao.findByName(name);
	}
}
