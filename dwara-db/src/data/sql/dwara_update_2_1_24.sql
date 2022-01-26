SET foreign_key_checks = 0;

-- wrong volume barcode used
UPDATE `file_volume_diff` SET `volume_id`='C17027L7' WHERE `volume_id`='C17027L6';
UPDATE `file_volume` SET `volume_id`='C17027L7' WHERE `volume_id`='C17027L6';
UPDATE `artifact_volume` SET `volume_id`='C17027L7' WHERE `volume_id`='C17027L6';
UPDATE `import` SET `volume_id`='C17027L7' WHERE `volume_id`='C17027L6';
UPDATE `request` SET `details`='{\"body\": {\"xmlPathname\": \"/data/dwara/import-staging/todo/C17027L7.xml\"}}' WHERE `id` IN (SELECT `request_id` FROM `import` where `volume_id`='C17027L7');
UPDATE `volume` SET `id`='C17027L7', `capacity`='6000000000000', `storagesubtype`='LTO-7', `finalized`=0, `first_written_at`='2020-05-06 00:00:00.000000' WHERE `id`='C17027L6';
UPDATE `volume` SET `finalized`=0, `first_written_at`='2017-12-31 00:00:00.000000' WHERE `id`='C16139L6';
UPDATE `volume` SET `finalized`=0, `first_written_at`='2020-05-16 00:00:00.000000' WHERE `id`='C17031L7';
UPDATE `volume` SET `finalized`=0, `first_written_at`='2010-12-29 00:00:00.000000' WHERE `id`='CA4220L4';
UPDATE `volume` SET `finalized`=0, `first_written_at`='2020-05-14 00:00:00.000000' WHERE `id`='P17023L6';

-- new volume groups
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('B1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('B2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('BA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('PA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '1');

UPDATE `sequence` SET `sequence_ref_id`='audio-grp' WHERE `id`='audio-priv3';

-- Adding config for our very own Archives dept
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('dept-archives', 'artifact', 'BA', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-archives', 'Archives team backup', 'dept-archives', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-archives', 'E1', 0, 1),
('dept-archives', 'E2', 0, 1),
('dept-archives', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-archives', 1),
('ingest', 'dept-archives', 2),
('ingest', 'dept-archives', 3),
('ingest', 'dept-archives', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-archives', 'archive-flow', 1);



-- Adding config for our very own Archives dept and Isha samskriti
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('dept-samskriti', 'artifact', 'BS', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-samskriti', 'Isha samskriti dept backup', 'dept-samskriti', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-samskriti', 'E1', 0, 1),
('dept-samskriti', 'E2', 0, 1),
('dept-samskriti', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-samskriti', 1),
('ingest', 'dept-samskriti', 2),
('ingest', 'dept-samskriti', 3),
('ingest', 'dept-samskriti', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-samskriti', 'archive-flow', 1);

SET foreign_key_checks = 1;

