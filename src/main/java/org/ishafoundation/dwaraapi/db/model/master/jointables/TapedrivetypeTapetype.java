package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.TapedrivetypeTapetypeKey;
import org.ishafoundation.dwaraapi.db.model.master.Tapetype;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tapedrivetype;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "TapedrivetypeTapetype")
@Table(name="tapedrivetype_tapetype")
public class TapedrivetypeTapetype {
	@EmbeddedId
	private TapedrivetypeTapetypeKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tapedrivetypeId")
	private Tapedrivetype tapedrivetype;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tapetypeId")
	private Tapetype tapetype;

	@Column(name = "can_read")
	private boolean canRead; 

	@Column(name = "can_write")
	private boolean canWrite; 

	public TapedrivetypeTapetype() {
		
	}

	public TapedrivetypeTapetype(Tapedrivetype tapedrivetype, Tapetype tapetype) {
		this.tapedrivetype = tapedrivetype;
		this.tapetype = tapetype;
		this.id = new TapedrivetypeTapetypeKey(tapedrivetype.getId(), tapetype.getId());
	}
	
	@JsonIgnore
	public TapedrivetypeTapetypeKey getId() {
		return id;
	}
	
	@JsonIgnore
	public void setId(TapedrivetypeTapetypeKey id) {
		this.id = id;
	}

	public Tapetype getTapetype() {
		return tapetype;
	}

	public void setTapetype(Tapetype tapetype) {
		this.tapetype = tapetype;
	}

	public Tapedrivetype getTapedrivetype() {
		return tapedrivetype;
	}

	public void setTapedrivetype(Tapedrivetype tapedrivetype) {
		this.tapedrivetype = tapedrivetype;
	}
	
	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TapedrivetypeTapetype that = (TapedrivetypeTapetype) o;
        return Objects.equals(tapedrivetype, that.tapedrivetype) &&
               Objects.equals(tapetype, that.tapetype);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(tapedrivetype, tapetype);
    }

}