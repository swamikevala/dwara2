package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TapedrivetypeVolumetypeKey implements Serializable {
 
	private static final long serialVersionUID = -1530123038755024502L;

	@Column(name = "tapedrivetype_id")
    private int tapedrivetypeId;
 
    @Column(name = "volumetype_id")
    private int volumetypeId;
 
    public TapedrivetypeVolumetypeKey() {}
    
    public TapedrivetypeVolumetypeKey(
        int tapedrivetypeId,
        int volumetypeId) {
        this.tapedrivetypeId = tapedrivetypeId;
        this.volumetypeId = volumetypeId;
    }
 
	public int getTapedrivetypeId() {
		return tapedrivetypeId;
	}

	public void setTapedrivetypeId(int tapedrivetypeId) {
		this.tapedrivetypeId = tapedrivetypeId;
	}

	public int getVolumetypeId() {
		return volumetypeId;
	}

	public void setVolumetypeId(int volumetypeId) {
		this.volumetypeId = volumetypeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TapedrivetypeVolumetypeKey that = (TapedrivetypeVolumetypeKey) o;
        return Objects.equals(tapedrivetypeId, that.tapedrivetypeId) &&
               Objects.equals(volumetypeId, that.volumetypeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(tapedrivetypeId, volumetypeId);
    }
}