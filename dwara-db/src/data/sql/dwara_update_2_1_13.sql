SET foreign_key_checks = 0; 

-- HDV support

INSERT INTO `filetype` (`id`, `description`) VALUES ('video-digi-2020', 'both mxf and mov');

INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'ftr', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'hdr', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'log', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'md5', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'mxf', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (1, 'qc', 'video-digi-2020');
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'mov', 'video-digi-2020');


UPDATE `flowelement` SET `task_config`='{\"pathname_regex\": \"(mxf|mov)/[^/]+\\\\.(mxf|mov)\"}' WHERE `id`='U7';


-- reverted the below change
UPDATE `flowelement` SET `active`=0, `deprecated`=1 WHERE `id`='U400';
UPDATE `flowelement` SET `dependencies`='[\"U4\", \"U5\"]' WHERE `id`='U6';
UPDATE `flowelement` SET `task_config`=NULL WHERE `id`='U4';


-- new metadata extraction task for hdv mov files  
INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) VALUES ('video-digi-2020-mov-meta-extract', 'extracts header and footer from uncompressed mxf', 'video-digi-2020-mxf-v210', '1', '', 'video-digi-2020-mxf-v210');

-- insert the above processing task to the digi flow
INSERT INTO `flowelement` (`id`, `active`, `deprecated`, `display_order`, `flow_id`, `processingtask_id`) VALUES ('U400', 1, 0, '1', 'video-digi-2020-flow', 'video-digi-2020-mov-meta-extract');

-- ensure the above task need to be included on condition that tape digitised is hdv
-- if hdv include video-digi-2020-mov-meta-extract(for mov only)
UPDATE `flowelement` SET `task_config`='{\"include_if\": {\"tag\": \"guru:shambho\"}}' WHERE `id`='U400';
-- if hdv exclude video-digi-2020-header-extract job(for mxf only)
UPDATE `flowelement` SET `task_config`='{\"exclude_if\": {\"tag\": \"guru:shambho\"}}' WHERE `id`='U4';

-- checksumgen prerequisite corrected
UPDATE `flowelement` SET `dependencies`='[\"U4\",\"U400\", \"U5\"]' WHERE `id`='U6';

-- exclude video-mam-update from video-digi-2020-proxy-flow for priv2
UPDATE `flowelement` SET `task_config`='{\"exclude_if\":{\"artifactclass_regex\":\".*-priv2.*\"}}' WHERE `id`='U22';

-- ******** -- ******** -- ******** -- ******** -- ********
 
-- prev_sequence_code missed out getting updated because of sequence.coderegex for video-digi-2020-edit* 
UPDATE `artifact1` SET `prev_sequence_code`=replace(name, CONCAT(sequence_code, '_')) WHERE name REGEXP '_Z-DVCAM' and artifactclass_id not like '%-proxy-low';

-- ******** -- ******** -- ******** -- ******** -- ********
-- EMEDIA And

-- SEQUENCE --
-- *************** NOTE *************** No grouping of sequence needed for dept artifact classes ***************
INSERT INTO `sequence` (`id`, `type`, `prefix`, `code_regex`, `number_regex`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`, `force_match`, `keep_code`, `replace_code`) VALUES 
('dept-emedia', 'artifact', 'BE', null, null, 0, 1, -1, 0, null, 0, 0, 0),
('dept-impressions', 'artifact', 'BM', null, null, 0, 1, -1, 0, null, 0, 0, 0);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-emedia', 'Emedia backup', 1, 'dept-emedia', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}'),
('dept-impressions', 'Impressions backup', 1, 'dept-impressions', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-emedia', 'E1', 0, 1),
('dept-emedia', 'E2', 0, 1),
('dept-emedia', 'E3', 0, 1),
('dept-impressions', 'E1', 0, 1),
('dept-impressions', 'E2', 0, 1),
('dept-impressions', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-emedia', 1),
('ingest', 'dept-emedia', 2),
('ingest', 'dept-emedia', 3),
('ingest', 'dept-emedia', 6),
('ingest', 'dept-impressions', 1),
('ingest', 'dept-impressions', 2),
('ingest', 'dept-impressions', 3),
('ingest', 'dept-impressions', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-emedia', 'archive-flow', 1),
('ingest', 'dept-impressions', 'archive-flow', 1);

SET foreign_key_checks = 1;

