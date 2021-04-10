package org.ishafoundation.dwaraapi.api.resp.autoloader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Drive
{
    private String id;

    private Integer address;

    private DriveStatus status;

    private boolean empty;

    private String barcode;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setAddress(Integer address){
        this.address = address;
    }
    public Integer getAddress(){
        return this.address;
    }
    public DriveStatus getStatus() {
		return status;
	}
	public void setStatus(DriveStatus status) {
		this.status = status;
	}
	public void setEmpty(boolean empty){
        this.empty = empty;
    }
    public boolean getEmpty(){
        return this.empty;
    }
    public void setBarcode(String barcode){
        this.barcode = barcode;
    }
    public String getBarcode(){
        return this.barcode;
    }
}
