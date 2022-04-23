SET foreign_key_checks = 0;

-- clean up
-- cancel failed restores 
update request SET `status`='cancelled' WHERE `id` in (select Id from (select id from request where action_id in ('restore','restore_process') and status in ('failed','completed_failures')) as t);

-- cancel ingests
UPDATE `request` SET `status`='cancelled', `message`='We ingested the wrong file instead of the folder to be ingested' WHERE `id`='87105';
UPDATE `request` SET `status`='cancelled' WHERE `id`='87106';
UPDATE `request` SET `status`='cancelled', `message`='We ingested the wrong file instead of the folder to be ingested' WHERE `id`='87107';
UPDATE `request` SET `status`='cancelled' WHERE `id`='87276';

-- ********* deactivate audio-proxy-flow for audio* artifactclasses and add Audio-proxy as a separate source ingestable artifactclass *********
UPDATE `sequence` SET `sequence_ref_id`='audio-grp' WHERE `id` in ('audio-pub-proxy','audio-priv2-proxy');

UPDATE `action_artifactclass_flow` SET `active`=0 WHERE `action_id`='ingest' and`artifactclass_id`in ('audio-pub','audio-priv1','audio-priv2') and`flow_id`='audio-proxy-flow';

-- audio-*-proxy as source
UPDATE  `artifactclass` SET `source`=1, `artifactclass_ref_id`=null WHERE `id` in ('audio-pub-proxy-low','audio-priv1-proxy-low','audio-priv2-proxy-low');

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'audio-pub-proxy-low', 1),
('ingest', 'audio-pub-proxy-low', 2),
('ingest', 'audio-pub-proxy-low', 3),
('ingest', 'audio-pub-proxy-low', 6),
('ingest', 'audio-priv1-proxy-low', 1),
('ingest', 'audio-priv1-proxy-low', 2),
('ingest', 'audio-priv1-proxy-low', 3),
('ingest', 'audio-priv1-proxy-low', 6),
('ingest', 'audio-priv2-proxy-low', 1),
('ingest', 'audio-priv2-proxy-low', 2),
('ingest', 'audio-priv2-proxy-low', 3),
('ingest', 'audio-priv2-proxy-low', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'audio-pub-proxy-low', 'archive-flow', 1),
('ingest', 'audio-priv1-proxy-low', 'archive-flow', 1),
('ingest', 'audio-priv2-proxy-low', 'archive-flow', 1);


-- ********* Adding config for HYS *********
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('dept-hys', 'artifact', 'BH', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-hys', 'Hatha Yoga School backup', 'dept-hys', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-hys', 'E1', 0, 1),
('dept-hys', 'E2', 0, 1),
('dept-hys', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-hys', 1),
('ingest', 'dept-hys', 2),
('ingest', 'dept-hys', 3),
('ingest', 'dept-hys', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-hys', 'archive-flow', 1);

-- ********* Adding config for GLP *********
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('dept-glp', 'artifact', 'BG', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-glp', 'GLP backup', 'dept-glp', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-glp', 'E1', 0, 1),
('dept-glp', 'E2', 0, 1),
('dept-glp', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-glp', 1),
('ingest', 'dept-glp', 2),
('ingest', 'dept-glp', 3),
('ingest', 'dept-glp', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-glp', 'archive-flow', 1);

-- ********* Adding config for Edited Audio *********
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('dept-glp', 'artifact', 'BG', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `description`, `sequence_id`, `source`, `concurrent_volume_copies`, `display_order`, `path_prefix`, `artifactclass_ref_id`, `import_only`, `config`) VALUES 
('dept-glp', 'GLP backup', 'dept-glp', 1, 1, 1, '/data/dwara/staged', null, 0, '{\"pathname_regex\": \"(?!)\"}');
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('dept-glp', 'E1', 0, 1),
('dept-glp', 'E2', 0, 1),
('dept-glp', 'E3', 0, 1);

-- ACTION_ARTIFACTCLASS_USER --
INSERT INTO `action_artifactclass_user` (`action_id`, `artifactclass_id`, `user_id`) VALUES 
('ingest', 'dept-glp', 1),
('ingest', 'dept-glp', 2),
('ingest', 'dept-glp', 3),
('ingest', 'dept-glp', 6);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'dept-glp', 'archive-flow', 1);


-- L36 need to be reverted from deletion

UPDATE `artifact_volume` SET `status`='current' WHERE `artifact_id`='5409';

UPDATE `t_file` SET `deleted`=0 WHERE `id` in (select id from (select id from t_file where artifact_id = 5409 and pathname not like "%.mxf") as t);

UPDATE `file` SET `deleted`=0 WHERE `id` in (select id from (select id from file where artifact_id = 5409 and pathname not like "%.mxf") as t);

UPDATE `artifact` SET `deleted`=0 WHERE `id`='5409';

/*

n

UPDATE `artifact_volume` SET `status`='current' WHERE `artifact_id`='7680' and`volume_id` in ('R19814L7','R29815L7','R39814L7');

UPDATE `t_file` SET `deleted`=0 WHERE `id` in ('165540','165162','165161','165160','165158','165157','165156','165155');

UPDATE `file` SET `deleted`=0 WHERE `id` in ('211112','211111','211113','211114','211116','211117','211118','211496');

UPDATE `artifact` SET `deleted`=0 WHERE `id`='7680';

UPDATE `job` SET `status`='marked_failed', `message`='Pls refer jira DU-987' WHERE `id` in ('109846','79128');

UPDATE `job` SET `status`='cancelled' WHERE `id`='490351';

*/

SET foreign_key_checks = 1;

