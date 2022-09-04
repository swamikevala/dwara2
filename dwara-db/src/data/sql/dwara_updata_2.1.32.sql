-- ********* Adding config for Edited Audio *********
-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('photo-priv1', 'photo priv 1', 'photo-pub', 1, 1, 1, '/data/dwara/staged', null, 0, null),
('photo-priv1-proxy', 'photo priv 1 proxy', 'photo-pub-proxy', 0, 1, 0, '/data/dwara/transcoded', 'photo-priv1', 0, '{"pathname_regex": "(?!)"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('photo-priv1', 'R1', 0, 1),
('photo-priv1', 'R2', 0, 1),
('photo-priv1', 'R3', 0, 1),
('photo-priv1-proxy', 'G1', 0, 1),
('photo-priv1-proxy', 'G2', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'photo-priv1', 1),
('ingest', 'photo-priv1', 2),
('ingest', 'photo-priv1', 3),
('ingest', 'photo-priv1', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'photo-priv1', 'archive-flow', 1),
('ingest', 'photo-priv1', 'photo-proxy-flow', 1);