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

import org.ishafoundation.dwaraapi.db.model.master.configuration.json.DeviceDetails;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;


@Entity
@Table(name="device")
public class Device {
	
	@Id
	@Column(name="id")
	private String id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="devicetype")
	private Devicetype devicetype;
	
	@Column(name="wwn_id", unique = true)
	private String wwnId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private DeviceStatus status;
	
	@Column(name="defective")
	private boolean defective;

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


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Devicetype getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Devicetype devicetype) {
		this.devicetype = devicetype;
	}

	public String getWwnId() {
		return wwnId;
	}

	public void setWwnId(String wwnId) {
		this.wwnId = wwnId;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public boolean isDefective() {
		return defective;
	}

	public void setDefective(boolean defective) {
		this.defective = defective;
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
        return Objects.equals(wwnId, obj.wwnId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(wwnId);
    }	
	
}