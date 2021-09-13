-- Adding config for Isha Programs Archive

INSERT INTO `sequence` (`id`, `type`, `prefix`, `code_regex`, `number_regex`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`, `force_match`, `keep_code`, `replace_code`) VALUES 
('dept-programs', 'artifact', 'BP', null, null, 0, 1, -1, 0, null, 0, 0, 0);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `domain_id`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-programs', 'Isha Programs Archive backup', 1, 'dept-programs', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-programs', 'E1', 0, 1),
('dept-programs', 'E2', 0, 1),
('dept-programs', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-programs', 1),
('ingest', 'dept-programs', 2),
('ingest', 'dept-programs', 3),
('ingest', 'dept-programs', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-programs', 'archive-flow', 1);
