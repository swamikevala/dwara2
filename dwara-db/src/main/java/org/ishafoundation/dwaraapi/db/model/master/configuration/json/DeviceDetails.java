package org.ishafoundation.dwaraapi.db.model.master.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDetails {
	
	//tape drive
	private String type; //	e.g. LTO
	
	@JsonProperty("interface")
	private String interface_;	
	
	@JsonProperty("autoloader_id")
	private String autoloaderId; //	Which autoloader the drive is connected to
	
	@JsonProperty("autoloader_address")
	private Integer autoloaderAddress; //	Which autoloader data transfer element the drive is associated with
	
	private Boolean standalone;//	Is the drive a standalone drive
	
	//tape autoloader
	private Integer slots; //	
	
	@JsonProperty("max_drives")
	private Integer maxDrives;
	
	@JsonProperty("generations_supported")
	private Integer[] generationsSupported;
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInterface_() {
		return interface_;
	}
	public void setInterface_(String interface_) {
		this.interface_ = interface_;
	}
	public String getAutoloaderId() {
		return autoloaderId;
	}
	public void setAutoloaderId(String autoloaderId) {
		this.autoloaderId = autoloaderId;
	}
	public Integer getAutoloaderAddress() {
		return autoloaderAddress;
	}
	public void setAutoloaderAddress(Integer autoloaderAddress) {
		this.autoloaderAddress = autoloaderAddress;
	}
	public Boolean getStandalone() {
		return standalone;
	}
	public void setStandalone(Boolean standalone) {
		this.standalone = standalone;
	}
	public Integer getSlots() {
		return slots;
	}
	public void setSlots(Integer slots) {
		this.slots = slots;
	}
	public Integer getMaxDrives() {
		return maxDrives;
	}
	public void setMaxDrives(Integer maxDrives) {
		this.maxDrives = maxDrives;
	}
	public Integer[] getGenerationsSupported() {
		return generationsSupported;
	}
	public void setGenerationsSupported(Integer[] generationsSupported) {
		this.generationsSupported = generationsSupported;
	}
}
