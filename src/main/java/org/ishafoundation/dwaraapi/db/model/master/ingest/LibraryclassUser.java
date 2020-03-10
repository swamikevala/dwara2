package org.ishafoundation.dwaraapi.db.model.master.ingest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="libraryclass_user")
public class LibraryclassUser {
	@Id
	@Column(name="libraryclass_user_id")
	private int libraryclassUserId;
	
	@Column(name="user_id")
	private int userId;
	
	@Column(name="libraryclass_id")
	private int libraryclassId;	

	@Column(name="display_order")
	private int displayOrder;
	

	public int getLibraryclassUserId() {
		return libraryclassUserId;
	}

	public void setLibraryclassUserId(int libraryclassUserId) {
		this.libraryclassUserId = libraryclassUserId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
}
