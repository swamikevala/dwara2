SET foreign_key_checks = 0;


-- new volume groups
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('BB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('BC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('PB', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '2');
INSERT INTO `volume` (`id`, `checksumtype`, `details`, `finalized`, `imported`, `storagelevel`, `storagetype`, `type`, `archiveformat_id`, `copy_id`) VALUES ('PC', 'sha256', '{\"blocksize\": 1048576, \"minimum_free_space\": 1099511627776}', 0, 1, 'block', 'tape', 'group', 'bru', '3');



-- Support for Audio ingest
-- Adding config for our very own Archives dept
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('audio-pub-proxy', 'artifact', 'AL', 0, null, null, null, null);
-- ('audio-priv2-proxy', 'artifact', 'ALX', 0, null, null, null, null);

-- ARTIFACTCLASS --
UPDATE `artifactclass` SET `concurrent_volume_copies`=1 WHERE `id`='audio-priv1';
UPDATE `artifactclass` SET `concurrent_volume_copies`=1 WHERE `id`='audio-priv2';
UPDATE `artifactclass` SET `concurrent_volume_copies`=1 WHERE `id`='audio-pub';

INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('audio-pub-proxy-low', 'audio proxy', 'audio-pub-proxy', 0, 1, '2', '/data/dwara/transcoded', 'audio-pub', 0, null),
('audio-priv1-proxy-low', 'audio priv1 proxy', 'audio-pub-proxy', 0, 1, '3', '/data/dwara/transcoded', 'audio-priv1', 0, null);
-- ('audio-priv2-proxy-low', 'audio priv2 proxy', 'audio-priv2-proxy', 0, 1, '4', '/data/dwara/transcoded', 'audio-priv2', 0, null);

-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('audio-pub-proxy-low', 'G1', 0, 1),
('audio-pub-proxy-low', 'G2', 0, 1),
('audio-priv1-proxy-low', 'G1', 0, 1),
('audio-priv1-proxy-low', 'G2', 0, 1);
-- ('audio-priv2-proxy-low', 'G1', 0, 1),
-- ('audio-priv2-proxy-low', 'G2', 0, 1);

-- DESTINATION --
INSERT INTO `destination` (`id`, `path`, `use_buffering`) VALUES
('catdv-audio-proxy', '172.18.1.24:/data/audio/ArchivesAudio', 0);

-- FLOW --
INSERT INTO `flow` ( `id`, `description`) VALUES
('audio-proxy-flow', 'audio proxy and copy');

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'audio-pub', 'audio-proxy-flow', 1),
('ingest', 'audio-priv1', 'audio-proxy-flow', 1),
('ingest', 'audio-priv2', 'audio-proxy-flow', 0),
('ingest', 'audio-priv3', 'audio-proxy-flow', 0);


INSERT INTO `flowelement` (`id`, `active`, `dependencies`, `deprecated`, `display_order`, `flow_id`, `flow_ref_id`, `processingtask_id`, `storagetask_action_id`, `task_config`) VALUES
('U31', 1, null, 0, 1, 'audio-proxy-flow', null, 'audio-proxy-low-gen', null, null),
('U32', 1, '["U31"]', 0, 2, 'audio-proxy-flow', null, 'file-copy', null, '{\"exclude_if\":{\"artifactclass_regex\":\".*-priv2.*\"}, \"destination_id\": \"catdv-audio-proxy\", \"pathname_regex\": \"[^/]+\\\\.mp3\"}'),
('U33', 1, '["U31"]', 0, 3, 'audio-proxy-flow', 'archive-flow', null, null, null);

-- EXTENSION --
INSERT INTO `extension` (`id`, `description`, `ignore`) VALUES
('ogg', '', null);

-- define new filetype for proxy
INSERT INTO `filetype` (`id`, `description`) VALUES
('audio-proxy', 'Audio proxy files');

-- add entry to extension_filetype
INSERT INTO `extension_filetype` (`sidecar`, `extension_id`, `filetype_id`) VALUES
(0, 'aac', 'audio'),
(0, 'ogg', 'audio'),
(0, 'mp3', 'audio-proxy');

INSERT INTO `processingtask` (`id`, `description`, `filetype_id`, `max_errors`, `output_artifactclass_suffix`, `output_filetype_id`) 
VALUES ('audio-proxy-low-gen', 'generate low resolution audio proxies', 'audio', '10', '-proxy-low', 'audio-proxy');

-- Support for Transcripts
-- Adding config for our very own Archives dept
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('transcript-grp', 'artifact', null, 1, 1, -1, 0, null),
('transcript-pub', 'artifact', 'S', 0, null, null, null, 'transcript-grp'),
('transcript-priv2', 'artifact', 'SX', 0, null, null, null, 'transcript-grp'),
('transcript-priv3', 'artifact', 'SXX', 0, null, null, null, 'transcript-grp');

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('transcript-pub', '', 'transcript-pub', 1, 1, 1, '/data/dwara/staged', null, 0, null),
('transcript-priv1', '', 'transcript-pub', 1, 1, 2, '/data/dwara/staged', null, 0, null),
('transcript-priv2', '', 'transcript-priv2', 1, 1, 3, '/data/dwara/staged', null, 0, null),
('transcript-priv3', '', 'transcript-priv3', 1, 1, 4, '/data/dwara/staged', null, 0, null);
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('transcript-pub', 'G1', 0, 1),
('transcript-pub', 'G2', 0, 1),
('transcript-priv1', 'X1', 0, 1),
('transcript-priv1', 'X2', 0, 1),
-- ('transcript-priv1', 'X3', 0, 1),
('transcript-priv2', 'X1', 0, 1),
('transcript-priv2', 'X2', 0, 1),
-- ('transcript-priv2', 'X3', 0, 1),
('transcript-priv3', 'X1', 0, 1),
('transcript-priv3', 'X2', 0, 1);
-- ('transcript-priv3', 'X3', 0, 1),

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'transcript-pub', 'archive-flow', 1),
('ingest', 'transcript-priv1', 'archive-flow', 1),
('ingest', 'transcript-priv2', 'archive-flow', 1),
('ingest', 'transcript-priv3', 'archive-flow', 1);

SET foreign_key_checks = 1;

