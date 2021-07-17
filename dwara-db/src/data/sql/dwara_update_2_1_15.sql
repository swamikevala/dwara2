SET foreign_key_checks = 0;

-- Remove G3 volume group

DELETE FROM `volume` WHERE `id` = 'G3';
DELETE FROM `sequence` WHERE `id` = 'generated-3';

-- Change Private 3 prefix from 'Y' to 'XX'

UPDATE `volume` SET `id` = 'XX1' WHERE `id` = 'Y1';
UPDATE `volume` SET `id` = 'XX2' WHERE `id` = 'Y2';

-- Volumes
UPDATE `sequence` SET `prefix` = 'XX' WHERE `id` in ('priv3-1', 'priv3-2');
-- Artifacts
UPDATE `sequence` SET `prefix` = 'VXX' WHERE `id` = 'video-priv3';
UPDATE `sequence` SET `prefix` = 'AXX' WHERE `id` = 'audio-priv3';

UPDATE `artifactclass_volume` SET `volume_id` = 'XX1' WHERE `volume_id` = 'Y1';
UPDATE `artifactclass_volume` SET `volume_id` = 'XX2' WHERE `volume_id` = 'Y2';

-- Generate proxies for private2 videos
UPDATE `action_artifactclass_flow` SET `active` = 1 WHERE `artifactclass_id` = 'video-priv2' AND `flow_id` = 'video-proxy-flow';
UPDATE `action_artifactclass_flow` SET `active` = 1 WHERE `artifactclass_id` = 'video-edit-tr-priv2' AND `flow_id` = 'video-edit-tr-proxy-flow';
UPDATE `action_artifactclass_flow` SET `active` = 1 WHERE `artifactclass_id` = 'video-edit-priv2' AND `flow_id` = 'video-edit-proxy-flow';

UPDATE `flowelement` SET `task_config` = '{"exclude_if": {"artifactclass_regex": ".*-priv2.*"}}' WHERE `artifactclass_id` = 'video-mam-update';

SET foreign_key_checks = 1;



