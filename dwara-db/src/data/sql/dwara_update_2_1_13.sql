SET foreign_key_checks = 0; 

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

