package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.TFilerenameKey;
import org.ishafoundation.dwaraapi.db.model.master.User;

@Entity(name = "TFilerename")
@Table(name="t_filerename")
public class TFilerename {

	@EmbeddedId
	private TFilerenameKey tFilerenameKey;
	
	@Column(name="new_filename")
	private String newFilename;
	
	@OneToOne(fetch = FetchType.LAZY)
	private User user;
	
	@Column(name="renamed_at")
	private LocalDateTime renamedAt;
	
	public String getNewFilename() {
		return newFilename;
	}

	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getRenamedAt() {
		return renamedAt;
	}

	public void setRenamedAt(LocalDateTime renamedAt) {
		this.renamedAt = renamedAt;
	}
}