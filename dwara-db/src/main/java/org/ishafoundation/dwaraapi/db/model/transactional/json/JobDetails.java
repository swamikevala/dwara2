package org.ishafoundation.dwaraapi.db.model.transactional.json;

import com.fasterxml.jackson.databind.JsonNode;

public class JobDetails {
	private JsonNode body;
	private Integer volume_id;
	private Integer device_id;
	
	public JsonNode getBody() {
		return body;
	}
	public void setBody(JsonNode body) {
		this.body = body;
	}
	public Integer getVolume_id() {
		return volume_id;
	}
	public void setVolume_id(Integer volume_id) {
		this.volume_id = volume_id;
	}
	public Integer getDevice_id() {
		return device_id;
	}
	public void setDevice_id(Integer device_id) {
		this.device_id = device_id;
	}
	
	// TODO : equals and hashCode
}
