package org.ishafoundation.dwaraapi.db.model.view;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.V_RestoreFileKey;


/*
From schema google spreadsheet
select * from file_tape 

inner join tape on file_tape.tape_id = tape.id 
inner join volumeset on tape.volumeset_id = volumeset.id
inner join file on file.id = file_tape.file_id
inner join library on file.library_id = library.id
inner join artifactclass_action_user on
    artifactclass_action_user.artifactclass_id = library.artifactclass_id "

select * has lot of id conflicts hence just choosing the fields needed upfront...

	SELECT 
        `library`.`artifactclass_id` AS `library_artifactclass_id`,
        `file`.`id` AS `file_id`,
        `file`.`pathname` AS `file_pathname`,
        `file`.`size` AS `file_size`,
        `file`.`crc` AS `file_crc`,
        `tape`.`barcode` AS `tape_barcode`,
        `tape`.`blocksize` AS `tape_blocksize`,
        `tape`.`finalized` AS `tape_finalized`,
        `volumeset`.`copy_number` AS `volumeset_copy_number`,
        `volumeset`.`storageformat_id` AS `volumeset_storageformat_id`,
        `file_tape`.`block` AS `file_tape_block`,
        `file_tape`.`offset` AS `file_tape_offset`,
        `file_tape`.`encrypted` AS `file_tape_encrypted`,
        `file_tape`.`deleted` AS `file_tape_deleted`,
        `artifactclass_targetvolume`.`targetvolume_id` AS `targetvolume_id`,
        `artifactclass_action_user`.`action_id` AS `action_id`,
        `artifactclass_action_user`.`user_id` AS `user_id`
    FROM
        ((((((`file_tape`
        JOIN `tape` ON ((`file_tape`.`tape_id` = `tape`.`id`)))
        JOIN `volumeset` ON ((`tape`.`volumeset_id` = `volumeset`.`id`)))
        JOIN `file` ON ((`file`.`id` = `file_tape`.`file_id`)))
        JOIN `library` ON ((`file`.`library_id` = `library`.`id`)))
        JOIN `artifactclass_targetvolume` ON ((`artifactclass_targetvolume`.`artifactclass_id` = `library`.`artifactclass_id`)))
        JOIN `artifactclass_action_user` ON ((`artifactclass_action_user`.`artifactclass_id` = `library`.`artifactclass_id`)))
 */

@Entity
@Table(name="v_restore_file")
public class V_RestoreFile {

	@EmbeddedId
	private V_RestoreFileKey id;
	

	@Column(name="file_pathname")
	private String filePathname;

	@Column(name="file_size")
	private double fileSize;

	@Column(name="file_crc")
	private String fileCrc;
	
	@Column(name="artifactclass_name")
	private String artifactclassName;
	
	@Column(name="tape_barcode")
	private String tapeBarcode;
	
	@Column(name="tape_blocksize")
	private int tapeBlocksize;
	
	@Column(name="tape_finalized")
	private boolean tapeFinalized;

	@Column(name="volumeset_copy_number")
	private int volumesetCopyNumber;

	@Column(name="storageformat_name")
	private String storageformatName;

	@Column(name="file_tape_block")
	private int fileTapeBlock;

	@Column(name="file_tape_offset")
	private int fileTapeOffset;
	
	@Column(name="file_tape_encrypted")
	private boolean fileTapeEncrypted;

	@Column(name="file_tape_deleted")
	private boolean fileTapeDeleted;

	
	public V_RestoreFileKey getId() {
		return id;
	}

	public void setId(V_RestoreFileKey id) {
		this.id = id;
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

	public String getFileCrc() {
		return fileCrc;
	}

	public void setFileCrc(String fileCrc) {
		this.fileCrc = fileCrc;
	}

	public String getArtifactclassName() {
		return artifactclassName;
	}

	public void setArtifactclassName(String artifactclassName) {
		this.artifactclassName = artifactclassName;
	}

	public String getTapeBarcode() {
		return tapeBarcode;
	}

	public void setTapeBarcode(String tapeBarcode) {
		this.tapeBarcode = tapeBarcode;
	}

	public int getTapeBlocksize() {
		return tapeBlocksize;
	}

	public void setTapeBlocksize(int tapeBlocksize) {
		this.tapeBlocksize = tapeBlocksize;
	}

	public boolean isTapeFinalized() {
		return tapeFinalized;
	}

	public void setTapeFinalized(boolean tapeFinalized) {
		this.tapeFinalized = tapeFinalized;
	}

	public int getVolumesetCopyNumber() {
		return volumesetCopyNumber;
	}

	public void setVolumesetCopyNumber(int volumesetCopyNumber) {
		this.volumesetCopyNumber = volumesetCopyNumber;
	}

	public String getStorageformatName() {
		return storageformatName;
	}

	public void setStorageformatName(String storageformatName) {
		this.storageformatName = storageformatName;
	}
	
	public int getFileTapeBlock() {
		return fileTapeBlock;
	}

	public void setFileTapeBlock(int fileTapeBlock) {
		this.fileTapeBlock = fileTapeBlock;
	}

	public int getFileTapeOffset() {
		return fileTapeOffset;
	}

	public void setFileTapeOffset(int fileTapeOffset) {
		this.fileTapeOffset = fileTapeOffset;
	}

	public boolean isFileTapeEncrypted() {
		return fileTapeEncrypted;
	}

	public void setFileTapeEncrypted(boolean fileTapeEncrypted) {
		this.fileTapeEncrypted = fileTapeEncrypted;
	}

	public boolean isFileTapeDeleted() {
		return fileTapeDeleted;
	}

	public void setFileTapeDeleted(boolean fileTapeDeleted) {
		this.fileTapeDeleted = fileTapeDeleted;
	}

//	
//	@Column(name = "artifactclass_id")
//	private int artifactclassId;
//	
//	@Column(name = "library_id")
//	private int libraryId;
//	
//	
//	@Column(name = "storageformat_id")
//	private int storageformatId;
//	
//	@Column(name = "volumeset_id")
//	private int volumesetId;
//
//	public int getArtifactclassId() {
//		return artifactclassId;
//	}
//
//	public void setArtifactclassId(int artifactclassId) {
//		this.artifactclassId = artifactclassId;
//	}
//
//	public int getLibraryId() {
//		return libraryId;
//	}
//
//	public void setLibraryId(int libraryId) {
//		this.libraryId = libraryId;
//	}
//
//	
//	public int getStorageformatId() {
//		return storageformatId;
//	}
//
//	public void setStorageformatId(int storageformatId) {
//		this.storageformatId = storageformatId;
//	}
//
//	public int getVolumesetId() {
//		return volumesetId;
//	}
//
//	public void setVolumesetId(int volumesetId) {
//		this.volumesetId = volumesetId;
//	}

}