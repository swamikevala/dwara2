package org.ishafoundation.dwaraapi.api.resp.autoloader;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ToLoadTape
{
    private String location;
    
    private String barcode;
    
    private String autoloader;

    private int priority;
    
    private boolean finalized;
    

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getAutoloader() {
		return autoloader;
	}

	public void setAutoloader(String autoloader) {
		this.autoloader = autoloader;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
	
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToLoadTape toLoadTape = (ToLoadTape) o;
        return Objects.equals(barcode, toLoadTape.barcode);
    }
}
