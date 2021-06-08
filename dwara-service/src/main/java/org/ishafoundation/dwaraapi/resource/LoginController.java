package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.RoleUserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RoleUser;
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
	
	@Autowired
	RoleUserDao roleUserDao;
	
	
	// use it for login... only when the jsessionid cookie is either not set or expired...
	@PostMapping(produces = "application/json")
	@RequestMapping({ "/login" })
	public org.ishafoundation.dwaraapi.api.resp.login.User login() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User userFromDB = userDao.findByName(name);
		
		org.ishafoundation.dwaraapi.api.resp.login.User userResp = new org.ishafoundation.dwaraapi.api.resp.login.User();
		int userId = userFromDB.getId();
		userResp.setId(userId);
		userResp.setName(userFromDB.getName());
		
		List<String> roleList = new ArrayList<String>();
		List<RoleUser> roleUserList = roleUserDao.findAllByIdUserId(userId);
		for (RoleUser roleUser : roleUserList) {
			roleList.add(roleUser.getRole().getName());
		}
		userResp.setRole(roleList);
		return userResp;
	}
}
