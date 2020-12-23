-- use `frm_prd`;
SET foreign_key_checks = 0; 

UPDATE `version` SET `version`='2.0.3' WHERE `version`='2.0.2';

-- transactional
UPDATE `request` SET `status`='completed_failures' WHERE status = 'partially_completed';

-- ensure job table flowelement constraint is dropped in prod and also datatype is String
-- update job table removing flowelement constraint and change to varchar;
ALTER TABLE `job` DROP FOREIGN KEY `FKegnlvrvev239boi0ta4nyi6ps`;
ALTER TABLE `job` DROP INDEX `FKegnlvrvev239boi0ta4nyi6ps` ;

ALTER TABLE `job` CHANGE COLUMN `flowelement_id` `flowelement_id` VARCHAR(255) NULL DEFAULT NULL ;

-- add new directory column in file* tables...
ALTER TABLE `file1` ADD COLUMN `directory` BIT(1) NULL DEFAULT NULL;
ALTER TABLE `file2` ADD COLUMN `directory` BIT(1) NULL DEFAULT NULL;
UPDATE `file1` SET `directory`=1 WHERE checksum is null;
UPDATE `file1` SET `directory`=0 WHERE checksum is not null;

-- Configuration
UPDATE `action` SET `id`='restore_process' WHERE `id`='finalize_process';

-- Extra digi related records
-- FILETYPE --
INSERT INTO filetype (id, `description`) VALUES ('video-digi-2020-mxf-v210', 'Digitized miniDV files, MXF uncompressed v210 original');
INSERT INTO filetype (id, `description`) VALUES ('video-digi-2020-mkv-ffv1', 'Digitized miniDV files, Matroska compressed ffv1 for preservation');
INSERT INTO filetype (id, `description`) VALUES ('mxf', 'MXF Video File');

-- EXTENSION --
-- INSERT INTO extension (id, `description`, `ignore`) VALUES ('mxf', '', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('mkv', 'Matroska', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('hdr', 'File header', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('ftr', 'File footer', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('idx', 'PFR index file', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('md5', 'MD5 checksum', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('qc', 'Digitization Quality Control file', 0);
INSERT INTO extension (id, `description`, `ignore`) VALUES ('log', 'Log file', 0);


-- EXTENSION_FILETYPE --
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('mkv', 'video', 0);

INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('mxf', 'video-digi-2020-mxf-v210', 0);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('md5', 'video-digi-2020-mxf-v210', 1);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('qc', 'video-digi-2020-mxf-v210', 1);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('log', 'video-digi-2020-mxf-v210', 1);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('hdr', 'video-digi-2020-mxf-v210', 1);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('ftr', 'video-digi-2020-mxf-v210', 1);

INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('mkv', 'video-digi-2020-mkv-ffv1', 0);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('hdr', 'video-digi-2020-mkv-ffv1', 1);
INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('idx', 'video-digi-2020-mkv-ffv1', 1);

INSERT INTO extension_filetype (extension_id, filetype_id, sidecar) VALUES ('mxf', 'mxf', 0);


-- SEQUENCE --
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-grp','artifact',null,null,null,1,1,-1,0,null,0,0);
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-pub','artifact','VD','^[A-Z]{1,2}\\d+',null,0,null,null,null,'video-digi-2020-grp',1,0);
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-priv2','artifact','VDX','^[A-Z]{1,2}\\d+',null,0,null,null,null,'video-digi-2020-grp',1,0);

INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-pub-proxy-low','artifact','VDL','^VD\\d+(?=_)','(?<=^VD)\\d+(?=_)',0,null,null,null,null,1,0);
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-priv2-proxy-low','artifact','VDXL','^VDX\\d+(?=_)','(?<=^VDX)\\d+(?=_)',0,null,null,null,null,1,0);

INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-1','volume','R',null,null,0,19801,19999,19800,null,0,0);
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-2','volume','R',null,null,0,29801,29999,29800,null,0,0);
INSERT INTO sequence (id, `type`, prefix, code_regex, number_regex, `group`, starting_number, ending_number, current_number, sequence_ref_id, force_match, keep_code) VALUES ('video-digi-2020-3','volume','R',null,null,0,39801,39999,39800,null,0,0);

ALTER TABLE `sequence` ADD COLUMN `replace_code` BIT(1) NULL DEFAULT NULL;
UPDATE `sequence` SET `replace_code`=0 WHERE type="artifact" AND `group`=0 AND id IN ("video-digi-2020-pub", "video-digi-2020-priv2");
UPDATE `sequence` SET `replace_code`=1 WHERE type="artifact" AND `group`=0 AND id NOT IN ("video-digi-2020-pub", "video-digi-2020-priv2");

-- ARTIFACTCLASS --

INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-pub','',1,'video-digi-2020-pub',1,1,28,'/data/dwara/staged',null,0);
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-priv1','',1,'video-digi-2020-pub',1,1,29,'/data/dwara/staged',null,0);
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-priv2','',1,'video-digi-2020-priv2',1,1,30,'/data/dwara/staged',null,0);
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-pub-proxy-low','',1,'video-digi-2020-pub-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-pub',0);
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-priv1-proxy-low','',1,'video-digi-2020-pub-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-priv1',0);
INSERT INTO artifactclass (id, `description`, domain_id, sequence_id, source, concurrent_volume_copies, display_order, path_prefix, artifactclass_ref_id, import_only) VALUES ('video-digi-2020-priv2-proxy-low','',1,'video-digi-2020-priv2-proxy-low',0,1,0,'/data/dwara/transcoded','video-digi-2020-priv2',0);


-- ARTIFACTCLASS_TASK

drop table artifactclass_processingtask;

--
-- Table structure for table `artifactclass_task`
--
DROP TABLE IF EXISTS `artifactclass_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artifactclass_task` (
  `id` int(11) NOT NULL,
  `config` json DEFAULT NULL,
  `processingtask_id` varchar(255) DEFAULT NULL,
  `storagetask_action_id` varchar(255) DEFAULT NULL,
  `artifactclass_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7fufhtcsqji1l21eapxfn73g5` (`artifactclass_id`),
  CONSTRAINT `FK7fufhtcsqji1l21eapxfn73g5` FOREIGN KEY (`artifactclass_id`) REFERENCES `artifactclass` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (1, 'video-edit-pub', NULL, 'video-proxy-low-gen', '{"pathname_regex": ".*/Outputs?/[^/]*\\\\.mov$"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (2, 'video-digi-2020-pub', 'write', NULL, '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (3, 'video-digi-2020-pub', NULL, 'video-proxy-low-gen', '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (4, 'video-digi-2020-pub', NULL, 'file-delete', '{"pathname_regex": ".*\\\\.mxf$"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (5, 'video-digi-2020-priv1', 'write', NULL, '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (6, 'video-digi-2020-priv1', NULL, 'video-proxy-low-gen', '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (7, 'video-digi-2020-priv1', NULL, 'file-delete', '{"pathname_regex": ".*\\\\.mxf$"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (8, 'video-digi-2020-priv2', 'write', NULL, '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (9, 'video-digi-2020-priv2', NULL, 'video-proxy-low-gen', '{"create_held_jobs": true}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (10, 'video-digi-2020-priv2', NULL, 'file-delete', '{"pathname_regex": ".*\\\\.mxf$"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (11, 'video-digi-2020-pub', NULL, 'video-digi-2020-preservation-gen', '{"output_path": "/"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (12, 'video-digi-2020-priv1', NULL, 'video-digi-2020-preservation-gen', '{"output_path": "/"}');
INSERT INTO artifactclass_task (id, artifactclass_id, storagetask_action_id, processingtask_id, config) VALUES (13, 'video-digi-2020-priv2', NULL, 'video-digi-2020-preservation-gen', '{"output_path": "/"}');

-- ARTIFACTCLASS_VOLUME --
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-pub', 'R198', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv1', 'R198', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv2', 'X1', 0, 1);

INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-pub', 'R298', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv1', 'R298', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv2', 'X2', 0, 1);

INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-pub', 'R398', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv1', 'R398', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv2', 'X3', 0, 1);

INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-pub-proxy-low', 'G1', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv1-proxy-low', 'G1', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv2-proxy-low', 'X1', 0, 1);

INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-pub-proxy-low', 'G2', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv1-proxy-low', 'G2', 0, 1);
INSERT INTO artifactclass_volume (artifactclass_id, volume_id, encrypted, active) VALUES ('video-digi-2020-priv2-proxy-low', 'X2', 0, 1);


-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO action_artifactclass_user (action_id, artifactclass_id, user_id) VALUES ('ingest', 'video-digi-2020-pub', 2);
INSERT INTO action_artifactclass_user (action_id, artifactclass_id, user_id) VALUES ('ingest', 'video-digi-2020-priv1', 2);
INSERT INTO action_artifactclass_user (action_id, artifactclass_id, user_id) VALUES ('ingest', 'video-digi-2020-priv2', 2);

-- PROCESSINGTASK --
truncate processingtask;

ALTER TABLE `processingtask` ADD COLUMN `output_filetype_id` VARCHAR(255) NULL DEFAULT NULL;

INSERT into processingtask (id, `description`, max_errors, filetype_id, output_filetype_id, output_artifactclass_suffix) VALUES ('video-proxy-low-gen', 'generate low resolution video proxies (with thumbnail and metadata xml)', 10, 'video', 'video-proxy', '-proxy-low');
INSERT into processingtask (id, `description`, max_errors, filetype_id, output_filetype_id, output_artifactclass_suffix) VALUES ('video-mam-update', 'move proxy files to mam server and add xml metadata to mam', 0, 'video-proxy', null, null);
INSERT into processingtask (id, `description`, max_errors, filetype_id, output_filetype_id, output_artifactclass_suffix) VALUES ('video-digi-2020-header-extract', 'extracts header and footer from uncompressed mxf', 0, 'video-digi-2020-mxf-v210', 'video-digi-2020-mxf-v210', '');
INSERT into processingtask (id, `description`, max_errors, filetype_id, output_filetype_id, output_artifactclass_suffix) VALUES ('video-digi-2020-preservation-gen', 'generates lossless mkv/ffv1 video preservation format inc PFR header and index files', 0, 'video-digi-2020-mxf-v210', 'video-digi-2020-mkv-ffv1', '');


-- FLOW --
INSERT INTO flow (id,`description`) VALUES ('video-digi-2020-flow','video ingest flow for dv digitization 2020 project');
INSERT INTO flow (id,`description`) VALUES ('video-digi-2020-archive-flow','modified archive flow for dv digitization 2020 project');

-- FLOWELEMENT --
--
-- Table structure for table `flowelement`
--
DROP TABLE IF EXISTS `flowelement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flowelement` (
  `id` varchar(255) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `dependencies` json DEFAULT NULL,
  `deprecated` bit(1) DEFAULT NULL,
  `display_order` int(11) DEFAULT NULL,
  `flow_id` varchar(255) DEFAULT NULL,
  `flow_ref_id` varchar(255) DEFAULT NULL,
  `processingtask_id` varchar(255) DEFAULT NULL,
  `storagetask_action_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('4', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('5', 'video-proxy-flow', null, 'video-mam-update', null, '[4]', 5, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('6', 'video-proxy-flow', null, null, 'archive-flow', '[4]', 6, 1, 1);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U1', 'video-proxy-flow', null, 'video-proxy-low-gen', null, null, 4, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U2', 'video-proxy-flow', null, 'video-mam-update', null, '["U1"]', 5, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U3', 'video-proxy-flow', null, null, 'archive-flow', '["U1"]', 6, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U4', 'video-digi-2020-flow', null, 'video-digi-2020-header-extract', null, null, 1, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U5', 'video-digi-2020-flow', null, 'video-digi-2020-preservation-gen', null, null, 2, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U6', 'video-digi-2020-flow', null, 'checksum-gen', null, '["U4","U5"]', 3, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U7', 'video-digi-2020-flow', null, 'file-delete', null, '["U6"]', 4, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U8', 'video-digi-2020-flow', null, null, 'video-digi-2020-archive-flow', '["U7"]', 5, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U9', 'video-digi-2020-flow', null, null, 'video-proxy-flow', '["U7"]', 6, 1, 0);
-- Need a custom video-digi-2020-archive-flow, since the core archive-flow starts with checksum-gen task which we do not need at this step
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U10', 'video-digi-2020-archive-flow', 'write', null, null, null, 1, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U11', 'video-digi-2020-archive-flow', 'restore', null, null, '["U10"]', 2, 1, 0);
INSERT INTO flowelement (id, flow_id, storagetask_action_id, processingtask_id, flow_ref_id, dependencies, display_order, active, deprecated) VALUES ('U12', 'video-digi-2020-archive-flow', null, 'checksum-verify', null, '["U11"]', 3, 1, 0);


-- ACTION_ARTIFACTCLASS_FLOW --
--
-- Table structure for table `action_artifactclass_flow`
--

DROP TABLE IF EXISTS `action_artifactclass_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action_artifactclass_flow` (
  `artifactclass_id` varchar(255) NOT NULL,
  `flow_id` varchar(255) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `action_id` varchar(255) NOT NULL,
  PRIMARY KEY (`action_id`,`artifactclass_id`,`flow_id`),
  CONSTRAINT `FKidvy2kx9wbuxydhl4ggyd8wjj` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-pub', 'archive-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-pub', 'video-proxy-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-digi-2020-pub', 'video-digi-2020-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-digi-2020-priv1', 'video-digi-2020-flow', 1);
INSERT INTO action_artifactclass_flow (action_id, artifactclass_id, flow_id, active) VALUES ('ingest', 'video-digi-2020-priv2', 'video-digi-2020-flow', 1);


-- VOLUME --
INSERT INTO volume (id,capacity,checksumtype,details,finalized,imported,storagelevel,storagesubtype,storagetype,`type`,archiveformat_id,copy_id,location_id,suspect,defective,group_ref_id,sequence_id,initialized_at) VALUES ('R198',null,'sha256','{"blocksize": 262144}',0,0,'block',null,'tape','group','tar',1,null,0,0,null,'video-digi-2020-1',null);
INSERT INTO volume (id,capacity,checksumtype,details,finalized,imported,storagelevel,storagesubtype,storagetype,`type`,archiveformat_id,copy_id,location_id,suspect,defective,group_ref_id,sequence_id,initialized_at) VALUES ('R298',null,'sha256','{"blocksize": 262144}',0,0,'block',null,'tape','group','bru',2,null,0,0,null,'video-digi-2020-2',null);
INSERT INTO volume (id,capacity,checksumtype,details,finalized,imported,storagelevel,storagesubtype,storagetype,`type`,archiveformat_id,copy_id,location_id,suspect,defective,group_ref_id,sequence_id,initialized_at) VALUES ('R398',null,'sha256','{"blocksize": 262144}',0,0,'block',null,'tape','group','tar',3,null,0,0,null,'video-digi-2020-3',null);


update device set status='offline', wwn_id=null, details='{"type": "LTO-7", "standalone": false, "autoloader_id": "xl80"}' where id='lto7-2';
insert into device (id, defective, details, manufacturer, model, serial_number, status, type, warranty_expiry_date, wwn_id) values ('lto7-5', 0, '{"type": "LTO-7", "standalone": false, "autoloader_id": "xl80", "autoloader_address":2}', 'IBM', 'Ultrium HH7', 'YX10WT093970', 'online', 'tape_drive', '2023-07-12', '/dev/tape/by-id/scsi-35000e111c5aa70c9-nst');

SET foreign_key_checks = 1; 
