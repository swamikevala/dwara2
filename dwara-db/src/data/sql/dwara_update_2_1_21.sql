-- SET foreign_key_checks = 0;

-- import appends
-- Artifact sequences
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `starting_number`, `type`) VALUES ('video-imported-grp', '0', '-1', 0, 1, 0, '1', 'artifact');
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `starting_number`, `type`) VALUES ('video-digi-2012-grp', '0', '-1', 0, 1, 0, '1', 'artifact');
INSERT INTO `sequence` (`id`, `code_regex`, `force_match`, `group`, `keep_code`, `prefix`, `type`, `sequence_ref_id`, `replace_code`) VALUES ('video-digi-2012-pub', '^[0-9A-Za-z-]+', 1, 0, 0, 'VDSN', 'artifact', 'video-digi-2012-grp', 0); -- VDSN - Video digi Swami Nir-vichara

-- volume sequences
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-1', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-2', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-3', '-1', '-1', 0, 0, 0, 'C', '-1', 'volume');
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-priv-1', '-1', '-1', 0, 0, 0, 'P', '-1', 'volume');
INSERT INTO `sequence` (`id`, `current_number`, `ending_number`, `force_match`, `group`, `keep_code`, `prefix`, `starting_number`, `type`) VALUES ('imported-priv-2', '-1', '-1', 0, 0, 0, 'P', '-1', 'volume');

-- volume group
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-1');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('C3', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', 'imported-3');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CA', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-1');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('CC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '3', 'imported-3');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P1', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '1', 'imported-priv-1');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`, `sequence_id`) VALUES ('P2', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 0, 'block', 'tape', 'group', 'bru', '2', 'imported-priv-2');


