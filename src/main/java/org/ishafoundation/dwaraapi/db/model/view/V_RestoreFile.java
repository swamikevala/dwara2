package org.ishafoundation.dwaraapi.db.model.view;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.constants.Requesttype;


/*
From schema google spreadsheet
select * from file_tape 

inner join tape on file_tape.tape_id = tape.id 
inner join tapeset on tape.tapeset_id = tapeset.id
inner join file on file.id = file_tape.file_id
inner join library on file.library_id = library.id
inner join libraryclass_requesttype_user on
    libraryclass_requesttype_user.libraryclass_id = library.libraryclass_id "

select * has lot of id conflicts hence just choosing the fields needed upfront...

select 

`dwara_v2_2`.`file`.`id` AS `file_id`,
`dwara_v2_2`.`file`.`pathname` AS `file_pathname`,
`dwara_v2_2`.`file`.`size` AS `file_size`,
`dwara_v2_2`.`library`.`libraryclass_id` AS `libraryclass_id`,
`dwara_v2_2`.`tape`.`barcode` AS `barcode`,
`dwara_v2_2`.`libraryclass_requesttype_user`.`requesttype_id` AS `requesttype_id`,
`dwara_v2_2`.`tapeset`.`copy_number` AS `copy_number`,
`dwara_v2_2`.`libraryclass_requesttype_user`.`user_id` AS `user_id` 

from 

(((((`dwara_v2_2`.`file_tape` join `dwara_v2_2`.`tape` on((`dwara_v2_2`.`file_tape`.`tape_id` = `dwara_v2_2`.`tape`.`id`))) 
join `dwara_v2_2`.`tapeset` on((`dwara_v2_2`.`tape`.`tapeset_id` = `dwara_v2_2`.`tapeset`.`id`))) 
join `dwara_v2_2`.`file` on((`dwara_v2_2`.`file`.`id` = `dwara_v2_2`.`file_tape`.`file_id`))) 
join `dwara_v2_2`.`library` on((`dwara_v2_2`.`file`.`library_id` = `dwara_v2_2`.`library`.`id`))) 
join `dwara_v2_2`.`libraryclass_requesttype_user` on((`dwara_v2_2`.`libraryclass_requesttype_user`.`libraryclass_id` = `dwara_v2_2`.`library`.`libraryclass_id`)))
 */

@Entity
@Table(name="v_restore_file")
public class V_RestoreFile {

	@Id
	@Column(name="file_id")
	private int fileId;
	
	@Column(name="file_pathname")
	private String filePathname;

	@Column(name="file_size")
	private double fileSize;
	
	@Column(name="libraryclass_id")
	private int libraryclassId;

	@Column(name="barcode")
	private String barcode;
	
	@Column(name="requesttype_id")
	private Requesttype requesttype;
	
	@Column(name="copy_number")
	private int copyNumber;
	
	@Column(name="user_id")
	private int userId;
	
	

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getFilePathname() {
		return filePathname;
	}

	public void setFilePathname(String filePathname) {
		this.filePathname = filePathname;
	}

	public double getFileSize() {
		return fileSize;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	public int getLibraryclassId() {
		return libraryclassId;
	}

	public void setLibraryclassId(int libraryclassId) {
		this.libraryclassId = libraryclassId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public Requesttype getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(Requesttype requesttype) {
		this.requesttype = requesttype;
	}

	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}