package org.ishafoundation.dwaraapi.api.resp.login;

import java.util.List;

public class User extends org.ishafoundation.dwaraapi.db.model.master.configuration.User {

	private List<String> role;

	public List<String> getRole() {
		return role;
	}

	public void setRole(List<String> role) {
		this.role = role;
	}
}
