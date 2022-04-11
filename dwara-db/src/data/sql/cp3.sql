-- Some bad file(s) causing the job to fail stagnating the artifact in staged. Had to do the bad file process
-- Swami experimented something with camera setting and these are audience shots
UPDATE `file` SET `bad`=1, `reason`='Some camera experimentated file. Karthik anna confirmed these files can be deleted' WHERE `pathname` in (select pathname from t_file where id in (select file_id from t_t_file_job  where `job_id`='1132' and status='failed'));
UPDATE `t_file` SET `bad`=1, `reason`='Some camera experimentated file. Karthik anna confirmed these files can be deleted' WHERE `id` in (select file_id from t_t_file_job  where `job_id`='1132' and status='failed');

DELETE FROM `t_t_file_job` WHERE `job_id`='1132' and status='failed';

UPDATE `job` SET `status`='marked_completed' WHERE `id`='1132';

-- create dependent job using api

UPDATE `request` SET `status`='queued' WHERE `id` in ('323','322'); -- update both system and user request to queued



-- FX9_Proxy as an ingestable/source artifactclass
INSERT INTO `sequence` (`id`, `type`, `prefix`, `group`, `starting_number`, `ending_number`, `current_number`, `sequence_ref_id`) VALUES 
('video-fx9-proxy', 'artifact', 'VF', 0, 1, -1, 0, null);

-- ARTIFACTCLASS --
INSERT INTO `artifactclass` (`id`, `concurrent_volume_copies`, `description`, `display_order`, `import_only`, `path_prefix`, `source`, `artifactclass_ref_id`, `sequence_id`, `config`, `auto_ingest`) VALUES
('video-fx9-proxy',1,'fx9 mezzanine proxy video as an ingestable',3,0,'staged',1,NULL,'video-fx9-proxy',NULL,NULL);
 
-- ARTIFACTCLASS_VOLUME --
INSERT INTO `artifactclass_volume` (`artifactclass_id`, `volume_id`, `encrypted`, `active`) VALUES 
('video-fx9-proxy', 'M', 0, 1);

-- ACTION_ARTIFACTCLASS_FLOW --
INSERT INTO `action_artifactclass_flow` (`action_id`, `artifactclass_id`, `flow_id`, `active`) VALUES 
('ingest', 'video-fx9-proxy', 'cp-archive-flow', 1);


-- cancelled as bad video-fx9-proxy artifactclass config
UPDATE `request` SET `status`='cancelled' WHERE `id`='412';
UPDATE `request` SET `status`='cancelled' WHERE `id`='413';
UPDATE `request` SET `status`='cancelled' WHERE `id`='414';
UPDATE `request` SET `status`='cancelled' WHERE `id`='415';
