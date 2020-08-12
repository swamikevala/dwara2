package org.ishafoundation.dwaraapi.api.resp.autoloader;

public class Drives
{
    private String id;

    private int address;

    private DriveStatus status;

    private boolean empty;

    private String barcode;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setAddress(int address){
        this.address = address;
    }
    public int getAddress(){
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
