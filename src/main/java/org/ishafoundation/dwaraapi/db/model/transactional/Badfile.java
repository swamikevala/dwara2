package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="badfile")
public class Badfile {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="badfile_id")
	private int badfileId;
	
	@Column(name="file_id")
	private int fileId;

	@Column(name="reason")
	private String reason;

		
	public int getBadfileId() {
		return badfileId;
	}

	public void setBadfileId(int badfileId) {
		this.badfileId = badfileId;
	}
	
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}