package org.ishafoundation.dwaraapi.db.model.master.configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.transactional.Request;

@Entity
@Table(name="tag")
public class Tag {
    @Id
	@Column(name="tag")
	String tag;

	@ManyToMany
	@JoinTable(
		name = "request_tag",
		joinColumns = @JoinColumn(name = "tag"),
		inverseJoinColumns = @JoinColumn(name = "request_id")
	)
	Set<Request> requests;

	public Tag(){

	}

	public Tag(String value) {
		tag = value;
	}

	public Request getRequestById(int requestId) {
		if(requests != null) {
			for (Request request : requests) {
				if(request.getId() == requestId)
					return request;
			}
		}
		return null;
	}

	public Request addRequest(Request r) {
		if(requests == null)
			requests = new LinkedHashSet<Request>();
		requests.add(r);
		return r;
	}

	public void deleteRequest(Request r) {
		if(r != null) {
			requests.remove(r);
		}
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Set<Request> getRequests() {
		return requests;
	}

	public void setRequests(Set<Request> requests) {
		this.requests = requests;
	}
}
