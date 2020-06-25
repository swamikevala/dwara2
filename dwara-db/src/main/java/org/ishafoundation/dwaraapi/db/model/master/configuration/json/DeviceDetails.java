package org.ishafoundation.dwaraapi.db.model.master.configuration.json;

public class DeviceDetails {
	
	//tape drive
	private String type; //	e.g. LTO
	private Integer generation;	
	private Integer[] readable_generations;		
	private Integer[] writeable_generations;	
	private String interface_;	
	private Integer autoloader_id; //	Which autoloader the drive is connected to
	private Integer autoloader_address; //	Which autoloader data transfer element the drive is associated with
	private Boolean standalone;//	Is the drive a standalone drive
	
	//tape autoloader
	private Integer slots; //	
	private Integer max_drives;	
	private Integer[] generations_supported;
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getGeneration() {
		return generation;
	}
	public void setGeneration(Integer generation) {
		this.generation = generation;
	}
	public Integer[] getReadable_generations() {
		return readable_generations;
	}
	public void setReadable_generations(Integer[] readable_generations) {
		this.readable_generations = readable_generations;
	}
	public Integer[] getWriteable_generations() {
		return writeable_generations;
	}
	public void setWriteable_generations(Integer[] writeable_generations) {
		this.writeable_generations = writeable_generations;
	}
	public String getInterface_() {
		return interface_;
	}
	public void setInterface_(String interface_) {
		this.interface_ = interface_;
	}
	public Integer getAutoloader_id() {
		return autoloader_id;
	}
	public void setAutoloader_id(Integer autoloader_id) {
		this.autoloader_id = autoloader_id;
	}
	public Integer getAutoloader_address() {
		return autoloader_address;
	}
	public void setAutoloader_address(Integer autoloader_address) {
		this.autoloader_address = autoloader_address;
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
	public Integer getMax_drives() {
		return max_drives;
	}
	public void setMax_drives(Integer max_drives) {
		this.max_drives = max_drives;
	}
	public Integer[] getGenerations_supported() {
		return generations_supported;
	}
	public void setGenerations_supported(Integer[] generations_supported) {
		this.generations_supported = generations_supported;
	}
}
