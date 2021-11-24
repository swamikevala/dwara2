SET foreign_key_checks = 0;

/**
 * XL80 Drives replaced 
 */

/*
Ran on 22nd Nov - 14 hrs

UPDATE `device` SET `defective`=1, `status`='replaced', `retired_date`='2021-11-22 12:00:00.000000', `wwn_id`=null WHERE `id`='lto7-1';
UPDATE `device` SET `defective`=1, `status`='replaced', `retired_date`='2021-11-22 12:00:00.000000', `wwn_id`=null WHERE `id`='lto7-4';
INSERT INTO `device` (`id`, `defective`, `details`, `manufacturer`, `model`, `serial_number`, `status`, `type`, `warranty_expiry_date`, `wwn_id`, `employed_date`) VALUES ('lto7-8', 0, '{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 1}', 'IBM', 'Ultrium HH7', '91WT802864', 'online', 'tape_drive', '2022-04-15 00:00:00.000000', '/dev/tape/by-id/scsi-35000e111c5aa70bf-nst', '2021-11-22 13:00:00.000000');
INSERT INTO `device` (`id`, `defective`, `details`, `manufacturer`, `model`, `serial_number`, `status`, `type`, `warranty_expiry_date`, `wwn_id`, `employed_date`) VALUES ('lto7-9', 0, '{\"type\": \"LTO-7\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 3}', 'IBM', 'Ultrium HH7', '90WT805086', 'online', 'tape_drive', '2023-07-12 00:00:00.000000', '/dev/tape/by-id/scsi-35000e111c5aa70d3-nst', '2021-11-22 13:00:00.000000');


UPDATE `device` SET `serial_number`='10WT083802' WHERE `id`='lto6-1';
UPDATE `device` SET `serial_number`='10WT134623' WHERE `id`='lto7-2';
UPDATE `device` SET `serial_number`='10WT096005' WHERE `id`='lto7-3';
UPDATE `device` SET `serial_number`='10WT103628' WHERE `id`='lto7-6';
UPDATE `device` SET `serial_number`='10WT093970' WHERE `id`='lto7-5';
UPDATE `device` SET `serial_number`='10WT124812' WHERE `id`='lto7-7';
UPDATE `device` SET `serial_number`='10WT103623' WHERE `id`='lto7-1';
UPDATE `device` SET `serial_number`='1097011322' WHERE `id`='lto7-4';

*/
/*
 * Added LTO6 drive in library slot position 5 
UPDATE `device` SET `wwn_id`=null WHERE `id`='lto7-6';
UPDATE `device` SET `details`='{\"type\": \"LTO-6\", \"standalone\": false, \"autoloader_id\": \"xl80\", \"autoloader_address\": 4}', `wwn_id`='/dev/tape/by-id/scsi-35000e111c5aa70dd-nst' WHERE `id`='lto6-1';
UPDATE `device` SET `wwn_id`='/dev/tape/by-id/scsi-35000e111c5aa70e7-nst' WHERE `id`='lto7-6';
*/
/*
 * Missed out updating on 31st Oct ALTER TABLE `t_file_volume` CHANGE COLUMN `volume_block` `volume_start_block` INT(11) NULL DEFAULT NULL ;
 * 
 * so we did this on 21st Nov, 1 PM
 * update t_file_volume set volume_start_block = volume_block where volume_block is not null and volume_start_block is null;
 * ALTER TABLE `t_file_volume` DROP COLUMN `volume_block`; 
 */


ALTER TABLE `artifactclass` DROP COLUMN `domain_id`;

ALTER TABLE `artifact1` RENAME TO  `artifact` ;
ALTER TABLE `file1` RENAME TO  `file` ;
ALTER TABLE `artifact1_volume` RENAME TO  `artifact_volume` ;
ALTER TABLE `file1_volume` RENAME TO  `file_volume` ;

ALTER TABLE `artifact1_label` CHANGE COLUMN `artifact1_id` `artifact_id` INT(11) NOT NULL , RENAME TO  `artifact_label` ;

DROP TABLE `artifact2`;
DROP TABLE `file2`;
DROP TABLE `artifact2_volume`;
DROP TABLE `file2_volume`;  

SET foreign_key_checks = 1;
-- import appends
/* Already executed on 31st Oct
ALTER TABLE `file1_volume` CHANGE COLUMN `volume_block` `volume_start_block` INT(11) NULL DEFAULT NULL ;
ALTER TABLE `file2_volume` CHANGE COLUMN `volume_block` `volume_start_block` INT(11) NULL DEFAULT NULL ;
*/
-- Artifact sequences
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `starting_number`, `type`) VALUES ('video-imported-grp', '0', '-1', 0, 1, 0, '1', 'artifact');

INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `starting_number`, `type`) VALUES ('video-digi-2010-grp', '0', '-1', 0, 1, 0, '1', 'artifact');
INSERT INTO `sequence` (`id`, `code_regex`, `force_match`, `group`, `keep_code`, `prefix`, `type`, `sequence_ref_id`, `replace_code`) VALUES ('video-digi-2010-pub', '^[0-9A-Za-z-]+', 1, 0, 0, 'VDSN', 'artifact', 'video-digi-2010-grp', 0); -- VDSN - Video digi Swami Nir-vichara
INSERT INTO `sequence` (`id`, `code_regex`, `force_match`, `group`, `keep_code`, `prefix`, `type`, `sequence_ref_id`, `replace_code`) VALUES ('video-digi-2010-priv2', '^[0-9A-Za-z-]+', 1, 0, 0, 'VDXSN', 'artifact', 'video-digi-2010-grp', 0); -- VDSN - Video digi Swami Nir-vichara


INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('video-digi-2010-pub', 'Digi attempt 1 - SN', 1, 'video-digi-2010-pub', 1, 1, 9, '/data/dwara/staged', null, 1, null);
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('video-digi-2010-priv1', 'Digi attempt 1 - SN', 1, 'video-digi-2010-pub', 1, 1, 9, '/data/dwara/staged', null, 1, null);
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('video-digi-2010-priv2', 'Digi attempt 1 - SN', 1, 'video-digi-2010-priv2', 1, 1, 9, '/data/dwara/staged', null, 1, null);

-- volume sequences
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-1', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-2', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-3', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-priv-1', '-1', '-1', 0, 0, 0, 'P', '-1', 'volume');
-- INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-priv-2', '-1', '-1', 0, 0, 0, 'P', '-1', 'volume');

-- volume group
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C3', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2', null);

-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-1');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-2');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C3', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', 'imported-3');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-1');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-2');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', 'imported-3');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-priv-1');
-- INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-priv-2');

/* Already executed on 31st Oct
-- BM9 cancelled - rogue artifact - so marked the failed jobs as completed so next of queued jobs will get picked up... 
UPDATE `job` SET `status`='completed' WHERE `id` in ('492037','492038','492040');
UPDATE `request` SET `status`='completed' WHERE `id`='77612';


-- ROLLOUT DAY appends
-- during rollout this processing job had lot of files queued for processing - delaying the app shutdown - so had to put it on hold so ProcessingJobManager skips the queued files
update job set status = 'on_hold' where id = 494329;

-- after rollout we put back the job to queued state
update job set status = 'queued' where id = 494329;

-- When Rewrite jobs were queued which generally in 1000+ viewing tape UI caused all the jobs to be loaded for storage job meta wrapping pulling the DB and hence system down - so had to put the jobs on_hold and once fixed will set the jobs back to queued  
update request set status='queued' where action_id='rewrite' and type ='system' and status='on_hold';
update job set status='queued' where status='on_hold' and storagetask_action_id='restore';

-- LIVE Incident - Rewrite jobs were queued but requests were on_hold thus causing the tapeView to pull the system down again - had to hard stop the app when the requeued write job 492808 in_progress
-- So had to run the following queries later...
DELETE FROM `t_activedevice` WHERE `id`='231200';
update job set status = 'queued' where id = 492808;
*/