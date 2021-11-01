-- SET foreign_key_checks = 0;

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
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C3', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', null);
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', null);

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
DELETE FROM `dwara`.`t_activedevice` WHERE `id`='231200';
update job set status = 'queued' where id = 492808;
*/