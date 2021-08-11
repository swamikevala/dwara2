package org.ishafoundation.dwaraapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.ishafoundation.dwaraapi.api.resp.login.GoogleLoginResponse;
import org.ishafoundation.dwaraapi.authn.MyPasswordEncoder;
import org.ishafoundation.dwaraapi.db.dao.master.PrioritybandDao;
import org.ishafoundation.dwaraapi.db.dao.master.UserDao;
import org.ishafoundation.dwaraapi.db.dao.master.jointables.RoleUserDao;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Priorityband;
import org.ishafoundation.dwaraapi.db.model.master.configuration.User;
import org.ishafoundation.dwaraapi.db.model.master.jointables.RoleUser;
import org.ishafoundation.dwaraapi.exception.DwaraException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class LoginController {
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	RoleUserDao roleUserDao;
	
	@Autowired
	private PrioritybandDao prioritybandDao;

	@Autowired 
    private MyPasswordEncoder myPasswordEncoder;

	private static String CLIENT_ID = "929349570862-2phhjnccr7v3loq06hs9prkn0rv2f961.apps.googleusercontent.com";
	
	
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

	@PostMapping(value="/googleLogin", produces = "application/json")
    public GoogleLoginResponse googleLogin(@RequestBody String idTokenString) {
		// System.out.println("google token: " + idTokenString);
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),new GsonFactory())
			// Specify the CLIENT_ID of the app that accesses the backend:
			.setAudience(Collections.singletonList(CLIENT_ID))
			// Or, if multiple clients access the backend:
			//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
			.build();

		// (Receive idTokenString by HTTPS POST)

		GoogleIdToken idToken;
		try {
			idToken = verifier.verify(idTokenString);
			if (idToken != null && idToken.verifyExpirationTime(System.currentTimeMillis(), 60)) {
				Payload payload = idToken.getPayload();
		
				// Print user identifier
				String googleUserId = payload.getSubject();
				// System.out.println("Google User ID: " + googleUserId);
		
				/* // Get profile information from payload
				String email = payload.getEmail();
				boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");
				String locale = (String) payload.get("locale");
				String familyName = (String) payload.get("family_name");
				String givenName = (String) payload.get("given_name"); */
		
				// Use or store profile information
				GoogleLoginResponse googleLoginResponse = new GoogleLoginResponse();
				User userFromDB = userDao.findByGoogleId(googleUserId);
		
				if(userFromDB == null) {
					userFromDB = new User();
					User lastUser = userDao.findTopByOrderByIdDesc();
					
					userFromDB.setId(lastUser.getId() + 1);
					String givenName = (String) payload.get("given_name");
					String familyName = (String) payload.get("family_name");
					userFromDB.setName(givenName.toLowerCase() + familyName.toLowerCase());
					String rawPassword = generatingRandomAlphanumericString();
					userFromDB.setHash(myPasswordEncoder.encode(rawPassword));

					Priorityband priorityband = prioritybandDao.findById(1).get(); // TODO hardcoded to 1st PriorityBand
					userFromDB.setPriorityband(priorityband);

					userFromDB.setGoogleId(googleUserId);
					String email = payload.getEmail();
					userFromDB.setEmail(email);
					userDao.save(userFromDB);
				}
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

				googleLoginResponse.user = userResp;
				googleLoginResponse.password = userFromDB.getHash();
				return googleLoginResponse;
			} else {
				System.out.println("Invalid ID token.");
				throw new DwaraException("Invalid ID token.", null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DwaraException("Invalid ID token.", null);
		}
	}

	public String generatingRandomAlphanumericString() {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
	
		String generatedString = random.ints(leftLimit, rightLimit + 1)
		  .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
		  .limit(targetStringLength)
		  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		  .toString();
	
		System.out.println(generatedString);
		return generatedString;
	}
	
}
