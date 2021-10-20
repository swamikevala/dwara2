SET foreign_key_checks = 0;

-- resetting the minimum_free_space to 2TB rather than 10
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E2';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='E3';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R198';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R2';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R298';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R3';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}' WHERE `id`='R398';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 1099511627776}' WHERE `id`='XX1';
UPDATE `volume` SET `details`='{\"blocksize\": 262144, \"remove_after_job\": true, \"minimum_free_space\": 1099511627776}' WHERE `id`='XX2';

-- IT Infra modifications
-- new tape pool for IT Infra - so new volume sequence
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('it-infra-1', '10000', '19999', 0, 0, 0, 'I', '10001', 'volume');

-- new tape pool for IT Infra
INSERT INTO `volume` (`id`, `checksumtype`, `defective`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `suspect`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('I1', 'sha256', 0, '{\"blocksize\": 262144, \"minimum_free_space\": 2199023255552}', 0, 0, 'block', 'tape', 0, 'group', 'tar', '1', 'it-infra-1');

-- ARTIFACTCLASS_VOLUME (just 1 copy and not 3 as planned before) --
UPDATE `artifactclass_volume` SET `volume_id`='I1' WHERE `artifactclass_id`='dept-it-infra' and`volume_id`='E1';
DELETE FROM `artifactclass_volume` WHERE `artifactclass_id`='dept-it-infra' and`volume_id`='E2';
DELETE FROM `artifactclass_volume` WHERE `artifactclass_id`='dept-it-infra' and`volume_id`='E3';


-- Defective tape rewrite
UPDATE `request` SET `status`='marked_failed', `completed_at`='2021-10-20 15:47:27.353000' WHERE `id`='75665';
UPDATE `artifact1_volume` SET `status`='current' WHERE `volume_id`='G10002L7';