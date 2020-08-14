package org.ishafoundation.dwaraapi.api.resp.volume;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class VolumeResponse {
	 private String id;
	 private String volumetype;
	 private String storagetype;
	 private String storagelevel;
	 private String volumeRef = null;
	 private String checksumtype;
	 private String formattedAt = null;
	 private boolean finalized;
	 private boolean imported;
	 private String archiveformat;
	 private float totalCapacity;
	 private float usedCapacity;
	 private float unusedCapacity;
	 private float maxPhysicalUnusedCapacity;
	 private String sizeUnit;
	 private String location;
	 Details DetailsObject;


	 // Getter Methods 

	 public String getId() {
	  return id;
	 }

	 public String getVolumetype() {
	  return volumetype;
	 }

	 public String getStoragetype() {
	  return storagetype;
	 }

	 public String getStoragelevel() {
	  return storagelevel;
	 }

	 public String getVolumeRef() {
	  return volumeRef;
	 }

	 public String getChecksumtype() {
	  return checksumtype;
	 }

	 public String getFormattedAt() {
	  return formattedAt;
	 }

	 public boolean getFinalized() {
	  return finalized;
	 }

	 public boolean getImported() {
	  return imported;
	 }

	 public String getArchiveformat() {
	  return archiveformat;
	 }

	 public float getTotalCapacity() {
	  return totalCapacity;
	 }

	 public float getUsedCapacity() {
	  return usedCapacity;
	 }

	 public float getUnusedCapacity() {
	  return unusedCapacity;
	 }

	 public float getMaxPhysicalUnusedCapacity() {
	  return maxPhysicalUnusedCapacity;
	 }

	 public String getSizeUnit() {
	  return sizeUnit;
	 }

	 public String getLocation() {
	  return location;
	 }

	 public Details getDetails() {
	  return DetailsObject;
	 }

	 // Setter Methods 

	 public void setId(String id) {
	  this.id = id;
	 }

	 public void setVolumetype(String volumetype) {
	  this.volumetype = volumetype;
	 }

	 public void setStoragetype(String storagetype) {
	  this.storagetype = storagetype;
	 }

	 public void setStoragelevel(String storagelevel) {
	  this.storagelevel = storagelevel;
	 }

	 public void setVolumeRef(String volumeRef) {
	  this.volumeRef = volumeRef;
	 }

	 public void setChecksumtype(String checksumtype) {
	  this.checksumtype = checksumtype;
	 }

	 public void setFormattedAt(String formattedAt) {
	  this.formattedAt = formattedAt;
	 }

	 public void setFinalized(boolean finalized) {
	  this.finalized = finalized;
	 }

	 public void setImported(boolean imported) {
	  this.imported = imported;
	 }

	 public void setArchiveformat(String archiveformat) {
	  this.archiveformat = archiveformat;
	 }

	 public void setTotalCapacity(float totalCapacity) {
	  this.totalCapacity = totalCapacity;
	 }

	 public void setUsedCapacity(float usedCapacity) {
	  this.usedCapacity = usedCapacity;
	 }

	 public void setUnusedCapacity(float unusedCapacity) {
	  this.unusedCapacity = unusedCapacity;
	 }

	 public void setMaxPhysicalUnusedCapacity(float maxPhysicalUnusedCapacity) {
	  this.maxPhysicalUnusedCapacity = maxPhysicalUnusedCapacity;
	 }

	 public void setSizeUnit(String sizeUnit) {
	  this.sizeUnit = sizeUnit;
	 }

	 public void setLocation(String location) {
	  this.location = location;
	 }

	 public void setDetails(Details detailsObject) {
	  this.DetailsObject = detailsObject;
	 }
	}
