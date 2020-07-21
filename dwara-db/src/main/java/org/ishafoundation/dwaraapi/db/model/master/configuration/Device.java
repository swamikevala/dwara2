package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.cache.Cacheable;
import org.ishafoundation.dwaraapi.db.model.master.configuration.json.DeviceDetails;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;


@Entity
@Table(name="device")
public class Device implements Cacheable{
	
	@Override
	public String getName() {
		// Just to make the class cacheable
		return uid;
	}
	
	@Id
	@Column(name="id")
	private int id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="devicetype")
	private Devicetype devicetype;
	
	@Column(name="uid", unique = true)
	private String uid;

	@Column(name="serial_number", unique = true)
	private String serialNumber;
	
	@Column(name="warranty_expiry_date")
	private LocalDateTime warrantyExpiryDate;

	@Column(name="manufacturer")
	private String manufacturer;
		
	@Column(name="model")
	private String model;
	
	@Lob
	@Column(name="details")
	private DeviceDetails details;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Devicetype getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Devicetype devicetype) {
		this.devicetype = devicetype;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public LocalDateTime getWarrantyExpiryDate() {
		return warrantyExpiryDate;
	}

	public void setWarrantyExpiryDate(LocalDateTime warrantyExpiryDate) {
		this.warrantyExpiryDate = warrantyExpiryDate;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public DeviceDetails getDetails() {
		return details;
	}

	public void setDetails(DeviceDetails details) {
		this.details = details;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device obj = (Device) o;
        return Objects.equals(uid, obj.uid);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }	
	
}