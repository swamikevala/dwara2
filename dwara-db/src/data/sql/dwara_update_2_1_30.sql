SET foreign_key_checks = 0;

-- Add support for 360 insv files --
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'insv', 'video');

/*** Artifact class for Audio Edited ***/
-- SEQUENCE --
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('audio-edit-pub-proxy', 'artifact', 'AZL', 0, null, null, null, 'audio-edit-grp'),
('audio-edit-priv2-proxy', 'artifact', 'AZXL', 0, null, null, null, 'audio-edit-grp');

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('audio-edit-pub-proxy-low', '', 'audio-edit-pub-proxy', 1, 1, 1, '/data/dwara/staged', null, 0, null),
('audio-edit-priv1-proxy-low', '', 'audio-edit-pub-proxy', 1, 1, 2, '/data/dwara/staged', null, 0, null),
('audio-edit-priv2-proxy-low', '', 'audio-edit-priv2-proxy', 1, 1, 3, '/data/dwara/staged', null, 0, null);

-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('audio-edit-pub-proxy-low', 'G1', 0, 1),
('audio-edit-pub-proxy-low', 'G2', 0, 1),
('audio-edit-priv1-proxy-low', 'G1', 0, 1),
('audio-edit-priv1-proxy-low', 'G2', 0, 1),
('audio-edit-priv2-proxy-low', 'X1', 0, 1),
('audio-edit-priv2-proxy-low', 'X2', 0, 1),
('audio-edit-priv2-proxy-low', 'X3', 0, 1);


-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'audio-edit-pub-proxy-low', 'archive-flow', 1),
('ingest', 'audio-edit-priv1-proxy-low', 'archive-flow', 1),
('ingest', 'audio-edit-priv2-proxy-low', 'archive-flow', 1);

INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'video-priv2-proxy-low', 'X3');
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'video-edit-priv2-proxy-low', 'X3');

SET foreign_key_checks = 1;

-- DU-998
update job set status='marked_failed',message='Both G1/2 copies has the proxy for this artifact corrupted' where id in (79164, 79319, 79230);
update request set status='marked_failed',message='Both G1/2 copies has the proxy for this artifact corrupted' where id in (13523,13541, 13527);