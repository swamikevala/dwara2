package org.ishafoundation.dwaraapi.db.model.master.configuration.json;

public class DeviceDetails {
	
	//tape drive
	private String type; //	e.g. LTO
	private int generation;	
	private int[] readable_volumetype_ids;		
	private int[] writeable_volumetype_ids;	
	private String interface_;	
	private int autoloader_id; //	Which autoloader the drive is connected to
	private int autoloader_address; //	Which autoloader data transfer element the drive is associated with
	private boolean standalone;//	Is the drive a standalone drive
	
	//tape autoloader
	private int slots; //	
	private int max_drives;	
	private int[] generations_supported;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}
	public int[] getReadable_volumetype_ids() {
		return readable_volumetype_ids;
	}
	public void setReadable_volumetype_ids(int[] readable_volumetype_ids) {
		this.readable_volumetype_ids = readable_volumetype_ids;
	}
	public int[] getWriteable_volumetype_ids() {
		return writeable_volumetype_ids;
	}
	public void setWriteable_volumetype_ids(int[] writeable_volumetype_ids) {
		this.writeable_volumetype_ids = writeable_volumetype_ids;
	}
	public String getInterface_() {
		return interface_;
	}
	public void setInterface_(String interface_) {
		this.interface_ = interface_;
	}
	public int getAutoloader_id() {
		return autoloader_id;
	}
	public void setAutoloader_id(int autoloader_id) {
		this.autoloader_id = autoloader_id;
	}
	public int getAutoloader_address() {
		return autoloader_address;
	}
	public void setAutoloader_address(int autoloader_address) {
		this.autoloader_address = autoloader_address;
	}
	public boolean isStandalone() {
		return standalone;
	}
	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}
	public int getSlots() {
		return slots;
	}
	public void setSlots(int slots) {
		this.slots = slots;
	}
	public int getMax_drives() {
		return max_drives;
	}
	public void setMax_drives(int max_drives) {
		this.max_drives = max_drives;
	}
	public int[] getGenerations_supported() {
		return generations_supported;
	}
	public void setGenerations_supported(int[] generations_supported) {
		this.generations_supported = generations_supported;
	}
}
