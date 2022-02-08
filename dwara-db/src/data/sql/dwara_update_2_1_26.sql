SET foreign_key_checks = 0;

INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES (0, 'mp4', 'audio');

UPDATE `action_artifactclass_flow` SET `active`=1 WHERE `action_id`='ingest' and`artifactclass_id`='audio-priv2' and`flow_id`='audio-proxy-flow';

INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('audio-priv2-proxy', 'artifact', 'AXL', 0, null, null, null, null);

INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('audio-priv2-proxy-low', 'audio priv2 proxy', 'audio-priv2-proxy', 0, 1, '4', '/data/dwara/transcoded', 'audio-priv2', 0, null);

-- ARTIFACTCLASS_VOLUME --
UPDATE `artifactclass_volume` SET `volume_id`='G1' WHERE `artifactclass_id`='transcript-priv1' and`volume_id`='X1';
UPDATE `artifactclass_volume` SET `volume_id`='G2' WHERE `artifactclass_id`='transcript-priv1' and`volume_id`='X2';
UPDATE `artifactclass_volume` SET `volume_id`='XX1' WHERE `artifactclass_id`='transcript-priv3' and`volume_id`='X1';
UPDATE `artifactclass_volume` SET `volume_id`='XX2' WHERE `artifactclass_id`='transcript-priv3' and`volume_id`='X2';
INSERT INTO `artifactclass_volume` (`active`, `encrypted`, `artifactclass_id`, `volume_id`) VALUES (1, 0, 'transcript-priv2', 'X3');

INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('audio-priv2-proxy-low', 'G1', 0, 1),
('audio-priv2-proxy-low', 'G2', 0, 1);

-- priv2 proxy copy not to be suppressed
UPDATE `flowelement` SET `task_config`='{\"destination_id\": \"catdv-audio-proxy\", \"pathname_regex\": \"[^/]+\\\\.mp3\"}' WHERE `id`='U32';

UPDATE `volume` SET `copy_id`='1' WHERE `id`='XX1';
UPDATE `volume` SET `copy_id`='2' WHERE `id`='XX2';

-- Sequence reset - Import and Ingest had sequence conflict - Fixed now but had to hard reset the sequence 
UPDATE `sequence` SET `current_number`='32658' WHERE `id`='video-grp';

SET foreign_key_checks = 1;

