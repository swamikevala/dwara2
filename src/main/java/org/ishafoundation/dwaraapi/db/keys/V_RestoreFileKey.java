package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.ishafoundation.dwaraapi.constants.Requesttype;


@Embeddable
public class V_RestoreFileKey implements Serializable {

	private static final long serialVersionUID = 766133869669209715L;

	@Column(name = "file_id")
	private int fileId;

	@Column(name = "tape_id")
	private int tapeId;

	@Column(name = "targetvolume_id")
	private int targetvolumeId;

	@Column(name = "requesttype_id")
	private Requesttype requesttype;

	@Column(name = "user_id")
	private int userId;

	public V_RestoreFileKey() {
	}

	public V_RestoreFileKey(int fileId, int tapeId, int targetvolumeId, Requesttype requesttype, int userId) {
		this.fileId = fileId;
		this.tapeId = tapeId;
		this.targetvolumeId = targetvolumeId;
		this.requesttype = requesttype;
		this.userId = userId;
	}



	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	
	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}

	public int getTargetvolumeId() {
		return targetvolumeId;
	}

	public void setTargetvolumeId(int targetvolumeId) {
		this.targetvolumeId = targetvolumeId;
	}

	public Requesttype getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(Requesttype requesttype) {
		this.requesttype = requesttype;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		V_RestoreFileKey that = (V_RestoreFileKey) o;
		return Objects.equals(fileId, that.fileId) && Objects.equals(tapeId, that.tapeId)
				&& Objects.equals(targetvolumeId, that.targetvolumeId) && Objects.equals(requesttype, that.requesttype)
				&& Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileId, tapeId, targetvolumeId, requesttype, userId);
	}
}