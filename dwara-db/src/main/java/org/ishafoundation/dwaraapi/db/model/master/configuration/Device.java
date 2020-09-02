package org.ishafoundation.dwaraapi.db.model.master.configuration;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ishafoundation.dwaraapi.db.model.master.configuration.json.DeviceDetails;
import org.ishafoundation.dwaraapi.enumreferences.DeviceStatus;
import org.ishafoundation.dwaraapi.enumreferences.Devicetype;

import com.vladmihalcea.hibernate.type.json.JsonStringType;


@Entity
@Table(name="device")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class Device{// implements Cacheable{
	
	@Id
	@Column(name="id")
	private String id;
	
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private Devicetype type;
	
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
	
	@Type(type = "json")
	@Column(name="details", columnDefinition = "json")
	private DeviceDetails details;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Devicetype getType() {
		return type;
	}

	public void setType(Devicetype devicetype) {
		this.type = devicetype;
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