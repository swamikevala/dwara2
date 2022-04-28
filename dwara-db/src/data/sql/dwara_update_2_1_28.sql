SET foreign_key_checks = 0;

/*** Artifact class for Audio Edited ***/

INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('audio-edit-grp', 'artifact', null, 1, 1, -1, 0, null),
('audio-edit-pub', 'artifact', 'AZ', 0, null, null, null, 'audio-edit-grp'),
('audio-edit-priv2', 'artifact', 'AZX', 0, null, null, null, 'audio-edit-grp'),
('audio-edit-tr-grp', 'artifact', null, 1, 1, -1, 0, null),
('audio-edit-tr-pub', 'artifact', 'AZT', 0, null, null, null, 'audio-edit-tr-grp'),
('audio-edit-tr-priv2', 'artifact', 'AZTX', 0, null, null, null, 'audio-edit-tr-grp');

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('audio-edit-pub', '', 'audio-edit-pub', 1, 1, 1, '/data/dwara/staged', null, 0, null),
('audio-edit-priv1', '', 'audio-edit-pub', 1, 1, 2, '/data/dwara/staged', null, 0, null),
('audio-edit-priv2', '', 'audio-edit-priv2', 1, 1, 3, '/data/dwara/staged', null, 0, null),
('audio-edit-tr-pub', '', 'audio-edit-tr-pub', 1, 1, 1, '/data/dwara/staged', null, 0, null),
('audio-edit-tr-priv1', '', 'audio-edit-tr-pub', 1, 1, 2, '/data/dwara/staged', null, 0, null),
('audio-edit-tr-priv2', '', 'audio-edit-tr-priv2', 1, 1, 3, '/data/dwara/staged', null, 0, null);

-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('audio-edit-pub', 'E1', 0, 1),
('audio-edit-pub', 'E2', 0, 1),
('audio-edit-pub', 'E3', 0, 1),
('audio-edit-priv1', 'E1', 0, 1),
('audio-edit-priv1', 'E2', 0, 1),
('audio-edit-priv1', 'E3', 0, 1),
('audio-edit-priv2', 'X1', 0, 1),
('audio-edit-priv2', 'X2', 0, 1),
('audio-edit-priv2', 'X3', 0, 1),
('audio-edit-tr-pub', 'E1', 0, 1),
('audio-edit-tr-pub', 'E2', 0, 1),
('audio-edit-tr-pub', 'E3', 0, 1),
('audio-edit-tr-priv1', 'E1', 0, 1),
('audio-edit-tr-priv1', 'E2', 0, 1),
('audio-edit-tr-priv1', 'E3', 0, 1),
('audio-edit-tr-priv2', 'X1', 0, 1),
('audio-edit-tr-priv2', 'X2', 0, 1),
('audio-edit-tr-priv2', 'X3', 0, 1);


-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'audio-edit-pub', 'archive-flow', 1),
('ingest', 'audio-edit-priv1', 'archive-flow', 1),
('ingest', 'audio-edit-priv2', 'archive-flow', 1),
('ingest', 'audio-edit-tr-pub', 'archive-flow', 1),
('ingest', 'audio-edit-tr-priv1', 'archive-flow', 1),
('ingest', 'audio-edit-tr-priv2', 'archive-flow', 1);


-- update artifactclass not through api
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48373';
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48375';
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48374';
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48380';
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48381';
UPDATE `artifact` SET `artifactclass_id`='video-pub' WHERE `id`='48382';

UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48376';
UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48377';
UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48378';
UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48384';
UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48396';
UPDATE `artifact` SET `artifactclass_id`='video-pub-proxy-low' WHERE `id`='48397';



SET foreign_key_checks = 1;

