package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TapedrivetypeTapetypeKey implements Serializable {
 
	private static final long serialVersionUID = -1530123038755024502L;

	@Column(name = "tapedrivetype_id")
    private int tapedrivetypeId;
 
    @Column(name = "tapetype_id")
    private int tapetypeId;
 
    public TapedrivetypeTapetypeKey() {}
    
    public TapedrivetypeTapetypeKey(
        int tapedrivetypeId,
        int tapetypeId) {
        this.tapedrivetypeId = tapedrivetypeId;
        this.tapetypeId = tapetypeId;
    }
 
	public int getTapedrivetypeId() {
		return tapedrivetypeId;
	}

	public void setTapedrivetypeId(int tapedrivetypeId) {
		this.tapedrivetypeId = tapedrivetypeId;
	}

	public int getTapetypeId() {
		return tapetypeId;
	}

	public void setTapetypeId(int tapetypeId) {
		this.tapetypeId = tapetypeId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TapedrivetypeTapetypeKey that = (TapedrivetypeTapetypeKey) o;
        return Objects.equals(tapedrivetypeId, that.tapedrivetypeId) &&
               Objects.equals(tapetypeId, that.tapetypeId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(tapedrivetypeId, tapetypeId);
    }
}